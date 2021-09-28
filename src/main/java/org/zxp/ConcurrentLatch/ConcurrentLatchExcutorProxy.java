package org.zxp.ConcurrentLatch;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 通过代理方式设置任务key
 * 目的是让每一个LatchThread调用handle方法都被map包围，标记其任务名称，达到执行任务返回结果与任务名称绑定的效果
 */
class ConcurrentLatchExcutorProxy implements ConcurrentLatch {
    /** 线程池 不能是单例，否则有事物问题*/
    private ExecutorService excutor = null;
    /**入参的map*/
    private Map<String,InputMapValue<Object>> inputMap = null;
    /**出参的map*/
    private Map<String ,Object> mapResult = new HashMap<String,Object>();

    public ConcurrentLatchExcutorProxy(){
        inputMap = new HashMap<String,InputMapValue<Object>>();
    }

    /**
     * 由代理调用，自动放入任务
     * @param latchThread
     * @param taskName
     * @throws Exception
     */
    @Override
    public void put(LatchThread latchThread,String taskName,Object m) throws Exception {
        String taskname = taskName;
        if(inputMap.containsKey(taskname)){
            throw new IllegalArgumentException("不能添加重复的任务");
        }
        /**代理类生成组件 必须每次重新初始化*/
        ConcurrentLatchBeanFactory beanFactory = new ConcurrentLatchBeanFactory();
        /**把入参也放入map以便调用时传入*/
        InputMapValue<Object> inputMapValue = new InputMapValue<Object>();
        LatchThread latchThreadProxy = beanFactory.getBean(latchThread,taskName,m);
        inputMapValue.setLatchThread(latchThreadProxy);
        inputMapValue.setM(m);
        inputMap.put(taskname,inputMapValue);
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
            final Map<String, InputMapValue<Object>> mapHandle = inputMap;
            List<Callable<Object>> callables = new ArrayList<Callable<Object>>();
            for (final String key : mapHandle.keySet()) {
                callables.add(() -> {
                    InputMapValue<Object> inputMapValue = mapHandle.get(key);
                    LatchThread latchThread = inputMapValue.getLatchThread();
                    Object m = inputMapValue.getM();
                    return latchThread.handle(m);
                });
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
            //todo 后续可以增加一个兜底策略，当报错时走备份方案
            throw e;
        }finally {
            LatchExcutorBlockingQueueManager.takeExcutor(excutor);
            clean();
        }
    }


    @Override
    public <T> T get(String taskName, Class<T> clazz){
        if(mapResult.containsKey(taskName)){
            return (T)mapResult.get(taskName);
        }else{
            throw new IllegalArgumentException("未知的任务名称:"+taskName);
        }
    }


    private static class InputMapValue<M>{
        private LatchThread latchThread;
        private M m;

        public LatchThread getLatchThread() {
            return latchThread;
        }

        public void setLatchThread(LatchThread latchThread) {
            this.latchThread = latchThread;
        }

        public M getM() {
            return m;
        }

        public void setM(M m) {
            this.m = m;
        }
    }
}
