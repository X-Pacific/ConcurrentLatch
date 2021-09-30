package org.zxp.ConcurrentLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理对象生成类
 */
class ConcurrentLatchBeanFactory<T,M> implements InvocationHandler{
    private static Logger logger = LoggerFactory.getLogger(ConcurrentLatchBeanFactory.class);
    private Object target;
    private String key = "";
    private List<T> in = null;

    /**
     * 绑定委托对象并返回一个代理类
     * @param target 目标类型
     * @param pKey 任务代号
     * @return
     */
    public LatchThread getBean(Object target,String pKey,List<T> in)  {
        this.key = pKey;
        this.target = target;
        this.in = in;
        LatchThread proxyInstance = (LatchThread)Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), this);
        //取得代理对象
        return proxyInstance;
    }

    /**
     * 代理层将返回结果封装在map中以标识任务名称
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public LatchThreadReturn<M> invoke(Object proxy, Method method, Object[] args) {
        LatchThreadReturn result = null;
        try {
            result = (LatchThreadReturn)method.invoke(target, in);
        } catch (Exception e) {
            logger.error("reflect invoke exception",e);
            throw new ConcurrentLatchException("reflect invoke exception",e);
        }
        result.setKey(key);
        return result;
    }
}
