package org.zxp.ConcurrentLatch;

/**
 * 需要让并发闩执行的任务必须实现这个接口
 */
public interface LatchThread<M,T> {
    public T handle(M m);
}
