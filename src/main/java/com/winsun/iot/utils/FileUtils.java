package com.winsun.iot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.stream.Collectors;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 获取windows/linux的项目根目录
     *
     * @return
     */
    public static String getProjectPath() {
        String fileUrl = null;
        try {
            if (Thread.currentThread().getContextClassLoader().getResource("") == null) {
                URI uri = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                String path = uri.getPath();
                if (path.contains(".jar")) {
                    path = path.substring(0, path.lastIndexOf("/") + 1);
                }
                fileUrl = java.net.URLDecoder.decode(path, "utf-8");
            } else {
                fileUrl = Thread.currentThread().getContextClassLoader().getResource("").getPath();
                fileUrl = java.net.URLDecoder.decode(fileUrl, "utf-8");
            }
            logger.info(fileUrl);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            return fileUrl;
        }
    }

    public static String readContent(String file) {
        String content = null;
        try {
            if (Thread.currentThread().getContextClassLoader().getResource("") == null
                    && FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().endsWith(".jar")) {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);

                try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
                    content = r.lines().collect(Collectors.joining("\n"));
                }
            } else {
                String fileUrl = Thread.currentThread().getContextClassLoader().getResource("").getPath();
                fileUrl = java.net.URLDecoder.decode(fileUrl, "utf-8").substring(1) + file;
                File f = new File(fileUrl);
                if(!f.exists()&&fileUrl.contains("test-classes")){
                    fileUrl = fileUrl.replaceAll("test-classes","classes");
                    f = new File(fileUrl);
                }
                try (BufferedReader r = new BufferedReader(new FileReader(fileUrl))) {
                    content = r.lines().collect(Collectors.joining("\n"));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return content;
    }

    public static void main(String[] args) {

    }
}
