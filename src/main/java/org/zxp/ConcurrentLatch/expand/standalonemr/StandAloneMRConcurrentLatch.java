package org.zxp.ConcurrentLatch.expand.standalonemr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zxp.ConcurrentLatch.*;
import org.zxp.ConcurrentLatch.expand.standalonemr.utils.CollectionUtils;
import org.zxp.ConcurrentLatch.expand.standalonemr.utils.ComputUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class StandAloneMRConcurrentLatch implements StandAloneMR {
    private static Logger logger = LoggerFactory.getLogger(StandAloneMRConcurrentLatch.class);

    @Override
    public <M,T> List<T> deal(List<M> inList, Function<M,T> function,CalcType calcType,SplitType splitType) {
        int tc = ComputUtils.calcBestThreadCount(calcType);
        return deal(inList,function,tc,splitType);
    }

    public <M,T> List<T> deal(List<M> inList, Function<M,T> function,Integer tc,SplitType splitType) {
        ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
        List<List<M>> splitListO = null;
        if(splitType == SplitType.HASH) {
            splitListO  = CollectionUtils.splitListWithHash(inList, tc);
        } else if(splitType == SplitType.RANGE){
            splitListO = CollectionUtils.splitListWithCount(inList,true, tc.longValue());
        }
        if(splitListO == null){
            throw new ConcurrentLatchException("splitList is null");
        }
        List<List<M>> splitList = splitListO;
        Stream.iterate(0, n -> n + 1).limit(splitList.size()).forEach(n -> {
            LatchThread<M,T> latchThread = new LatchThread<M, T>() {
                @Override
                public LatchThreadReturn<T> handle(List<M> list) {
                    LatchThreadReturn latchThreadReturn = new LatchThreadReturn();
                    List<T> tList = new ArrayList<>();
                    if(list != null && list.size() > 0){
                        for (int i = 0; i < list.size(); i++) {
                            T t = function.apply(list.get(i));
                            tList.add(t);
                        }
                    }
                    latchThreadReturn.setLatchThreadReturn(tList);
                    return latchThreadReturn;
                }
            };
            excutor.put(latchThread,splitList.get(n));
        });
        excutor.excute();
        List<List> all = excutor.getAll();
        List<T> result = new ArrayList<>();
        if(all != null){
            all.forEach(s -> {
                s.forEach(m -> result.add((T)m));
            });
        }
        return result;
    }

    @Override
    public <M, T> List<T> deal(List<M> inList, Function<M, T> function) {
        int tc = ComputUtils.calcBestThreadCount(CalcType.CPU);
        return deal(inList,function,tc,SplitType.HASH);
    }


    private static class Init{
        static{
            ConcurrentLatchExcutorFactory.init(
                    ConcurrentLatchCfg.builder()
                            .maxCorePoolSize(10)
                            .maxExcutorSize(10)
                            .maxPoolSizeRatio(2).build()
            );
        }
    }
}
