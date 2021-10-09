package org.zxp.ConcurrentLatch.expand.standalonemr;

import java.util.List;
import java.util.function.Function;

public interface StandAloneMR {
    public <M,T> List<T> deal(List<M> inList, Function<M,T> function,CalcType calcType,SplitType splitType);
    public <M,T> List<T> deal(List<M> inList, Function<M,T> function,Integer tc,SplitType splitType);

    public <M,T> List<T> deal(List<M> inList, Function<M,T> function);
    public static enum CalcType{
        IO,CPU;
    }
    public static enum SplitType{
        HASH,RANGE;
    }
}
