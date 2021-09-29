package org.zxp.ConcurrentLatch;

import java.util.List;
import java.util.Map;

public interface ConcurrentLatch {
    /**
     * 插入一个任务
     * @param latchThread
     * @throws Exception
     */
    public <T,M> void put(LatchThread<T,M> latchThread, String taskName, List<T> m);

    /**
     * 插入一个任务
     * @param latchThread
     * @throws Exception
     */
    public <T,M> void put(LatchThread<T,M> latchThread, List<T> m);



    /**
     * 清除指定任务
     */
    public void cleanTask(String threadName);

    /**
     * 执行线程任务
     * @return
     * @throws Exception
     */
    public <T> Map<String ,List<LatchThreadReturn>> excute();

    /**
     * 根据任务名称获取返回结果
     * @param taskName
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> get(String taskName,Class<T> clazz);

    /**
     * 获取所有结果
     * @return
     */
    public List<List> getAll();


    /**
     * 释放资源，如果想重复使用此ConcurrentLatch，则需要调用这个方法
     */
    public void release();
}
