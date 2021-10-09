package org.zxp.ConcurrentLatch;

/**
 * @program: ConcurrentLatch
 * @description:
 * @author: X-Pacific zhang
 * @create: 2021-01-28 13:42
 **/
public class ConcurrentLatchCfg {
    /**
     * 单个线程池最大核心线程数
     */
    private int maxCorePoolSize = 10;
    /**
     * 线程池最大数量
     */
    private int maxExcutorSize= 20;
    /**
     * 最大线程池数量倍数
     */
    private int maxPoolSizeRatio = 2;
    /**
     * 是否有界，默认无界，即不会有任务超出容量导致丢弃任务的情况
     */
    private boolean hasLimits = false;
    /**
     * 任务等待队列长度，有界时有效
     */
    private int limitsSize = 50;
    /**
     * 通过尝试后无法获得线程池资源，是否挂起等待（false抛出异常）
     */
    private boolean afterTryBlock = true;
    /**
     * 最大等待获取线程池的数量
     */
    private int dangerWaitCount = 10;


    /**
     * 线程池类型
     * 1、缓存线程
     * 2、不缓存线程 newCachedThreadPool
     */
    private int poolType = 1;


    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private int maxCorePoolSize = 10;
        private int maxExcutorSize= 20;
        private int maxPoolSizeRatio = 2;
        private boolean hasLimits = false;
        private int limitsSize = 50;
        private boolean afterTryBlock = true;
        private int dangerWaitCount = 1;

        public ConcurrentLatchCfg build(){
            return new ConcurrentLatchCfg();
        }

        public Builder maxCorePoolSize(int maxCorePoolSize){
            this.maxCorePoolSize = maxCorePoolSize;
            return this;
        }

        public Builder maxExcutorSize(int maxExcutorSize){
            this.maxExcutorSize = maxExcutorSize;
            return this;
        }

        public Builder limitsSize(int limitsSize){
            this.limitsSize = limitsSize;
            return this;
        }

        public Builder maxPoolSizeRatio(int maxPoolSizeRatio){
            this.maxPoolSizeRatio = maxPoolSizeRatio;
            return this;
        }

        public Builder hasLimits(boolean hasLimits){
            this.hasLimits = hasLimits;
            return this;
        }

        public Builder afterTryBlock(boolean afterTryBlock){
            this.afterTryBlock = afterTryBlock;
            return this;
        }

        public Builder dangerWaitCount(int dangerWaitCount){
            this.dangerWaitCount = dangerWaitCount;
            return this;
        }
    }


    public int getMaxCorePoolSize() {
        return maxCorePoolSize;
    }

    public int getMaxExcutorSize() {
        return maxExcutorSize;
    }

    public int getMaxPoolSizeRatio() {
        return maxPoolSizeRatio;
    }

    public boolean isHasLimits() {
        return hasLimits;
    }

    public int getLimitsSize() {
        return limitsSize;
    }

    public boolean isAfterTryBlock() {
        return afterTryBlock;
    }


    public int getDangerWaitCount() {
        return dangerWaitCount;
    }

    public int getPoolType() {
        return poolType;
    }
}
