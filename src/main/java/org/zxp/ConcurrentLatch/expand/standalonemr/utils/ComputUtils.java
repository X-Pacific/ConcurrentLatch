package org.zxp.ConcurrentLatch.expand.standalonemr.utils;


import org.zxp.ConcurrentLatch.expand.standalonemr.StandAloneMR;

public class ComputUtils {
    public static int calcBestThreadCount(StandAloneMR.CalcType calcType){
        int cpus = Runtime.getRuntime().availableProcessors();
        if(calcType == StandAloneMR.CalcType.CPU){
            return cpus + 1;
        }else{
            return cpus * 2;
        }
    }
}
