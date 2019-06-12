package org.zxp.ConcurrentLatch.demo.service;

import org.zxp.ConcurrentLatch.LatchThread;

public class WucanLatch implements LatchThread {
    @Override
    public Object handle() {
        System.out.println("我是CCC");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
