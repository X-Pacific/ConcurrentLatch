package org.zxp.ConcurrentLatch;

public class Constants {
    /**
     * 单个线程池最大核心线程数
     */
    public static final int MAX_CORE_POOL_SIZE = 10;
    /**
     * 线程池最大数量
     */
    public static final int MAX_EXCUTOR_SIZE= 20;
    /**
     * 最大线程池数量倍数
     */
    public static final int MAX_POOL_SIZE_RATIO = 2;
    /**
     * 是否有界，默认无界，即不会有任务超出容量导致丢弃任务的情况
     */
    public static final boolean HAS_LIMITS = false;
    /**
     * 任务等待队列长度，有界时有效
     */
    public static final int LIMITS_SIZE = 50;

    /**
     * 通过尝试后无法获得线程池资源，是否挂起等待（false抛出异常）
     */
    public static final boolean AFTER_TRY_BLOCK = false;

}
