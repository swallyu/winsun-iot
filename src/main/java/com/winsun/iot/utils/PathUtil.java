package com.winsun.iot.utils;

public class PathUtil {

    public static String getPath(String uri,int pos){
       return getPath(uri,pos,"/");
    }

    public static String getPath(String uri,int pos,String delimit){
        String[] tmp = uri.split(delimit);
        if(pos<tmp.length){
            return tmp[pos];
        }
        return null;
    }
}
