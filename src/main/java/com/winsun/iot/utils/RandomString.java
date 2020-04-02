package com.winsun.iot.utils;

import com.google.common.base.Stopwatch;
import com.winsun.iot.utils.idutil.IdWorker;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RandomString {
    private static String base = "abcdefghijklmnopqrstuvwxyz0123456789";
    public static String getRandomString(int length) {

        Random random = new Random(IdWorker.getId());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Set<String> value = new HashSet<>();
        Stopwatch sw = Stopwatch.createStarted();
        for (int i = 0; i < 1000000; i++) {
            String str = getRandomString(16);
            value.add(str);
        }
        sw.stop();

        System.out.println("random str is "+value.size());
        System.out.println("use time "+sw.elapsed(TimeUnit.MILLISECONDS));
    }
}
