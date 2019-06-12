package org.zxp.ConcurrentLatch.demo.service;

import org.zxp.ConcurrentLatch.LatchThread;

import java.util.concurrent.TimeUnit;

/**
 * @program: ConcurrentLatch
 * @description:
 * @author: X-Pacific zhang
 * @create: 2019-06-12 10:09
 **/
public class BlockLatch implements LatchThread {
    static volatile Integer index = 0;

    @Override
    public Object handle() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (BlockLatch.class){
            index ++;
        }
//        System.out.println(Thread.currentThread().getName());
        return index;
    }
}
