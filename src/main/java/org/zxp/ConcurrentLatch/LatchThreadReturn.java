package org.zxp.ConcurrentLatch;

import java.util.Arrays;
import java.util.List;

/**
 * @program: ConcurrentLatch
 * @description:
 * @author: X-Pacific zhang
 * @create: 2021-09-29 06:15
 **/
public class LatchThreadReturn<M> {
    List<M> latchThreadReturn;

    public List<M> getLatchThreadReturn() {
        return latchThreadReturn;
    }

    public void setLatchThreadReturn(List<M> latchThreadReturn) {
        this.latchThreadReturn = latchThreadReturn;
    }

    public static <M> LatchThreadReturn returnLatchThreadReturn(M m){
        LatchThreadReturn<M> latchThreadReturn = new LatchThreadReturn<>();
        latchThreadReturn.setLatchThreadReturn(Arrays.asList(m));
        return latchThreadReturn;
    }

    public static <M> LatchThreadReturn returnLatchThreadReturn(List<M> list){
        LatchThreadReturn<M> latchThreadReturn = new LatchThreadReturn<>();
        latchThreadReturn.setLatchThreadReturn(list);
        return latchThreadReturn;
    }
}
