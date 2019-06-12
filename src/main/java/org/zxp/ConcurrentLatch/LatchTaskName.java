package org.zxp.ConcurrentLatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LatchTaskName {
    /**
     * 对应非代理方式绑定任务名称与业务处理类，但是这种方式有限制，即相同的业务类无法设置不同的任务名称
     * @return
     */
    public String value();
}
