package com.winsun.iot.dao;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtilTest {

    public static void main(String[] args) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter22 = DateTimeFormatter.ofPattern("yyyyMMdd-HH");
        LocalDateTime time = LocalDateTime.of(2020, 4, 28, 0, 0);
        for (int i = 0; i < 60; i++) {
            String ext = time.format(formatter22);
            int v = time.getMinute() / 5 * 5;

            ext = ext + String.format("%02d", v);

            System.out.println(formatter.format(time) + " => " + ext);
            time = time.plusMinutes(1);
        }

    }
}
