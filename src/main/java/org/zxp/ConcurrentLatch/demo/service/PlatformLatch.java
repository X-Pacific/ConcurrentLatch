package org.zxp.ConcurrentLatch.demo.service;

import org.zxp.ConcurrentLatch.LatchTaskName;
import org.zxp.ConcurrentLatch.LatchThread;
import org.zxp.ConcurrentLatch.demo.dto.PlatformDto;

//@LatchTaskName("platform")
public class PlatformLatch implements LatchThread {

    @Override
    public PlatformDto handle() {
        System.out.println("我是AAA");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PlatformDto dto = new PlatformDto();
        dto.setName("0516");
        dto.setPremium(6500.98);
        dto.setPolicyNo("000000000001");
        return dto;
    }
}
