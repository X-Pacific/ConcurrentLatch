package org.zxp.ConcurrentLatch;


/**
 * 组件获取工厂
 */
public class ConcurrentLatchExcutorFactory {
    /**
     * 是否初始化，当初始化标识为true后，init方法不能再调用
     */
    private static boolean initFlag = false;

    /**
     * 初始化参数，只能调用一次
     * @param concurrentLatchCfg
     */
    public static synchronized void init(ConcurrentLatchCfg concurrentLatchCfg){
        if(initFlag){
            throw new IllegalArgumentException("ConcurrentLatch配置已经初始化");
        }
        Constants.AFTER_TRY_BLOCK = concurrentLatchCfg.isAfterTryBlock();
        Constants.HAS_LIMITS = concurrentLatchCfg.isHasLimits();
        Constants.LIMITS_SIZE = concurrentLatchCfg.getLimitsSize();
        Constants.MAX_CORE_POOL_SIZE = concurrentLatchCfg.getMaxCorePoolSize();
        Constants.MAX_EXCUTOR_SIZE = concurrentLatchCfg.getMaxExcutorSize();
        Constants.MAX_POOL_SIZE_RATIO = concurrentLatchCfg.getMaxPoolSizeRatio();
        Constants.DANGER_WAIT_COUNT = concurrentLatchCfg.getDangerWaitCount();
        initFlag = true;
    }
    /**
     *获取默认组件（代理方式）
     * @return
     */
    public static ConcurrentLatch getConcurrentLatch(){
        initFlag = true;
        return new ConcurrentLatchExcutorProxy();
    }

    /**
     * 打印当前快照信息
     */
    public static void print(){
        LatchExcutorBlockingQueueManager.print();
    }
}
