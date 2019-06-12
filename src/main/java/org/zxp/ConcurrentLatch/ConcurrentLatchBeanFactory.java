package org.zxp.ConcurrentLatch;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * 代理对象生成类
 */
public class ConcurrentLatchBeanFactory implements InvocationHandler{
    private Object target;
    private String key = "";

    /**
     * 绑定委托对象并返回一个代理类
     * @param target 目标类型
     * @param pKey 任务代号
     * @return
     */
    public LatchThread getBean(Object target,String pKey) throws Exception {
        this.key = pKey;
        this.target = target;
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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        result = method.invoke(target, null);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put(this.key,result);
        return map;
    }
}
