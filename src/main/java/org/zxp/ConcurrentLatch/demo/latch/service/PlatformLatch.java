package org.zxp.ConcurrentLatch.demo.latch.service;

import org.zxp.ConcurrentLatch.LatchThread;
import org.zxp.ConcurrentLatch.LatchThreadReturn;
import org.zxp.ConcurrentLatch.demo.latch.dto.PlatformDto;

import java.util.List;

/**
 * @program: ConcurrentLatch
 * @description: 这个LatchThread演示无入参有返回值
 * @author: X-Pacific zhang
 * @create: 2019-06-12 10:09
 **/
public class PlatformLatch implements LatchThread<Void ,PlatformDto> {
    @Override
    public LatchThreadReturn<PlatformDto> handle(List<Void> v) {
        System.out.println("我是PlatformLatch");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PlatformDto dto = new PlatformDto();
        dto.setName("0516");
        dto.setPremium(6500.98);
        dto.setPolicyNo("000000000001");
        return LatchThreadReturn.set(dto);
    }
}