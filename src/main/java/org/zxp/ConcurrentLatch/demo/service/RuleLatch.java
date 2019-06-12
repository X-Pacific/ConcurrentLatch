package org.zxp.ConcurrentLatch.demo.service;

import org.zxp.ConcurrentLatch.LatchTaskName;
import org.zxp.ConcurrentLatch.LatchThread;
import org.zxp.ConcurrentLatch.demo.dto.RuleDto;

@LatchTaskName("rule")
public class RuleLatch implements  LatchThread {
    RuleDto dto = null;
    public RuleLatch(RuleDto args){
        dto = args;
    }

    @Override
    public RuleDto handle() {
        System.out.println("我是BBB");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dto.setMmmm(dto.getMmmm()+ 9999);
        return dto;
    }
}
