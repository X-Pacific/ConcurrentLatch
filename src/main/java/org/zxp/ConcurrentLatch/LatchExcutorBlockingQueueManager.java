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
    private static volatile BlockingQueue<ExcutorHolder> freeQueue = new LinkedBlockingQueue<>(Constants.MAX_EXCUTOR_SIZE);
    //线程池以及其名字，用于记录所有线程池的名字，只第一次创建线程池才会put
    private static volatile Map<String,ExcutorHolder> excutorMap = new ConcurrentHashMap<>();
    //正在执行（未释放）线程池数量
    private static AtomicInteger inUseExcutorCount = new AtomicInteger(0);
    //当前阻塞线程池数量
    private static AtomicInteger blockCount = new AtomicInteger(0);
    //已经执行线程池数量（可能已经释放，也可能未释放）
    private static AtomicInteger alreadyExcuteCount = new AtomicInteger(0);


    private static ExecutorService poll(){
        ExcutorHolder poll = freeQueue.poll();
        if(poll == null){
            return null;
        }else{
            return poll.getExecutor();
        }
    }

    private static ExecutorService poll(long timeout, TimeUnit unit) throws InterruptedException {
        ExcutorHolder poll = freeQueue.poll(timeout,unit);
        if(poll == null){
            return null;
        }else{
            return poll.getExecutor();
        }
    }

    /**
     * 获得一个线程池
     * @param threadCount
     * @return
     * @throws InterruptedException
     */
    protected static ExcutorHolder getExcutor(int threadCount) throws InterruptedException {
        synchronized (freeQueue) {
            //先尝试获取一个空闲的线程池
            ExcutorHolder holder = freeQueue.poll();
            //没获取到可能有两种情况1、未满（包含从未创建线程池） 2、满了，线程池都在工作
            //未满，可以继续创建
            if(holder == null && excutorMap.size() < Constants.MAX_EXCUTOR_SIZE){
                //并为其命名
                String excutorName = addCount();
                ExcutorHolder excutorHolder = createOrReplaceThreadPoolExecutor(new ExcutorHolder(excutorName,null,0,0,0), threadCount);
                excutorMap.put(excutorName,excutorHolder);
                //这里一定是新建的excutor
                return excutorHolder;
            }
            //满了，不能再创建线程池，执行等待策略
            else if(holder == null && excutorMap.size() ==  Constants.MAX_EXCUTOR_SIZE){
                //自旋10次获取连接池
                for (int i = 0; i < 10; i++) {
                    ExcutorHolder middleHolder = freeQueue.poll();
                    //如果获取到线程池，则记录并返回
                    if (middleHolder != null) {
                        createOrReplaceThreadPoolExecutor(middleHolder,threadCount);
                        addCount();
                        return middleHolder;
                    }
                }
                //如果没成功则再等待50ms
                ExcutorHolder middleHolder = freeQueue.poll(50,TimeUnit.MILLISECONDS);
                if(middleHolder != null) {
                    createOrReplaceThreadPoolExecutor(middleHolder,threadCount);
                    addCount();
                    return middleHolder;
                }else {
                    //再不成功，根据配置决定挂起还是抛弃
                    if(Constants.AFTER_TRY_BLOCK) {
                        int newBlockCount = blockCount.addAndGet(1);
                        if(newBlockCount > Constants.DANGER_WAIT_COUNT){
                            throw new ConcurrentLatchException("over max wait excutor counts");
                        }
                        //等待获取excutor
                        ExcutorHolder take = freeQueue.take();
                        if(take != null) {
                            createOrReplaceThreadPoolExecutor(take,threadCount);
                            addCount();
                            return take;
                        }else{
                            throw new ConcurrentLatchException("freeQueue take error excutor is null");
                        }
                    }else{
                        throw new ConcurrentLatchException("can not poll a excutor");
                    }
                }
            }
            //直接从线程池池中获取线程池
            else{
                createOrReplaceThreadPoolExecutor(holder,threadCount);
                addCount();
                return holder;
            }
        }
    }

    /**
     * 增加计数器
     */
    private static String addCount(){
        inUseExcutorCount.addAndGet(1);
        int current  = alreadyExcuteCount.addAndGet(1);
        return "LatchExcutor_"+current;
    }

    /**
     * 创建或替换线程池,并返回线程池包装类（包含线程池对象本身以及最大可承受任务数等信息）
     * @param excutorHolder
     * @param newThreadCount
     * @return
     */
    private static ExcutorHolder createOrReplaceThreadPoolExecutor(ExcutorHolder excutorHolder,int newThreadCount){
        if(excutorHolder == null){
            throw new ConcurrentLatchException("excutorHolder is null");
        }
        if(newThreadCount < 0){
            throw new ConcurrentLatchException("newThreadCount is wrong");
        }

        //普通线程池涉及扩容线程池的问题
        if(Constants.POOL_TYPE == 1){
            //如果当前线程池不为空，则判断是否需要扩容|缩容核心线程数量
            if(excutorHolder.getExecutor() != null){
                //如果无界直接返回当前线程池，无需扩容
                if(!Constants.HAS_LIMITS){
                    return excutorHolder;
                }
                //当线程数超过最大核心线程数，则保持当前线程池中数量策略不变
                if(newThreadCount > Constants.MAX_CORE_POOL_SIZE){
                    return excutorHolder;
                }
                if(newThreadCount > excutorHolder.getInherit()){
                    //需要对线程池扩容
                    if(excutorHolder.addTopHit() >= 6){
                        return resetThreadPoolExecutor(excutorHolder,newThreadCount);
                    }
                }else if(newThreadCount < excutorHolder.getInherit()/2){
                    //需要对线程池缩容
                    if(excutorHolder.addBottomHit() >= 6){
                        return resetThreadPoolExecutor(excutorHolder,newThreadCount);
                    }
                }
            }
            //新创建线程池
            int corepoolsize = newThreadCount;
            if(newThreadCount > Constants.MAX_CORE_POOL_SIZE){
                corepoolsize = Constants.MAX_CORE_POOL_SIZE;
            }
            int inherit = Integer.MAX_VALUE;
            ExecutorService excutor = new ThreadPoolExecutor(corepoolsize, corepoolsize*Constants.MAX_POOL_SIZE_RATIO
                    , 60, TimeUnit.SECONDS, getThreadPoolExecutorBlockingQueue());
            if(Constants.HAS_LIMITS){
                inherit = corepoolsize + corepoolsize*Constants.MAX_POOL_SIZE_RATIO + Constants.LIMITS_SIZE;
            }
            excutorHolder.setExecutor(excutor);
            excutorHolder.setInherit(inherit);
            excutorHolder.setBottomHit(0);
            excutorHolder.setTopHit(0);
        }
        //newCachedThreadPool不涉及扩容线程池的问题
        else{
            if(excutorHolder.getExecutor() != null){
                return excutorHolder;
            }
            excutorHolder.setExecutor(Executors.newCachedThreadPool());
            excutorHolder.setInherit(Integer.MAX_VALUE);
            excutorHolder.setBottomHit(0);
            excutorHolder.setTopHit(0);
        }
        return excutorHolder;
    }


    /**
     * 重建线程池
     */
    private static ExcutorHolder resetThreadPoolExecutor(ExcutorHolder excutorHolder,int newThreadCount){
        if(excutorHolder.getExecutor() != null){
            //销毁原有线程池
            excutorHolder.getExecutor() .shutdown();
        }
        //新建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(newThreadCount, newThreadCount * Constants.MAX_POOL_SIZE_RATIO
                , 60, TimeUnit.SECONDS, getThreadPoolExecutorBlockingQueue());
        int inherit = newThreadCount + newThreadCount*Constants.MAX_POOL_SIZE_RATIO + Constants.LIMITS_SIZE;
        //重置监控数据
        excutorHolder.reset(inherit);
        excutorHolder.setExecutor(executor);
        return excutorHolder;
    }

    private static BlockingQueue<Runnable> getThreadPoolExecutorBlockingQueue(){
        BlockingQueue<Runnable> queue = null;
        if(Constants.HAS_LIMITS){
            queue = new LinkedBlockingQueue<Runnable>(Constants.LIMITS_SIZE);
        }else {
            queue = new LinkedBlockingQueue<Runnable>();
        }
        return queue;
    }

    /**
     * 线程池执行完成归还连接池
     * @param holder
     */
    protected static synchronized void takeExcutor(ExcutorHolder holder) {
        boolean offer = freeQueue.offer(holder);
        if (!offer) {//异常情况
            holder.getExecutor().shutdown();
            //help GC
            holder.setExecutor(null);
            logger.warn("freeQueue offer error freeQueue size is" + freeQueue.size());
        }
        inUseExcutorCount.addAndGet(-1);
    }



    protected static void print(){
        String br = "\n";
        StringBuffer sb = new StringBuffer();
        sb.append("===================ConcurrentLatch Running Info===================").append(br);
        sb.append("Excutor Map Info : ").append(br);
        sb.append("ExcutorMap size:"+LatchExcutorBlockingQueueManager.excutorMap.size()).append(br);
        for (Map.Entry<String, ExcutorHolder> entry : LatchExcutorBlockingQueueManager.excutorMap.entrySet()){
            sb.append("excutorName : " + entry.getKey() + " excutor : " + entry.getValue()).append(br);
        }
        sb.append("【当前空闲线程池】freeQueue size : " + freeQueue.size()).append(br);
        sb.append("【已经运行线程池】alreadyExcuteCount size : " + alreadyExcuteCount.get()).append(br);
        sb.append("【正在运行线程池】inUseExcutorCount size : " + inUseExcutorCount.get()).append(br);
        sb.append("【排队获取线程池】blockCount size : " + blockCount.get()).append(br);
        sb.append("===================ConcurrentLatch Running Info===================");
        System.out.println(sb.toString());
        logger.info(sb.toString());
    }



    protected static class ExcutorHolder{
        //线程池名称
        private String executorName;
        //线程池
        private ExecutorService executor;
        //当前线程池最大可容纳线程数
        private int inherit;
        //触碰最大可容纳线程数的次数
        private int topHit;
        //触碰最大可容纳线程数/2的次数
        private int bottomHit;


        public ExcutorHolder(String executorName, ExecutorService executor, int inherit, int topHit, int bottomHit) {
            this.executorName = executorName;
            this.executor = executor;
            this.inherit = inherit;
            this.topHit = topHit;
            this.bottomHit = bottomHit;
        }

        public int getInherit() {
            return inherit;
        }

        public int addTopHit() {
            return ++topHit;
        }

        public int addBottomHit() {
            return ++bottomHit;
        }

        public void setInherit(int inherit) {
            this.inherit = inherit;
        }


        public ExecutorService getExecutor() {
            return executor;
        }

        public void setExecutor(ExecutorService executor) {
            this.executor = executor;
        }

        public int getTopHit() {
            return topHit;
        }

        public void setTopHit(int topHit) {
            this.topHit = topHit;
        }

        public int getBottomHit() {
            return bottomHit;
        }

        public void setBottomHit(int bottomHit) {
            this.bottomHit = bottomHit;
        }

        public void reset(int newInherit) {
            this.inherit = newInherit;
            this.bottomHit = 0;
            this.topHit = 0;
        }

        public String getExecutorName() {
            return executorName;
        }

        public void setExecutorName(String executorName) {
            this.executorName = executorName;
        }


        @Override
        public String toString() {
            return "ExcutorHolder{" +
                    "executorName='" + executorName + '\'' +
                    ", executor=" + executor +
                    ", inherit=" + inherit +
                    ", topHit=" + topHit +
                    ", bottomHit=" + bottomHit +
                    '}';
        }
    }
}
