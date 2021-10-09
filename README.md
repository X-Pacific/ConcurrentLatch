[toc]

# ConcurrentLatch
ConcurrentLatch是一个基于JDK的多线程归类并发处理闩工具(基于JDK1.8)

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

### 2021-01-28[2.0-SNAPSHOT]

   1. 添加了参数线程、线程池策略配置的功能
   1. 重构了调用出入参的实现，去掉原有通过构造方法入参的方式，增加通过put方法入参的方式
   1. 增加了泛型的支持
   1. 增加了获取返回结果方法的支持
   2. 优化了异常抛出的方式
   3. 移除了原有通过注解标识任务名称的方式

### 2021-10-09[3.0-SNAPSHOT]

1. 统一调整出入参数为list，并且定义了出参的通用类
1. 增加了一些对于已经put任务后对于任务处理的api
1. 增加了一个单机mapreduce的封装类，简化并行代码量

## 使用ConcurrentLatch
在pom文件中添加以下依赖

```
<dependency>
    <groupId>org.zxp</groupId>
    <artifactId>concurrentLatch</artifactId>
    <version>3.0-SNAPSHOT</version>
</dependency>
```


### 业务实现类定制
您需要做的只是实现LatchThread接口，并将业务代码写入handle方法
有入参的例子
```
public class RuleLatch implements LatchThread<RuleQo,RuleDto> {
    @Override
    public LatchThreadReturn<RuleDto> handle(List<RuleQo> ruleQo) {
        System.out.println("我是RuleLatch");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<RuleDto> ls = new ArrayList<>();
        ruleQo.forEach(s -> {
            RuleDto dto = new RuleDto();
            dto.setRuleID(s.getRuleID()+"已处理");
            dto.setMmmm(dto.getMmmm()+ 9999);
            ls.add(dto);
        });
        return LatchThreadReturn.set(ls);
    }
}
```
```
public class RuleQo {
    private String ruleID = "";

    public String getRuleID() {
        return ruleID;
    }

    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }
}
```
```
public class RuleDto {
    public String getRuleID() {
        return ruleID;
    }

    @Override
    public String toString() {
        return "RuleDto{" +
                "ruleID='" + ruleID + '\'' +
                ", mmmm=" + mmmm +
                '}';
    }
    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public double getMmmm() {
        return mmmm;
    }

    public void setMmmm(double mmmm) {
        this.mmmm = mmmm;
    }

    private String ruleID = "";
    private double mmmm;
}
```
无入参的例子
```
public class PlatformLatch implements LatchThread<Void ,PlatformDto> {
    @Override
    public LatchThreadReturn<PlatformDto> handle(List<Void> v) {
        System.out.println("我是PlatformLatch");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PlatformDto dto = new PlatformDto();
        dto.setName("0516");
        dto.setPremium(6500.98);
        dto.setPolicyNo("000000000001");
        return LatchThreadReturn.set(dto);
    }
}
```
```
public class PlatformDto {
    private String name = "";

    public String getName() {
        return name;
    }

        @Override
        public String toString() {
            return "PlatformDto{" +
                    "name='" + name + '\'' +
                    ", premium=" + premium +
                    ", policyNo='" + policyNo + '\'' +
                    '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPremium() {
        return premium;
    }

    public void setPremium(double premium) {
        this.premium = premium;
    }

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    private double premium;
    private String policyNo = "";
}
```
### Quick Start 编写调用

```
//配置ConcurrentLatch（全局只能配置一次）
ConcurrentLatchExcutorFactory.init(
        ConcurrentLatchCfg.builder()
            .maxCorePoolSize(10)
            .maxExcutorSize(10)
            .maxPoolSizeRatio(2).build()
);
//获取一个ConcurrentLatch执行器
ConcurrentLatch excutor = ConcurrentLatchExcutorFactory.getConcurrentLatch();
//声明RuleLatch
RuleQo ruleQo = new RuleQo();
ruleQo.setRuleID("zxp1");
RuleQo ruleQo2 = new RuleQo();
ruleQo2.setRuleID("zxp2");
RuleQo ruleQo3 = new RuleQo();
ruleQo3.setRuleID("zxp3");
LatchThread ruleLatchThread = new RuleLatch();
//声明PlatformLatch
LatchThread platformLatchThread = new PlatformLatch();
//将LatchThread（任务）置入ConcurrentLatch框架
excutor.put(platformLatchThread,"platformLatch",null);
excutor.put(ruleLatchThread,"ruleLatch", Arrays.asList(ruleQo,ruleQo2,ruleQo3));
//执行全部任务
excutor.excute();
//获取返回结果
List<PlatformDto> platformDto = excutor.get("platformLatch",PlatformDto.class);
List<RuleDto> ruleDto = excutor.get("ruleLatch",RuleDto.class);
System.out.println("platformLatch");
System.out.println(platformDto);

System.out.println("ruleLatch");
System.out.println(ruleDto);
```

### 打印线程池情况快照

```
ConcurrentLatchExcutorFactory.print();
```


## 配置说明

需要通过`org.zxp.ConcurrentLatch.ConcurrentLatchExcutorFactory#init`方法配置，全局只能配置一次（第二次配置报错）
默认配置详见org.zxp.ConcurrentLatch.Constants
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

3、如果仍无法获得线程池资源执行失败策略（详见下面`AFTER_TRY_BLOCK`配置）


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

### DANGER_WAIT_COUNT
```
/**
 * 最大等待获取线程池的数量
 */
static int DANGER_WAIT_COUNT = 10;
```
当排队等待获取线程池的线程数大于DANGER_WAIT_COUNT将抛出异常

## 关键逻辑说明

1. `ConcurrentLatchExcutor`中调用了线程池管理器来获取线程池
1. 通过Future获取线程执行获得返回对象
1. `LatchExcutorBlockingQueueManager`线程池管理器中通过阻塞队列来监控线程池的使用情况，线程池使用完成后不销毁，而是归还可用线程池队列，当可用线程池队列为空则无法获取线程池并执行相关失败策略，
1. 代理方式put任务时，内部会将任务返回对象包装为`LatchThreadReturn`以绑定任务名称（JDK）[2017-11-25]
1. 相关测试代码详见`org.zxp.ConcurrentLatch.demo.latch.ConcurrentLatchTester#main`


# StandAloneMR
1. StandAloneMR是一个单机处理多线程计算的框架，他的处理过程有部分类似于mapreduce的处理方式
1. 他可以自动对入参列表进行分片，并自动调度线程进行计算，计算完成后再将结果平铺返回
1. 底层采用`ConcurrentLatch`，极大的简化了并行计算时的代码量

例如下面这段代码，我有一个输入的列表1, 2, 3, 4, 5, 6, 7, 8, 9, 10
在逻辑处理中需要将每个整数+1（当然有可能处理过程中需要消耗更多时间，例子中固定消耗1s）并返回
```
Date d1 = new Date();
StandAloneMR standAloneMR = new StandAloneMRConcurrentLatch();
List<Integer> rt = standAloneMR.deal(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), s -> {
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    //System.out.println(Thread.currentThread().getName()+"===="+s);
    return s + 1;
});
Date d2 = new Date();
System.out.println(d2.getTime()-d1.getTime());
Collections.sort(rt);
rt.forEach(System.out::println);
```

## 输入分片
输入参数支持的分片方式`SplitType`有两种：hash分片、范围分片
```
public <M,T> List<T> deal(List<M> inList, Function<M,T> function,CalcType calcType,SplitType splitType);
```
默认为hash分片
## 单次任务线程数量
可以支持三种方式定义线程数：

通过`CalcType`参数可以定义当前任务采用的是CPU密集型任务还是IO密集型任务，CPU型线程数为CPU数量+1，IO型线程数为CPU数*2
```
public <M,T> List<T> deal(List<M> inList, Function<M,T> function,CalcType calcType,SplitType splitType);
```
当然也可以通过自定义线程数
```
public <M,T> List<T> deal(List<M> inList, Function<M,T> function,Integer tc,SplitType splitType);
```