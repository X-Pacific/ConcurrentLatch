package org.zxp.ConcurrentLatch;

import java.util.Map;

public interface ConcurrentLatch {
    /**
     * 插入一个任务
     * @param latchThread
     * @throws Exception
     */
    public void put(LatchThread latchThread) throws Exception ;


    /**
     * 插入一个任务
     * @param latchThread
     * @throws Exception
     */
    public void put(LatchThread latchThread,String taskName) throws Exception ;

    /**
     * 清除所有任务
     */
    public void clean();

    /**
     * 清除指定任务
     */
    public void clean(String threadName);


    /**
     * 执行线程任务
     * @return
     * @throws Exception
     */
    public Map<String ,Object> excute() throws Exception;
}
