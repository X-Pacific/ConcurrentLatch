package org.zxp.ConcurrentLatch.demo;

import org.zxp.ConcurrentLatch.*;
import org.zxp.ConcurrentLatch.demo.dto.RuleDto;
import org.zxp.ConcurrentLatch.demo.service.BlockLatch;
import org.zxp.ConcurrentLatch.demo.service.PlatformLatch;
import org.zxp.ConcurrentLatch.demo.service.RuleLatch;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class TestProxy {

    public static void main(String[] args) throws Exception {
        ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
        LatchThread platformLatchThread = new PlatformLatch();
        LatchThread platformLatchThread2 = new PlatformLatch();
        RuleDto ruleDto = new RuleDto();
        ruleDto.setRuleID("zxp123");
        ruleDto.setMmmm(0.00001);
        LatchThread ruleLatchThread = new RuleLatch(ruleDto);

        excutor.put(platformLatchThread,"aaa");
        excutor.put(ruleLatchThread,"bbb");
        excutor.put(platformLatchThread,"ccc");
//        excutor.put(platformLatchThread);
//        excutor.put(ruleLatchThread);
//        excutor.put(platformLatchThread);
        Map<String, Object> map = excutor.excute();
        for (String key : map.keySet()) {
            System.out.println("taskname==========" + key);
            Object out = map.get(key);
            System.out.println(out);
        }
        System.out.println("看看能不能阻塞");

//        TestProxy testProxy = new TestProxy();
//        long begin = System.currentTimeMillis();
//        testProxy.run3();
//        long end = System.currentTimeMillis();
//        System.out.println("耗时："+(end-begin));
//        LatchExcutorBlockingQueueManager.print();
    }

    /**
     * 测试线程池管理器会不会被挤爆
     */
    private void run(){
        final CountDownLatch c = new CountDownLatch(4);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
                    LatchThread blockLatch = new BlockLatch();
                    excutor.put(blockLatch, "1");
                    Map<String, Object> map = excutor.excute();
                    System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
                    c.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
                    LatchThread blockLatch = new BlockLatch();
                    excutor.put(blockLatch, "1");
                    Map<String, Object> map = excutor.excute();
                    System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
                    c.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
                    LatchThread blockLatch = new BlockLatch();
                    excutor.put(blockLatch, "1");
                    Map<String, Object> map = excutor.excute();
                    System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
                    c.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
                    LatchThread blockLatch = new BlockLatch();
                    excutor.put(blockLatch, "1");
                    Map<String, Object> map = excutor.excute();
                    System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
                    c.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            c.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试线程池工作饱和情况下的耗时情况
     */
    private void run2(){
        int count = 16;
        final CountDownLatch c = new CountDownLatch(count);
        for (int i = 0; i < 15; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
                        LatchThread blockLatch = new BlockLatch();
                        excutor.put(blockLatch, "1");
                        Map<String, Object> map = excutor.excute();
                        System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
                        c.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
                    excutor.put(new BlockLatch(), "1");
                    excutor.put(new BlockLatch(), "2");
                    Map<String, Object> map = excutor.excute();
                    System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
                    System.out.println(Thread.currentThread().getName()+"  "+map.get("2"));
                    c.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            c.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 预期：压力测试100次，线程池最多20个 ，每批次运行20个线程池（1秒运行完成），需要运行5秒能运行完成
     * 要求：线程池最多只能有20个(每个线程池设置线程数为1个，最多也就是20个线程)，并且计数器等值是正确的运行结果
     */
    private void run3(){
        int count = 100;
        final CountDownLatch c = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
                        LatchThread blockLatch = new BlockLatch();
                        excutor.put(blockLatch, "1");
                        Map<String, Object> map = excutor.excute();
                        System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        c.countDown();
                    }
                }
            }).start();
        }
        try {
            c.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
