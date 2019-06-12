package org.zxp.ConcurrentLatch.demo.dto;

import org.zxp.ConcurrentLatch.LatchTaskName;

@LatchTaskName("rule")
public class RuleDto {
    public String getRuleID() {
        return ruleID;
    }

    @Override
    public String toString() {
        return "RuleDto{" +
                "ruleID='" + ruleID + '\'' +
                ", mmmm=" + mmmm +
                '}';
    }
    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public double getMmmm() {
        return mmmm;
    }

    public void setMmmm(double mmmm) {
        this.mmmm = mmmm;
    }

    private String ruleID = "";
    private double mmmm;
}
