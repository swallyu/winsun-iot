package com.winsun.iot;

import ch.qos.logback.classic.util.ContextInitializer;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.http.HttpServer;
import com.winsun.iot.iocmodule.Ioc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class IotApplication {

    private static final Logger logger = LoggerFactory.getLogger(IotApplication.class);
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Condition STOP = LOCK.newCondition();

    static {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback.xml");
    }
    public static void main(String[] args) {


        logger.info("service start success !~");

        DeviceManager mgr = Ioc.getInjector().getInstance(DeviceManager.class);
        mgr.start();

        HttpServer.getInstance().start();

        addHook();
        //主线程阻塞等待，守护线程释放锁后退出
        try {
            LOCK.lock();
            STOP.await();
        } catch (InterruptedException e) {
            logger.warn(" service   stopped, interrupted by other thread!", e);
        } finally {
            LOCK.unlock();
        }

    }

    /**
     * <p>
     * Discription:[添加一个守护线程]
     *
     * @param
     *
     */
    private static void addHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            try {
                //todo 系统退出清理
            } catch (Exception e) {
                logger.error("StartMain stop exception ", e);
            }

            logger.info("jvm exit, all service stopped.");
            try {
                LOCK.lock();
                STOP.signal();
            } finally {
                LOCK.unlock();
            }
        }, "StartMain-shutdown-hook"));
    }
}
