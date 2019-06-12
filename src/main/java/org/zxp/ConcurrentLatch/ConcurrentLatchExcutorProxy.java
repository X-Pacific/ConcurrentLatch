package org.zxp.ConcurrentLatch;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 通过代理方式设置任务key
 * 目的是让每一个LatchThread调用handle方法都被map包围，标记其任务名称，达到执行任务返回结果与任务名称绑定的效果
 */
public class ConcurrentLatchExcutorProxy implements ConcurrentLatch {
    /** 线程池 不能是单例，否则有事物问题*/
    private ExecutorService excutor = null;
    private SimpleDateFormat sdf = null;
    /**入参的map*/
    private Map<String,LatchThread> inputMap = null;
    /**出参的map*/
    private Map<String ,Object> mapResult = new HashMap<String,Object>();

    public ConcurrentLatchExcutorProxy(){
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputMap = new HashMap<String,LatchThread>();
    }

    @Override
    public void put(LatchThread latchThread) throws Exception {
        throw new Exception("ConcurrentLatchExcutorProxy不支持当前put方法");
    }

    /**
     * 由代理调用，自动放入任务
     * @param latchThread
     * @param taskName
     * @throws Exception
     */
    @Override
    public void put(LatchThread latchThread,String taskName) throws Exception {
        String taskname = taskName;
        if(inputMap.containsKey(taskname)){
            throw new Exception("不能添加重复的任务");
        }
        /**代理类生成组件 必须每次重新初始化*/
        ConcurrentLatchBeanFactory beanFactory = new ConcurrentLatchBeanFactory();
        LatchThread latchThreadProxy = beanFactory.getBean(latchThread,taskName);
        inputMap.put(taskname,latchThreadProxy);
    }

    @Override
    public void clean() {
        inputMap.clear();
    }

    @Override
    public void clean(String threadName) {
        inputMap.remove(threadName);
    }

    @Override
    public Map<String, Object> excute() throws Exception {
        try {
            if (inputMap == null || inputMap.size() == 0) {
                return null;
            }
            excutor = LatchExcutorBlockingQueueManager.getExcutor(inputMap.size());
            if (inputMap == null || inputMap.size() == 0) {
                return null;
            }
            final Map<String, LatchThread> mapHandle = inputMap;
            Date before = new Date();
            List<Callable<Object>> callables = new ArrayList<Callable<Object>>();
            for (final String key : mapHandle.keySet()) {
                Callable<Object> task = new Callable<Object>() {
                    public Object call() throws Exception {
                        return mapHandle.get(key).handle();
                    }
                };
                callables.add(task);
            }
            List<Future<Object>> results = excutor.invokeAll(callables);
            if(results == null){
                return null;
            }
            for (Future<Object> f : results) {
                if(f == null || f.get() == null){
                    continue;
                }
                Map<String,Object> map = (Map<String,Object>)f.get();
                for (String key : map.keySet()){
                    mapResult.put(key, map.get(key));
                }
            }
            return mapResult;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            LatchExcutorBlockingQueueManager.takeExcutor(excutor);
        }
    }
}
