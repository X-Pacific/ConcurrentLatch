package org.zxp.ConcurrentLatch;

/**
 * @program: ConcurrentLatch
 * @description:
 * @author: X-Pacific zhang
 * @create: 2021-09-29 04:28
 **/
public class ConcurrentLatchException extends RuntimeException{
    String msg;
    public ConcurrentLatchException(String msg) {
        super(msg);
    }
    public ConcurrentLatchException(String msg,Throwable e) {
        super(msg,e);
    }
}
