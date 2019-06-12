package org.zxp.ConcurrentLatch;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 通过阻塞队列实现的线程池管理器
 */
public class LatchExcutorBlockingQueueManager {

    //无任务执行的线程池
    public static volatile BlockingQueue<ExecutorService> freeQueue = new LinkedBlockingQueue<ExecutorService>(Constants.MAX_EXCUTOR_SIZE);
    //线程池以及其名字
    public static volatile Map<Integer,ExecutorService> excutorNameMap = new ConcurrentHashMap<Integer, ExecutorService>();
    //当前最大线程池标号
    public static int incr = 0;

    /**
     * 获得一个线程池
     * @param threadCount
     * @return
     * @throws InterruptedException
     */
    public static ExecutorService getExcutor(int threadCount) throws InterruptedException {
        synchronized (freeQueue) {
            //先尝试获取一个空闲的线程池
            ExecutorService excutor = freeQueue.poll();
            //没获取到可能有两种情况1、未满（包含从未创建线程池） 2、满了，线程池都在工作
            //未满，可以继续创建
            if(excutor == null && excutorNameMap.size() < Constants.MAX_EXCUTOR_SIZE){
                int corepoolsize = threadCount;
                if(threadCount > Constants.MAX_CORE_POOL_SIZE){
                    corepoolsize = Constants.MAX_CORE_POOL_SIZE;
                }
                BlockingQueue<Runnable> queue = null;
                if(Constants.HAS_LIMITS){
                    queue = new LinkedBlockingQueue<Runnable>(Constants.LIMITS_SIZE);
                }else {
                    queue = new LinkedBlockingQueue<Runnable>();
                }
                excutor = new ThreadPoolExecutor(corepoolsize, threadCount*Constants.MAX_POOL_SIZE_RATIO
                        , 60, TimeUnit.SECONDS, queue);
                //并为其命名
                int current = ++incr;
                excutorNameMap.put(current,excutor);
                return excutor;
            }
            //满了，不能再创建线程池，执行等待策略
            else if(excutor == null && excutorNameMap.size() ==  Constants.MAX_EXCUTOR_SIZE){
                //自旋10次获取连接池
                for (int i = 0; i < 10; i++) {
                    excutor = freeQueue.poll();
                    //如果获取到线程池，则记录并返回
                    if (excutor != null) {
                        return excutor;
                    }
                }
                //如果没成功则再等待50ms
                excutor = freeQueue.poll(50,TimeUnit.MILLISECONDS);
                if(excutor != null) {
                    return excutor;
                }else {
                    //再不成功，根据配置决定挂起还是抛弃
                    if(Constants.AFTER_TRY_BLOCK) {
                        //等待获取excutor
                        excutor = freeQueue.take();
                        if(excutor != null) {
                            return excutor;
                        }else{
                            throw new InterruptedException("freeQueue take error excutor is null");
                        }
                    }else{
                        throw new InterruptedException("can not poll a excutor");
                    }
                }
            }else{
                return excutor;
            }
        }
    }

    /**
     * 线程池执行完成归还连接池
     * @param excutor
     * @throws InterruptedException
     */
    public static void takeExcutor(ExecutorService excutor) throws InterruptedException {
        boolean offer = freeQueue.offer(excutor);
        if (!offer) {//异常情况
            excutor.shutdown();
            excutor = null;
            throw new InterruptedException("freeQueue offer error freeQueue size is" + freeQueue.size());
        }
    }

    /**
     * 打印当前快照信息
     */
    public static void print(){
        System.out.println("===================SNAPSHOT===================");
        System.out.println("Map Info:");
        System.out.println("Map size:"+excutorNameMap.size());
        for (Map.Entry<Integer,ExecutorService> entry : excutorNameMap.entrySet()){
            System.out.println("excutorname : " + entry.getKey() + " excutor : " + entry.getValue());
        }
        System.out.println("freeQueue size  : " + freeQueue.size());
        System.out.println("incr : " + incr);
        System.out.println("===================SNAPSHOT===================");
    }
}
