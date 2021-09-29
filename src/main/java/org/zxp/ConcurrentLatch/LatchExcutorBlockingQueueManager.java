package org.zxp.ConcurrentLatch;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过阻塞队列实现的线程池管理器
 */
class LatchExcutorBlockingQueueManager {
    private static Logger logger = LoggerFactory.getLogger(LatchExcutorBlockingQueueManager.class);

    //无任务执行的线程池，用于控制归还和复用线程池
    private static volatile BlockingQueue<ExecutorService> freeQueue = new LinkedBlockingQueue<ExecutorService>(Constants.MAX_EXCUTOR_SIZE);
    //线程池以及其名字，用于记录所有线程池的名字，只第一次创建线程池才会put
    private static volatile Map<Integer,ExecutorService> excutorNameMap = new ConcurrentHashMap<Integer, ExecutorService>();
    //正在执行（未释放）线程池数量
    private static AtomicInteger inUseExcutorCount = new AtomicInteger(0);
    //当前阻塞线程池数量
    private static AtomicInteger blockCount = new AtomicInteger(0);
    //已经执行线程池数量（可能已经释放，也可能未释放）
    private static AtomicInteger alreadyExcuteCount = new AtomicInteger(0);


    /**
     * 获得一个线程池
     * @param threadCount
     * @return
     * @throws InterruptedException
     */
    protected static ExecutorService getExcutor(int threadCount) throws InterruptedException {
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
                inUseExcutorCount.addAndGet(1);
                int current = alreadyExcuteCount.addAndGet(1);
                //并为其命名
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
                        int newBlockCount = blockCount.addAndGet(1);
                        if(newBlockCount > Constants.DANGER_WAIT_COUNT){
                            throw new ConcurrentLatchException("over max wait excutor counts");
                        }
                        //等待获取excutor
                        excutor = freeQueue.take();
                        if(excutor != null) {
                            return excutor;
                        }else{
                            throw new ConcurrentLatchException("freeQueue take error excutor is null");
                        }
                    }else{
                        throw new ConcurrentLatchException("can not poll a excutor");
                    }
                }
            }else{
                inUseExcutorCount.addAndGet(1);
                int current = alreadyExcuteCount.addAndGet(1);
                return excutor;
            }
        }
    }

    /**
     * 线程池执行完成归还连接池
     * @param excutor
     * @throws InterruptedException
     */
    protected static synchronized void takeExcutor(ExecutorService excutor) {
        boolean offer = freeQueue.offer(excutor);
        if (!offer) {//异常情况
            excutor.shutdown();
            //help GC
            excutor = null;
            logger.warn("freeQueue offer error freeQueue size is" + freeQueue.size());
        }
        inUseExcutorCount.addAndGet(-1);
        if(alreadyExcuteCount.get() > 0){
            alreadyExcuteCount.addAndGet(-1);
        }
    }



    protected static void print(){
        String br = "\n";
        StringBuffer sb = new StringBuffer();
        sb.append("===================ConcurrentLatch Running Info===================").append(br);
        sb.append("Excutor Map Info : ").append(br);
        sb.append("Map size:"+LatchExcutorBlockingQueueManager.excutorNameMap.size());
        for (Map.Entry<Integer, ExecutorService> entry : LatchExcutorBlockingQueueManager.excutorNameMap.entrySet()){
            sb.append("excutorname : " + entry.getKey() + " excutor : " + entry.getValue()).append(br);
        }
        sb.append("【当前空闲线程池】freeQueue size : " + freeQueue.size()).append(br);
        sb.append("【已经运行线程池】alreadyExcuteCount size : " + alreadyExcuteCount.get()).append(br);
        sb.append("【正在运行线程池】inUseExcutorCount size : " + inUseExcutorCount.get()).append(br);
        sb.append("【排队获取线程池】blockCount size : " + blockCount.get()).append(br);
        sb.append("===================ConcurrentLatch Running Info===================");
    }
}
