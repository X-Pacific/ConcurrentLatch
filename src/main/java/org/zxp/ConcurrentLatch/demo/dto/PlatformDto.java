package org.zxp.ConcurrentLatch.demo.dto;

import org.zxp.ConcurrentLatch.LatchTaskName;

//@LatchTaskName("platform")
public class PlatformDto {
    private String name = "";

    public String getName() {
        return name;
    }

        @Override
        public String toString() {
            return "PlatformDto{" +
                    "name='" + name + '\'' +
                    ", premium=" + premium +
                    ", policyNo='" + policyNo + '\'' +
                    '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPremium() {
        return premium;
    }

    public void setPremium(double premium) {
        this.premium = premium;
    }

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    private double premium;
    private String policyNo = "";
}
