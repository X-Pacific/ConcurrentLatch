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
    private List<M> latchThreadReturn;
    private String key;

    public List<M> get() {
        return latchThreadReturn;
    }

    public void setLatchThreadReturn(List<M> latchThreadReturn) {
        this.latchThreadReturn = latchThreadReturn;
    }

    public static <M> LatchThreadReturn set(M m){
        LatchThreadReturn<M> latchThreadReturn = new LatchThreadReturn<>();
        latchThreadReturn.setLatchThreadReturn(Arrays.asList(m));
        return latchThreadReturn;
    }

    public static <M> LatchThreadReturn set(List<M> list){
        LatchThreadReturn<M> latchThreadReturn = new LatchThreadReturn<>();
        latchThreadReturn.setLatchThreadReturn(list);
        return latchThreadReturn;
    }

    protected String getKey() {
        return key;
    }

    protected void setKey(String key) {
        this.key = key;
    }
}
