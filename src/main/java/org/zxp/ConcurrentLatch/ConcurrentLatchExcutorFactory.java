package org.zxp.ConcurrentLatch;

/**
 * 组件获取工厂
 */
public class ConcurrentLatchExcutorFactory {
    /**
     *获取默认组件（代理方式）
     * @return
     */
    public static ConcurrentLatch getConcurrentLatch(){
        return new ConcurrentLatchExcutorProxy();
    }

    /**
     * 获取默认组件（默认方式）
     * @param e
     * @return
     */
    public static ConcurrentLatch getConcurrentLatch(ExcutorType e){
        switch (e){
            case PROXY:
                return getConcurrentLatch();
            case NORMAL:
                return new ConcurrentLatchExcutor();
        }
        return null;
    }

    public enum ExcutorType{
        PROXY,NORMAL
    }
}
