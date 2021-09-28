package org.zxp.ConcurrentLatch;

import java.util.List;
import java.util.Map;

public interface ConcurrentLatch {
    /**
     * 插入一个任务
     * @param latchThread
     * @throws Exception
     */
    public <T,M> void put(LatchThread<T,M> latchThread, String taskName, List<Object> m) throws Exception ;

    /**
     * 插入一个任务
     * @param latchThread
     * @throws Exception
     */
    public <T,M> void put(LatchThread<T,M> latchThread, List<Object> m) throws Exception ;


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
    public <T> Map<String ,T> excute() throws Exception;

    /**
     * 根据任务名称获取返回结果
     * @param taskName
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(String taskName,Class<T> clazz);

    /**
     * 根据任务名称获取返回结果
     * @param <T>
     * @return
     */
    public List getAll();
}
