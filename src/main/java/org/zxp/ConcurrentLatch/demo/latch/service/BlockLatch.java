package org.zxp.ConcurrentLatch.demo.latch.service;

import org.zxp.ConcurrentLatch.LatchThread;
import org.zxp.ConcurrentLatch.LatchThreadReturn;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @program: ConcurrentLatch
 * @description: 这个LatchThread演示阻塞1秒的任务
 * @author: X-Pacific zhang
 * @create: 2019-06-12 10:09
 **/
public class BlockLatch implements LatchThread {
    static volatile Integer index = 0;

    @Override
    public LatchThreadReturn<Integer> handle(List m) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (BlockLatch.class){
            index ++;
        }
        return LatchThreadReturn.set(index);
    }
}
