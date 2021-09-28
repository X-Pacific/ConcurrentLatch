package org.zxp.ConcurrentLatch.demo.service;

import org.zxp.ConcurrentLatch.LatchThread;
import org.zxp.ConcurrentLatch.demo.dto.RuleDto;
import org.zxp.ConcurrentLatch.demo.dto.RuleQo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: ConcurrentLatch
 * @description: 这个LatchThread演示有入参有返回值
 * @author: X-Pacific zhang
 * @create: 2019-06-12 10:09
 **/
public class RuleLatch implements LatchThread<RuleQo,RuleDto> {
    @Override
    public RuleDto handle(List<RuleQo> ruleQo) {
        System.out.println("我是RuleLatch");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RuleDto dto = new RuleDto();
        dto.setRuleID(ruleQo.stream().map(RuleQo::getRuleID).collect(Collectors.joining(",")));
        dto.setMmmm(dto.getMmmm()+ 9999);
        return dto;
    }
}
