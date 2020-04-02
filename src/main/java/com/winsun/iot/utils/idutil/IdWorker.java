package com.winsun.iot.utils.idutil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by liangzy on 2018-10-30.
 */
public class IdWorker {
    /**
     * 默认主机和进程的机器码
     */
    private static Sequence WORKER = new Sequence();

    public static long getId() {
        return WORKER.nextId();
    }

    public static void main(String[] args) {
        Set<Long> value = new HashSet<>();
        System.out.println(System.currentTimeMillis()+"");
        for (int i = 0; i < 1000000; i++) {
            Long v = getId();
            value.add(v);
        }
        System.out.println("total:"+value.size());
        System.out.println(System.currentTimeMillis()+"");

    }
}
