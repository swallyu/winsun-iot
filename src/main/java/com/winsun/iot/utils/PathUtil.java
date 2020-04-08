package com.winsun.iot.utils;

public class PathUtil {

    public static String getPath(String uri, int pos) {
        return getPath(uri, pos, "/");
    }

    public static String getPathLast(String uri) {
        return getPathLast(uri,  "/");
    }

    public static String getPath(String uri, int pos, String delimit) {
        String[] tmp = uri.split(delimit);
        if (pos < tmp.length) {
            return tmp[pos];
        }
        return null;
    }

    public static String getPathLast(String uri, String delimit) {
        String[] tmp = uri.split(delimit);
        return tmp[tmp.length - 1];
    }
}
