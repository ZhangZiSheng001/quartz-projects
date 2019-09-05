
# quartz-projects

## 简介    
`quartz`可以用来实现任务的定时执行，支持将`JobDetail`和`Trigger`存入数据库，且能够再程序重启后从数据库中恢复定时任务。  

使用`quartz`需要注意几个重要的组件：  
1. **Job**
需要执行的具体内容。  

2. **JobDetail**
用来告诉调度容器，将来执行哪个`Job`的哪个方法。  
  
3. **Trigger**
描述触发Job执行的时间触发规则。一个Job可以对应多个Trigger，但一个Trigger只能对应一个Job。  

`quartz`中五种类型的 `Trigger`：`SimpleTrigger`，`CronTirgger`，`DateIntervalTrigger`，`NthIncludedDayTrigger`和`Calendar` 类（ `org.quartz.Calendar`）。  

最常用的：
`SimpleTrigger`：用来触发只需执行一次或者在给定时间触发并且重复N次且每次执行延迟一定时间的任务。  

`CronTrigger`：使用cron表达式的规则触发。  

4. **Scheduler** 
调度容器，维护`JobDetail`和`Trigger`。`Scheduler`可以将`JobDetail`绑定到某一`Trigger`中，这样当`Trigger`触发时，对应的`Job`就被执行。  

5. **JobStore**
数据保存方式。常用的：`RAMJobStore`（内存方式）和`JobStoreTX`（JDBC方式）。

## 需求
测试使用`quartz`不同存储方式(`RAM`和`JDBC`)执行定时任务，并且从测试从数据库中恢复定时任务。

## 工程环境
JDK：1.8.0_201  

maven：3.6.1  

IDE：Spring Tool Suites4 for Eclipse  

mysql：5.7  

quartz：2.3.1  

## 项目路径
[quartz-demo](https://github.com/ZhangZiSheng001/quartz-projects/tree/master/quartz-demo)   

[quartz-spring-demo](https://github.com/ZhangZiSheng001/quartz-projects/tree/master/quartz-spring-demo)   

[quartz-springboot-demo](https://github.com/ZhangZiSheng001/quartz-projects/tree/master/quartz-springboot-demo)   

> 学习使我快乐！！
