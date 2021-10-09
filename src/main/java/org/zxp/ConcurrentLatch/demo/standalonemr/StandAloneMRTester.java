package org.zxp.ConcurrentLatch.demo.standalonemr;


import org.zxp.ConcurrentLatch.expand.standalonemr.StandAloneMR;
import org.zxp.ConcurrentLatch.expand.standalonemr.StandAloneMRConcurrentLatch;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StandAloneMRTester {
    public static void main(String[] args) throws Exception {
        Date d1 = new Date();
        StandAloneMR standAloneMR = new StandAloneMRConcurrentLatch();
        List<Integer> rt = standAloneMR.deal(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), s -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println(Thread.currentThread().getName()+"===="+s);
            return s + 1;
        });
        Date d2 = new Date();
        System.out.println(d2.getTime()-d1.getTime());
        Collections.sort(rt);
        rt.forEach(System.out::println);
    }
}
