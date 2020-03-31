package com.winsun.iot.http.common;

import org.apache.ibatis.type.Alias;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpMap {
    String value() ;
}
