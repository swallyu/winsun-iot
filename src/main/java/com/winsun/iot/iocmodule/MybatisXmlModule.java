package com.winsun.iot.iocmodule;

import com.winsun.iot.utils.FileUtils;
import org.mybatis.guice.XMLMyBatisModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class MybatisXmlModule extends XMLMyBatisModule {
    private static final Logger logger = LoggerFactory.getLogger(MybatisXmlModule.class);
    @Override
    protected void initialize() {
        try {
            String content = FileUtils.readContent("application.properties");
            Properties prop = new Properties();
            prop.load(new StringReader(content));
            setClassPathResource("mybatis-config.xml");
            addProperties(prop);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
    }
}
