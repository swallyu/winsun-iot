package com.winsun.iot.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.iocmodule.Ioc;
import com.winsun.iot.persistence.PersistenceBatchService;
import com.winsun.iot.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceSensorData {

    private static final String TABLE_DATA_SENSOR_REAL="data_sensor_real";
    private static final String TABLE_DATA_SENSOR="data_sensor";

    private static Logger logger = LoggerFactory.getLogger(DeviceSensorData.class);
    private LocalDateTime time = LocalDateTime.now();                                                                                //最近数据更新时间
    private int period;                                                                                    //统计周期，单位为分钟
    private ConcurrentHashMap<String, Double> datacurrent = new ConcurrentHashMap<String, Double>();        //当前最新数据
    private SensorDataStatistics datasta = new SensorDataStatistics();

    public DeviceSensorData(int period) {
        this.period = period;
    }

    public boolean updateData(LocalDateTime datetime, String sensorname, Double val) {
        if (datetime != null && !"".equals(sensorname) && sensorname != null && val != null) {
            datacurrent.put(sensorname, val);
            time = datetime;
            return true;
        } else {
            return false;
        }
    }

    private void updateData(LocalDateTime datatime_dev, JSONObject sensors) {
        List<String> sensorNames = new ArrayList<>();
        for (String sensorname : sensors.keySet()) {
            updateData(datatime_dev, sensorname, sensors.getDouble(sensorname));
            sensorNames.add(sensorname);
        }
        List<String> removeKeys = new ArrayList<>();
        for (String key : datacurrent.keySet()) {
            if(!sensorNames.contains(key)){
                removeKeys.add(key);
            }
        }
        removeKeys.forEach(m->{
            datacurrent.remove(m);
        });
    }

    /**
     * 更新实时数据
     * @param datetime
     * @param baseid
     * @param sensordata
     * @return
     */
    public boolean updateDataAndPull2DB(LocalDateTime datetime, String baseid, JSONObject sensordata) {
        if (sensordata == null) {
            return false;
        }
        String timestr = sensordata.getString("Time");
        LocalDateTime datatime_dev = null;
        try {
            datatime_dev = DateTimeUtils.parseFullSecond(timestr);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            datatime_dev = datetime;
        }
        if (datatime_dev.isEqual(this.time)) {
            return false;
        }
        JSONObject sensors = sensordata.getJSONObject("Sensors");
        for (String sensorname : sensors.keySet()) {
            updateData(datatime_dev, sensorname, sensors.getDouble(sensorname));
        }
        updateData(datatime_dev, sensors);
        if (!"".equals(baseid) && baseid != null) {
            StringBuffer sqlstr = new StringBuffer();
            sqlstr.append(baseid).append("\t")
                    .append(DateTimeUtils.formatFullSecond(datatime_dev)).append("\t")
                    .append("0").append("\t")
                    .append(sensors.toString()).append("\t");

            PersistenceBatchService batchService = Ioc.getInjector().getInstance(PersistenceBatchService.class);
            batchService.addDataToTask(TABLE_DATA_SENSOR_REAL,sqlstr.toString());
        }
        return true;
    }

    /**
     * 周期数据
     * @param datetime
     * @param baseId
     * @param stssensordata
     * @return
     */
    public boolean updateStsDataAndPull2DB(LocalDateTime datetime, String baseId, JSONObject stssensordata) {
        if (stssensordata == null) {
            return false;
        }
        String timestr = stssensordata.getString("Time");
        LocalDateTime datatime_dev = null;
        try {
            datatime_dev = DateTimeUtils.parseFullSecond(timestr);
        } catch (Exception e) {
            datatime_dev = datetime;
        }

        if (datatime_dev.isEqual(datasta.getStatisticstime())) {
            return false;
        }

        int period = stssensordata.getIntValue("period");
        JSONObject sensors = stssensordata.getJSONObject("Sensors");
        for (String name : sensors.keySet()) {
            datasta.setStatisticstime(datatime_dev);
            StatisticsData stsdata = new StatisticsData();
            JSONObject sensordata = sensors.getJSONObject(name);
            Double max = sensordata.getDouble("max");
            Double min = sensordata.getDouble("min");
            Integer count = sensordata.getInteger("count");
            Double avg = sensordata.getDouble("avg");
            if (max != null) stsdata.setMax(max);
            if (min != null) stsdata.setMin(min);
            if (count != null) stsdata.setCount(count);
            if (avg != null) stsdata.setAvg(avg);
            if (count != null && avg != null) stsdata.setSum(count * avg);
            datasta.putDatastatistics(name, stsdata);
        }

        JSONObject jo = datasta.pullStatisticsData();
        if (jo != null) {
            int periodValue = period / 60;
            String tableName = periodValue > 0 ? TABLE_DATA_SENSOR : TABLE_DATA_SENSOR_REAL;

            if (!"".equals(baseId) && baseId != null) {
                StringBuffer sqlstr = new StringBuffer();
                sqlstr.append(baseId).append("\t")
                        .append(DateTimeUtils.formatFullSecond(datatime_dev)).append("\t")
                        .append((period / 60) + "").append("\t")
                        .append(jo.toString()).append("\t");

                String sql = sqlstr.toString();

                PersistenceBatchService batchService = Ioc.getInjector().getInstance(PersistenceBatchService.class);
                batchService.addDataToTask(tableName,sql);
                return true;
            }
        }
        logger.error("获取当前的周期统计失败 " + baseId + " time:" + DateTimeUtils.formatFullSecond(datetime));
        return false;
    }

    public class SensorDataStatistics{
        private LocalDateTime statisticstime = LocalDateTime.now().plusDays(-1);                //最新一次提取统计的时间
        private Map<String, StatisticsData> datastatistics = new ConcurrentHashMap<String, StatisticsData>();                //最新一次统计数据
        private AtomicBoolean lock = new AtomicBoolean(false);

        public void putDatastatistics(String sensorname, StatisticsData stsdata) {
            datastatistics.put(sensorname, stsdata);
        }

        public LocalDateTime getStatisticstime() {
            return statisticstime;
        }

        public void setStatisticstime(LocalDateTime statisticstime) {
            this.statisticstime = statisticstime;
        }

        public JSONObject pullStatisticsData() {

            Map<String,Object> data = new HashMap<>();
            this.datastatistics.forEach(data::put);
            JSONObject jo = new JSONObject(data);

            return jo;
        }
    }

    public class StatisticsData {
        private int count = 0;
        private Double avg = -10000.0;
        private Double sum = -10000.0;
        private Double max = -10000.0;
        private Double min = -10000.0;

        public void reset() {
            count = 0;
            avg = -10000.0;
            sum = -10000.0;
            max = -10000.0;
            min = -10000.0;
        }

        public synchronized void offerData(Double val) {
            if (count == 0) {
                count++;
                avg = val;
                sum = val;
                max = val;
                min = val;
            } else {
                count++;
                sum = sum + val;
                avg = sum / count;
                max = Math.max(max, val);
                min = Math.min(min, val);
            }
        }

        public Double getAvg() {
            return avg;
        }

        public Double getSum() {
            return sum;
        }

        public Double getMax() {
            return max;
        }

        public Double getMin() {
            return min;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setAvg(Double avg) {
            this.avg = avg;
        }

        public void setSum(Double sum) {
            this.sum = sum;
        }

        public void setMax(Double max) {
            this.max = max;
        }

        public void setMin(Double min) {
            this.min = min;
        }
    }
}
