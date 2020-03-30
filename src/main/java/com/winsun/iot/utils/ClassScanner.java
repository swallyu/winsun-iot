package com.winsun.iot.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClassScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClassScanner.class);

    private String pkgName;
    private ClassLoader classLoader;

    public ClassScanner(String pkgName, ClassLoader classLoader) {
        this.pkgName = pkgName;
        this.classLoader = classLoader;
    }

    public ClassScanner(String pkgName) {
        this.pkgName = pkgName;
        this.classLoader = getClass().getClassLoader();
    }

    public List<String> doScan(List<String> classNameList) throws IOException {
        return doScan(pkgName, classNameList);
    }

    public List<String> doScan(String pkgName, List<String> classNameList) throws IOException {
        String pkgPath = pkgName.replaceAll("\\.", "/");
        URL url = classLoader.getResource(pkgPath);

        String filePath = getRootPath(url);
        List<String> tmpFileList = new ArrayList<>();

        logger.debug("pkgPath {}", pkgPath);
        logger.debug("filePath {}", filePath);
        if (isJarFile(filePath)) {
            readFromJarFile(tmpFileList, filePath, pkgPath);
        } else {
            readFromPath(tmpFileList, filePath);
        }
        for (String clzName : tmpFileList) {
            clzName = clzName.replaceAll(pkgPath, "");
            if (clzName.startsWith("/")) {
                clzName = clzName.substring(1).replaceAll("/", ".");
            }
            logger.debug("clzname {}", clzName);
            if (isClassFile(clzName)) {
                classNameList.add(toFullyQualifiedName(clzName, pkgName));
            } else {
                doScan(pkgName + "." + clzName, classNameList);
            }
        }
        return classNameList;
    }

    private static String toFullyQualifiedName(String shortName, String basePackage) {
        StringBuilder sb = new StringBuilder(basePackage);
        sb.append('.');
        sb.append(trimExtension(shortName));
        //打印出结果
        logger.debug("消息处理器 {}", sb.toString());
        return sb.toString();
    }

    private void readFromPath(List<String> result, String path) {
        File file = new File(path);
        String[] names = file.list();

        if (null == names) {
            return;
        }

        result.addAll(Arrays.asList(names));
    }

    private void readFromJarFile(List<String> nameList, String jarPath, String splashedPackageName) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("从JAR包中读取类: {}", jarPath);
        }
        JarInputStream jarIn = new JarInputStream(new FileInputStream(jarPath));
        JarEntry entry = jarIn.getNextJarEntry();
        while (null != entry) {
            String name = entry.getName();
            if (name.startsWith(splashedPackageName) && isClassFile(name)) {
                nameList.add(name);
            }

            entry = jarIn.getNextJarEntry();
        }
    }

    private boolean isClassFile(String name) {
        return name.endsWith(".class");
    }


    private static boolean isJarFile(String path) {
        return path.endsWith(".jar");
    }

    public static String trimExtension(String name) {
        int pos = name.lastIndexOf('.');
        if (-1 != pos) {
            return name.substring(0, pos);
        }

        return name;
    }

    private static String getRootPath(URL path) {
        String file = null;
        try {
            file = URLDecoder.decode(path.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int pos = file.indexOf('!');
        if (pos == -1) {
            return file;
        }

        return file.substring(5, pos);
    }


    public static void main(String[] args) {

        String clzName = "impl.BizMsgHandler.class";
        String pkg = "com.zg.mqtt.server.msghandler";

        String name = toFullyQualifiedName(clzName, pkg);

        logger.info(name);
    }


}
