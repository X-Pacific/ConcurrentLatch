package org.zxp.ConcurrentLatch;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 通过代理方式设置任务key
 * 目的是让每一个LatchThread调用handle方法都被map包围，标记其任务名称，达到执行任务返回结果与任务名称绑定的效果
 */
class ConcurrentLatchExcutorProxy implements ConcurrentLatch {
    /** 线程池 不能是单例，否则有事物问题*/
    private ExecutorService excutor = null;
    /**入参的map*/
    private Map<String,InputMapValue<List>> inputMap = null;
    /**出参的map*/
    private Map<String ,List<LatchThreadReturn>> mapResult = null;

    public ConcurrentLatchExcutorProxy(){
        inputMap = new HashMap<>();
        mapResult = new HashMap<>();
    }



    /**
     * 由代理调用，自动放入任务
     * @param latchThread
     * @param taskName
     * @throws Exception
     */
    @Override
    public <T, M> void put(LatchThread<T, M> latchThread, String taskName, List<T> m) {
        String taskname = taskName;
        if(inputMap.containsKey(taskname)){
            throw new ConcurrentLatchException("repeat task name");
        }
        /**代理类生成组件 必须每次重新初始化*/
        ConcurrentLatchBeanFactory<T,M> beanFactory = new ConcurrentLatchBeanFactory();
        /**把入参也放入map以便调用时传入*/
        InputMapValue<List> inputMapValue = new InputMapValue<>();
        LatchThread latchThreadProxy = beanFactory.getBean(latchThread,taskName,m);
        inputMapValue.setLatchThread(latchThreadProxy);
        inputMapValue.setM(m);
        inputMap.put(taskname,inputMapValue);
    }

    @Override
    public <T, M> void put(LatchThread<T, M> latchThread, List<T> m) {
        String taskname = UUID.randomUUID().toString();
        while(inputMap.containsKey(taskname)){
            taskname = UUID.randomUUID().toString();
        }
        /**代理类生成组件 必须每次重新初始化*/
        ConcurrentLatchBeanFactory<T,M> beanFactory = new ConcurrentLatchBeanFactory();
        /**把入参也放入map以便调用时传入*/
        InputMapValue<List> inputMapValue = new InputMapValue<>();
        LatchThread latchThreadProxy = beanFactory.getBean(latchThread,taskname,m);
        inputMapValue.setLatchThread(latchThreadProxy);
        inputMapValue.setM(m);
        inputMap.put(taskname,inputMapValue);
    }

    private void clean() {
        inputMap.clear();
        mapResult.clear();
    }

    @Override
    public void cleanTask(String threadName) {
        inputMap.remove(threadName);
    }

    @Override
    public <T> Map<String, List<LatchThreadReturn>> excute() {
        try {
            if (inputMap == null || inputMap.size() == 0) {
                throw new ConcurrentLatchException("inputMap is null , please invoke function put");
            }
            if(mapResult != null && mapResult.size() > 0){
                throw new ConcurrentLatchException("mapResult is not null , please invoke function release");
            }
            //从线程池管理器（缓存）中获取一个excutor
            excutor = LatchExcutorBlockingQueueManager.getExcutor(inputMap.size());
            final Map<String, InputMapValue<List>> mapHandle = inputMap;
            List<Callable<LatchThreadReturn>> callables = new ArrayList<>();
            for (final String key : mapHandle.keySet()) {
                callables.add(() -> {
                    InputMapValue<List> inputMapValue = mapHandle.get(key);
                    LatchThread latchThread = inputMapValue.getLatchThread();
                    List m = inputMapValue.getM();
                    return latchThread.handle(m);
                });
            }
            List<Future<LatchThreadReturn>> results = excutor.invokeAll(callables);
            if(results == null){
                return null;
            }
            for (Future f : results) {
                if(f == null || f.get() == null){
                    continue;
                }
                LatchThreadReturn ret = (LatchThreadReturn)f.get();
                mapResult.put(ret.getKey(), ret.get());
            }
            return mapResult;
        }catch (Exception e){
            //todo 后续可以增加一个兜底策略，当报错时走备份方案
            throw new ConcurrentLatchException("ConcurrentLatchExcutorProxy excute exception",e);
        }finally {
            LatchExcutorBlockingQueueManager.takeExcutor(excutor);
        }
    }


    @Override
    public <T> List<T> get(String taskName, Class<T> clazz){
        if(mapResult.containsKey(taskName)){
            return (List<T>)mapResult.get(taskName);
        }else{
            throw new ConcurrentLatchException("unknown taskName:"+taskName);
        }
    }

    @Override
    public List<List> getAll() {
        if(mapResult != null){
            return mapResult.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
        }
        clean();
        return null;
    }

    @Override
    public void release() {
        //清空inputmap\清空mapResult
        clean();
    }


    private static class InputMapValue<M>{
        private LatchThread latchThread;
        private List m;

        public LatchThread getLatchThread() {
            return latchThread;
        }

        public void setLatchThread(LatchThread latchThread) {
            this.latchThread = latchThread;
        }

        public List<M> getM() {
            return m;
        }

        public void setM(List m) {
            this.m = m;
        }
    }
}
