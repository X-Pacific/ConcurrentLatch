package org.zxp.ConcurrentLatch.demo;

import org.zxp.ConcurrentLatch.*;
import org.zxp.ConcurrentLatch.demo.dto.PlatformDto;
import org.zxp.ConcurrentLatch.demo.dto.RuleDto;
import org.zxp.ConcurrentLatch.demo.dto.RuleQo;
import org.zxp.ConcurrentLatch.demo.service.PlatformLatch;
import org.zxp.ConcurrentLatch.demo.service.RuleLatch;

import java.util.Arrays;

public class TestProxy {
    public static void main(String[] args) throws Exception {
        //配置ConcurrentLatch（全局只能配置一次）
        ConcurrentLatchExcutorFactory.init(
                ConcurrentLatchCfg.builder()
                    .maxCorePoolSize(10)
                    .maxExcutorSize(10)
                    .maxPoolSizeRatio(2).build()
        );
        //获取一个ConcurrentLatch执行器
        ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
        //声明RuleLatch
        RuleQo ruleQo = new RuleQo();
        ruleQo.setRuleID("zxp1");
        RuleQo ruleQo2 = new RuleQo();
        ruleQo2.setRuleID("zxp2");
        RuleQo ruleQo3 = new RuleQo();
        ruleQo3.setRuleID("zxp3");
        LatchThread ruleLatchThread = new RuleLatch();
        //声明PlatformLatch
        LatchThread platformLatchThread = new PlatformLatch();
        //将LatchThread（任务）置入ConcurrentLatch框架
        excutor.put(platformLatchThread,"platformLatch",null);
        excutor.put(ruleLatchThread,"ruleLatch", Arrays.asList(ruleQo,ruleQo2,ruleQo3));
        //执行全部任务
        excutor.excute();
        //获取返回结果
        PlatformDto platformDto = excutor.get("platformLatch",PlatformDto.class);
        RuleDto ruleDto = excutor.get("ruleLatch",RuleDto.class);
        System.out.println("platformLatch");
        System.out.println(platformDto);

        System.out.println("ruleLatch");
        System.out.println(ruleDto);

        ConcurrentLatchExcutorFactory.print();
    }

    ///**
    // * 测试线程池管理器会不会被挤爆
    // */
    //private void run(){
    //    final CountDownLatch c = new CountDownLatch(4);
    //    new Thread(new Runnable() {
    //        @Override
    //        public void run() {
    //            try {
    //                ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
    //                LatchThread blockLatch = new BlockLatch();
    //                excutor.put(blockLatch, "1");
    //                Map<String, Object> map = excutor.excute();
    //                System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
    //                c.countDown();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //        }
    //    }).start();
    //    new Thread(new Runnable() {
    //        @Override
    //        public void run() {
    //            try {
    //                ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
    //                LatchThread blockLatch = new BlockLatch();
    //                excutor.put(blockLatch, "1");
    //                Map<String, Object> map = excutor.excute();
    //                System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
    //                c.countDown();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //        }
    //    }).start();
    //    new Thread(new Runnable() {
    //        @Override
    //        public void run() {
    //            try {
    //                ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
    //                LatchThread blockLatch = new BlockLatch();
    //                excutor.put(blockLatch, "1");
    //                Map<String, Object> map = excutor.excute();
    //                System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
    //                c.countDown();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //        }
    //    }).start();
    //    new Thread(new Runnable() {
    //        @Override
    //        public void run() {
    //            try {
    //                ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
    //                LatchThread blockLatch = new BlockLatch();
    //                excutor.put(blockLatch, "1");
    //                Map<String, Object> map = excutor.excute();
    //                System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
    //                c.countDown();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //        }
    //    }).start();
    //    try {
    //        c.await();
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //}
    //
    //
    ///**
    // * 测试线程池工作饱和情况下的耗时情况
    // */
    //private void run2(){
    //    int count = 16;
    //    final CountDownLatch c = new CountDownLatch(count);
    //    for (int i = 0; i < 15; i++) {
    //        new Thread(new Runnable() {
    //            @Override
    //            public void run() {
    //                try {
    //                    ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
    //                    LatchThread blockLatch = new BlockLatch();
    //                    excutor.put(blockLatch, "1");
    //                    Map<String, Object> map = excutor.excute();
    //                    System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
    //                    c.countDown();
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        }).start();
    //    }
    //
    //    new Thread(new Runnable() {
    //        @Override
    //        public void run() {
    //            try {
    //                ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
    //                excutor.put(new BlockLatch(), "1");
    //                excutor.put(new BlockLatch(), "2");
    //                Map<String, Object> map = excutor.excute();
    //                System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
    //                System.out.println(Thread.currentThread().getName()+"  "+map.get("2"));
    //                c.countDown();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //        }
    //    }).start();
    //
    //    try {
    //        c.await();
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //}
    //
    //
    ///**
    // * 预期：压力测试100次，线程池最多20个 ，每批次运行20个线程池（1秒运行完成），需要运行5秒能运行完成
    // * 要求：线程池最多只能有20个(每个线程池设置线程数为1个，最多也就是20个线程)，并且计数器等值是正确的运行结果
    // */
    //private void run3(){
    //    int count = 100;
    //    final CountDownLatch c = new CountDownLatch(count);
    //    for (int i = 0; i < count; i++) {
    //        new Thread(new Runnable() {
    //            @Override
    //            public void run() {
    //                try {
    //                    ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
    //                    LatchThread blockLatch = new BlockLatch();
    //                    excutor.put(blockLatch, "1");
    //                    Map<String, Object> map = excutor.excute();
    //                    System.out.println(Thread.currentThread().getName()+"  "+map.get("1"));
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }finally {
    //                    c.countDown();
    //                }
    //            }
    //        }).start();
    //    }
    //    try {
    //        c.await();
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //}
}
