[toc]

# ConcurrentLatch
ConcurrentLatch是一个基于JDK的多线程归类并发处理闩工具(基于JDK1.6)

## ConcurrentLatch使用场景

当你有5个无相关性操作，顺序执行那么消耗时间合将达到5个操作的所有操作时间和，如果开启多线程，那么又不能保证这5个操作都进行完毕才能进行后续操作，那么基于刚才说的场景ConcurrentLatch就是用来解决这个问题的，考虑到系统不能无限制的增加线程，所以ConcurrentLatch又增加了线程池管理器的概念，防止系统因为线程开启过多而宕机
## 为什么使用它
1. 与CountDownLatch不同的是它可以方便的获取哪个任务对应的哪个返回内容，比如执行了ABC三个任务，执行完成后可以通过任务名称A,B或者是C获得他们对应的返回结果，当并行的任务较多时这是个实用的功能
1. 与Future（或者FutureTask）不同的是，他可以通过一系列策略管理多个线程池，防止线程池创建过多而爆满，抑或线程过多导致系统宕机
1. 当然还有设计它的初衷，为了简化开发，你不需要触碰任何jdk底层的多线程工具类就可以很好的实现并发闩的效果，并且它很好的做到了业务代码分离

## git地址

> https://gitee.com/zxporz/ConcurrentLatch.git


## 更新说明

### 2017-11-25 [1.1-SNAPSHOT]
1. 本次优化修复了无返回参数还必须写一个返回类的问题
1. 新增了一种代理模式，代替在返回类中定死任务名称的实现方式（不在依赖注解），可以将同一个的业务组件以不同任务来运行（感谢刘仁豪同学提的建议以及思路）
1. 调整部分API，但不影响原有API的调用。新增了ConcurrentLatch工厂，默认获取代理方式的ConcurrentLatch实例

### 2019-06-12[1.2-SNAPSHOT]
1. 添加了若干注释
1. 重写了线程池管理机制，原有机制是每次使用完线程池会把线程池销毁，当再次使用并发闩时需要重建线程池，这是个糟糕的实现，因为他没有很好的利用线程池节省资源的思想，本次调整后，线程池可以缓存，使用完毕的线程池可以“归还”给一个闲置的线程池列表
1. 添加了若干配置，优化了线程池创建的方式，并在本文档中详细说明配置功能以及如何根据实际业务场景以及硬件情况定制线程池配置
1. 本次修改未涉及API的改动


## 使用ConcurrentLatch

ConcurrentLatch不依赖任何三方jar包，如果您使用的是Maven，那么把ConcurrentLatch安装到中央仓库后，在pom文件中添加以下依赖

```
<dependency>
    <groupId>org.zxp</groupId>
    <artifactId>concurrentLatch</artifactId>
    <version>1.2-SNAPSHOT</version>
</dependency>
```


### 业务实现类定制
您需要做的只是实现LatchThread接口，并将业务代码写入handle方法，如果有需要传入的对象或信息，可以通过构造方法传参的方式传入
```
public class RuleLatch implements  LatchThread {
    RuleDto dto = null;
    /**通过构造方法传递入参*/
    public RuleLatch(RuleDto args){
        dto = args;
    }
    /**你的业务处理*/
    public RuleDto handle() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dto.setMmmm(dto.getMmmm()+ 9999);
        return dto;
    }
}
```

还可以通过下面注解（或成员变量）的方式来标识业务的名称，但我们并不建议您这样做，因为这样会导致这个业务类不能以多个不同名称的任务来运行
```
/**添加这个类运行的任务名，注意这个名字在一次线程池运行中不可重复*/
/**也可以不配置注解，写一个名为TASKNAME的字符串成员变量也可行*/
@LatchTaskName("rule")
public class RuleLatch implements  LatchThread {
    RuleDto dto = null;
    /**通过构造方法传递入参*/
    public RuleLatch(RuleDto args){
        dto = args;
    }
    /**你的业务处理*/
    public RuleDto handle() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dto.setMmmm(dto.getMmmm()+ 9999);
        return dto;
    }
}
/**添加这个类运行的任务名，注意这个名字在一次线程池运行中不可重复*/
/**也可以不配置注解，写一个名为TASKNAME的字符串成员变量也可行*/
/**返回对象配置的注解值必须和对应的LatchThread实现类一致，否则无法获取返回值对应关系*/
@LatchTaskName("rule")
public class RuleDto {
……
}
```
### 编写调用

```
/**创建一个ConcurrentLatch执行器*/
ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
/**也可以通过其他静态方法获取其他的ConcurrentLatch*/
//ConcurrentLatch excutorproxy = ConcurrentLatchExcutorFactory.getConcurrentLatch(ConcurrentLatchExcutorFactory.ExcutorType.PROXY);
/**组织需要并发的业(任)务(务)类对象*/
LatchThread platformLatchThread = new PlatformLatch();//业务类A
RuleDto ruleDto = new RuleDto();
ruleDto.setRuleID("zxp123");
ruleDto.setMmmm(0.00001);
LatchThread ruleLatchThread = new RuleLatch(ruleDto);//业务类B
/**将任务推入ConcurrentLatch执行器*/
excutor.put(platformLatchThread,"AAA");
excutor.put(ruleLatchThread,"BBB");
/**可以把一个之前已经有的业务组件再放入池中，只需要名称不重复即可，当然你可以可以重新new一个*/
excutor.put(platformLatchThread,"CCC");
/**通知ConcurrentLatch执行器开始执行所有推入的任务*/
/**这里主线程或调用线程会挂起，直到所有任务都执行完毕*/
Map<String, Object> map = excutor.excute();
/**获取返回值*/
/**这里的key就是上面在注解(LatchTaskName)或者TASKNAME配置的值，能轻松的获取所有的返回对象*/
for (String key : map.keySet()) {
    Object out = map.get(key);
}
```
注解方式标注任务名称的调用稍有不同

```
/**创建一个ConcurrentLatch执行器，不同点*/
ConcurrentLatch excutor =
ConcurrentLatchExcutorFactory.getConcurrentLatch(ConcurrentLatchExcutorFactory.ExcutorType.NORMAL);
/**组织需要并发的业(任)务(务)类对象*/
LatchThread platformLatchThread = new PlatformLatch();//业务类A
RuleDto ruleDto = new RuleDto();
ruleDto.setRuleID("zxp123");
ruleDto.setMmmm(0.00001);
LatchThread ruleLatchThread = new RuleLatch(ruleDto);//业务类B
/**将任务推入ConcurrentLatch执行器，不同点是这里只有一个入参*/
excutor.put(platformLatchThread);
excutor.put(ruleLatchThread);
/**不可以把一个之前已经有的业务组件再放入池中，即使你重新new了一个实例也不允许，这也是为什么我们不推荐使用这种方式的原因*/
/**通知ConcurrentLatch执行器开始执行所有推入的任务*/
/**这里主线程或调用线程会挂起，直到所有任务都执行完毕*/
Map<String, Object> map = excutor.excute();
/**获取返回值*/
/**这里的key就是上面在注解(LatchTaskName)或者TASKNAME配置的值，能轻松的获取所有的返回对象*/
/**再一次强调，必须把handle方法返回的类配置与对应的LatchThread实现类一致的注解任务名称，否则无法正常获取对应关系*/
for (String key : map.keySet()) {
    Object out = map.get(key);
}
```
执行结果
```
我是BBB
我是AAA
我是AAA
taskname==========aaa
PlatformDto{name='0516', premium=6500.98, policyNo='000000000001'}
taskname==========ccc
PlatformDto{name='0516', premium=6500.98, policyNo='000000000001'}
taskname==========bbb
RuleDto{ruleID='zxp123', mmmm=9999.00001}
```
### 打印线程池情况快照

```
LatchExcutorBlockingQueueManager.print();
```


## 配置说明

配置详见org.zxp.ConcurrentLatch.Constants

调用详见org.zxp.ConcurrentLatch.LatchExcutorBlockingQueueManager#getExcutor
### MAX_CORE_POOL_SIZE
```
/**
 * 单个线程池最大核心线程数
 */
public static final int MAX_CORE_POOL_SIZE = 10;
```
线程池的核心线程数，如果不明线程池如何使用请参考我总结的一片线程池的使用文档：[线程池使用说明](https://gitee.com/zxporz/zxp-thread-test/blob/master/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%94%A8%E6%B3%95.pdf)

当然您可以结合您的业务情况以及硬件情况单独定制线程池的类型（我把线程池从上个版本的newFixedThreadPool调整为现在的普通线程池）

```
<!--详见org.zxp.ConcurrentLatch.LatchExcutorBlockingQueueManager#getExcutor-->
excutor = new ThreadPoolExecutor(corepoolsize, threadCount*Constants.MAX_POOL_SIZE_RATIO , 60, TimeUnit.SECONDS, queue);
```
### MAX_POOL_SIZE_RATIO
```
/**
 * 最大线程池数量倍数
 */
public static final int MAX_POOL_SIZE_RATIO = 2;
```

线程池的最大线程数倍数（=核心线程数*MAX_POOL_SIZE_RATIO），如果不明线程池如何使用请参考我总结的一片线程池的使用文档：[线程池使用说明](https://gitee.com/zxporz/zxp-thread-test/blob/master/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%94%A8%E6%B3%95.pdf)


### HAS_LIMITS
```
/**
 * 是否有界，默认无界，即不会有任务超出容量导致丢弃任务的情况
 */
public static final boolean HAS_LIMITS = false;
```

线程池任务等待队列是否无界，如果有界（配置为false）则下面LIMITS_SIZE配置有效，如果不明线程池如何使用请参考我总结的一片线程池的使用文档：[线程池使用说明](https://gitee.com/zxporz/zxp-thread-test/blob/master/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%94%A8%E6%B3%95.pdf)

### LIMITS_SIZE
```
/**
 * 任务等待队列长度，有界时有效
 */
public static final int LIMITS_SIZE = 50;
```
线程池任务等待队列长度，如果不明线程池如何使用请参考我总结的一片线程池的使用文档：[线程池使用说明](https://gitee.com/zxporz/zxp-thread-test/blob/master/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%94%A8%E6%B3%95.pdf)

### MAX_EXCUTOR_SIZE
```
/**
 * 线程池最大数量
 */
public static final int MAX_EXCUTOR_SIZE= 20;
```
在线程池管理器中维护的可用线程池数量，默认配置为20个，未到达20个线程池时会继续创建，直到到达20个线程

当新任务发现没有可用线程池并且线程池已经创建满20个后会有一定获取策略：

1、尝试10次获取线程池

2、再持续等待50ms获取线程池

3、如果仍无法获得线程池资源执行失败策略（详见下面AFTER_TRY_BLOCK配置）


### AFTER_TRY_BLOCK
```
/**
 * 通过尝试后无法获得线程池资源，是否挂起等待（false抛出异常）
 */
public static final boolean AFTER_TRY_BLOCK = false;
```
当无法获取线程池资源时执行的失败策略

true为继续等待（挂起直到有线程池资源的释放）

false为直接抛出异常，丢弃当前任务

## 关键逻辑说明

1. ConcurrentLatchExcutor中调用了线程池管理器来获取线程池
1. 通过Future获取线程执行获得返回对象
1. LatchExcutorBlockingQueueManager线程池管理器中通过阻塞队列来监控线程池的使用情况，线程池使用完成后不销毁，而是归还可用线程池队列，当可用线程池队列为空则无法获取线程池并执行相关失败策略，
1. 代理方式put任务时，内部会将任务返回对象包装为map以绑定任务名称（JDK）[2017-11-25]
1. 相关测试代码详见org.zxp.ConcurrentLatch.demo.TestProxy#main