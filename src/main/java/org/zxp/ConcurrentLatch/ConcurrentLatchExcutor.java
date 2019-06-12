package org.zxp.ConcurrentLatch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * 通过注解方式设置任务key
 */
public class ConcurrentLatchExcutor implements ConcurrentLatch {
    /** 线程池 不能是单例，否则有事物问题*/
    private ExecutorService excutor = null;
    private SimpleDateFormat sdf = null;
    /**入参的map*/
    private Map<String,LatchThread> inputMap = null;
    /**出参的mao*/
    private Map<String ,Object> mapResult = new HashMap<String,Object>();
    public ConcurrentLatchExcutor(){
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputMap = new HashMap<String,LatchThread>();
    }

    /**
     * 插入一个任务
     * @param latchThread
     * @throws Exception
     */
    @Override
    public void put(LatchThread latchThread) throws Exception {
        String taskname = getTaskName(latchThread);
        if(inputMap.containsKey(taskname)){
            throw new Exception("不能添加重复的任务");
        }
        inputMap.put(taskname,latchThread);
    }

    @Override
    public void put(LatchThread latchThread, String taskName) throws Exception {
        throw new Exception("ConcurrentLatchExcutor不支持当前put方法");
    }


    /**
     * 清除所有任务
     */
    @Override
    public void clean(){
        inputMap.clear();
    }
    /**
     * 清除指定任务
     */
    @Override
    public void clean(String threadName){
        inputMap.remove(threadName);
    }


    /**
     * 执行线程任务
     * @return
     * @throws Exception
     */
    @Override
    public Map<String ,Object> excute() throws Exception {
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
            for (Future<Object> f : results) {
                String serviceName = getTaskNameByObj(f.get());
                if(serviceName == null){
                    continue;
                }
                mapResult.put(serviceName, f.get());
            }
            return mapResult;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            LatchExcutorBlockingQueueManager.takeExcutor(excutor);
        }
    }

    /**
     * 获得taskname（多种方式）
     * @param latchThread
     * @return
     */
    private String getTaskName(LatchThread latchThread) throws Exception {
        if(latchThread == null){
            throw new Exception("LatchThread不能为空");
        }
        /**先获取线程执行内容类中的任务名*/
        String SERVICETASKNAME = getTaskNameByObj(latchThread);
        /**再获取返回对象的任务名*/
        String RETURNTASKNAME = getTaskNameByReturnMethod(latchThread);
        if(RETURNTASKNAME != null && !RETURNTASKNAME.equals("")
                && SERVICETASKNAME != null && !SERVICETASKNAME.equals("")
                && SERVICETASKNAME.equals(RETURNTASKNAME)){
            return SERVICETASKNAME;
        }else{
//            throw new Exception("请检查任务名称设置");
            //如果没有设置注解或者没有返回类，则直接返回一个随机值
            return UUID.randomUUID().toString();
        }
    }
    /**
     * 通过返回对象获得taskname（多种方式）
     * @param latchThread
     * @return
     */
    private String getTaskNameByReturnMethod(LatchThread latchThread) throws Exception {
        String RETURNTASKNAME = null;
        Method handleMethod = null;
        try {
            handleMethod = latchThread.getClass().getMethod("handle", null);
        }catch (Exception e){}
        try {
            LatchTaskName latchTaskName = handleMethod.getReturnType().getAnnotation(LatchTaskName.class);
            if(latchTaskName != null){
                RETURNTASKNAME = latchTaskName.value();
            }
        }catch (Exception e){ }
        try {
            if(RETURNTASKNAME ==null) {
                Object retObj = handleMethod.getReturnType().newInstance();
                Field taskNameField = retObj.getClass().getField("TASKNAME");
                RETURNTASKNAME = taskNameField.get(latchThread).toString();
            }
        }catch (Exception e){ }
        return RETURNTASKNAME;
    }
    /**
     * 通过返回对象获得taskname（多种方式）
     * @param obj
     * @return
     */
    private String getTaskNameByObj(Object obj) throws Exception {
        String RETURNTASKNAME = null;
        try {
            LatchTaskName latchTaskName = obj.getClass().getAnnotation(LatchTaskName.class);
            if(latchTaskName != null){
                RETURNTASKNAME = latchTaskName.value();
            }
        }catch (Exception e){ }
        try {
            if(RETURNTASKNAME ==null) {
                Field taskNameField = obj.getClass().getField("TASKNAME");
                RETURNTASKNAME = taskNameField.get(obj).toString();
            }
        }catch (Exception e){ }
        return RETURNTASKNAME;
    }
}
