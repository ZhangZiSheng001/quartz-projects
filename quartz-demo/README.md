
# quartz-demo

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

## 主要步骤
1. 编写需要执行什么任务：实现`Job`接口；  

2. 将任务绑定到`JobDetail`上；  

3. 编写任务的触发规则：创建`Trigger`对象；  

4. 将`JobDetail`和`Trigger`绑定到`Scheduler`；  

5. 启动`Scheduler`。  

## 创建表
官方给出了具体的sql脚本，在本项目`resources/scheme`目录下已提供。这里涉及10个表。每个表的解释可以参照一下博客（包括他的系列文章都写得非常好，可以看看）：  

[精进 Quartz—Quartz大致介绍（一）](https://blog.csdn.net/u010648555/article/details/54863144)

## 创建项目
项目类型`Maven Project`，打包方式`jar`  

## 引入依赖
quartz默认已经依赖了`c3p0`连接池，这里就不需要再引入了。  
```xml
<!-- quartz -->
<dependency>
	<groupId>org.quartz-scheduler</groupId>
	<artifactId>quartz</artifactId>
	<version>2.3.1</version>
</dependency>
<!-- logback -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-core</artifactId>
    <version>1.2.3</version>
    <type>jar</type>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.3</version>
    <type>jar</type>
</dependency>
<!-- 数据库驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.17</version>
</dependency>
```

## 编写quartz.properties
这里测试两种存储方式，所有项目里有两个`properties`文件。  

### RAM方式的配置
这种方式配置比较简单，只要在`properties`文件中配置`org.quartz.jobStore.class`就可以了。当然，还可以配置`Schedule`。  

```properties
org.quartz.jobStore.class:org.quartz.simpl.RAMJobStore
```

其实，RAM方式不配置`properties`文件也可以。`quartz`默认采用的存储方式就是RAM。  

### JDBC方式的配置
JDBC方式除了要配置`org.quartz.jobStore.class`，还需要配置数据源的参数。如下：  
  
```properties
#数据保存方式为持久化
org.quartz.jobStore.class:org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.driverDelegateClass:org.quartz.impl.jdbcjobstore.StdJDBCDelegate
#设置为TRUE不会出现序列化非字符串类到 BLOB 时产生的类版本问题
org.quartz.jobStore.useProperties:true
org.quartz.jobStore.tablePrefix:qrtz_
org.quartz.jobStore.dataSource:qzDS
org.quartz.dataSource.qzDS.driver:com.mysql.cj.jdbc.Driver
org.quartz.dataSource.qzDS.URL:jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=true
org.quartz.dataSource.qzDS.user:root
org.quartz.dataSource.qzDS.password:root
org.quartz.dataSource.qzDS.maxPoolSize:10
org.quartz.dataSource.qzDS.minPoolSize:3
org.quartz.dataSource.qzDS.maxIdleTime:0
org.quartz.dataSource.qzDS.initialPoolSize:3
```

## 编写MyJob
注意，这里要实现Job接口。  

```java
/**
 * @ClassName: MyJob
 * @Description: 定时任务
 * @author: zzs
 * @date: 2019年9月2日 上午8:05:44
 */
public class MyJob implements Job{
	private static Logger logger = LoggerFactory.getLogger(MyJob.class);
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("hello quartz " + new Date());
	}
}
```

## 编写测试类
注意，这里不能使用junit测试。  

```java
/**
 * @ClassName: QuartzTest
 * @Description: 测试存储方式为RAM的定时调度
 * @author: zzs
 * @date: 2019年9月2日 上午8:07:45
 */
public class QuartzTest {
	private static Logger logger = LoggerFactory.getLogger(QuartzTest.class);
	
	public static void main(String[] args) throws Exception {
		//startSchedule();
		//resumeJob();
	}
	
	//注意：这里不能用junit测试
	public static void startSchedule() throws SchedulerException {
		//1.创建JobDetail
		JobDetail jobDetail = JobBuilder.newJob(MyJob.class)
				.withDescription("this is my first job") //job的描述
				.withIdentity("firstJob", "firstGroup") //job 的name和group
				.build();

		//任务运行的时间，SimpleSchedle类型触发器有效
		long time = System.currentTimeMillis() + 3 * 1000L; //3秒后启动任务
		Date statTime = new Date(time);

		//2.创建Trigger
		//使用SimpleScheduleBuilder或者CronScheduleBuilder
		Trigger trigger = TriggerBuilder.newTrigger()
				.withDescription("this is my first trigger")
				.withIdentity("firstTrigger", "firstTriggerGroup")
				//.withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(statTime) //默认当前时间启动
				.withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?")) //10秒执行一次
				.build();
		
		//3.创建Scheduler的工厂
		//不指定默认文件名为quartz.properties
		//SchedulerFactory schedulerFactory = new StdSchedulerFactory("quartz-ram.properties");//RAM方式
		SchedulerFactory schedulerFactory = new StdSchedulerFactory("quartz-jdbc.properties");//JDBC方式
		
		//4.从工厂中获取调度器实例
		Scheduler scheduler = schedulerFactory.getScheduler();

		//5.注册任务和定时器
		scheduler.scheduleJob(jobDetail, trigger);

		//6.启动 调度器
		scheduler.start();
		logger.info("启动时间 ： " + new Date());
	}
	
	/**
	 * 
	 * @Title: resumeJob
	 * @Description: 从数据库中找到已经存在的job，并重新开启调度
	 * @author: zzs
	 * @date: 2019年9月2日 上午9:21:33
	 * @return: void
	 * @throws SchedulerException 
	 */
	public static void resumeJob() throws SchedulerException {
		SchedulerFactory schedulerFactory = new StdSchedulerFactory("quartz-jdbc.properties");
		Scheduler scheduler = schedulerFactory.getScheduler();
		JobKey jobKey = new JobKey("firstJob", "firstGroup");
		List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
		// 重新恢复在firstGroup组中，名为firstJob的 job的触发器运行
		if(triggers!=null){
			for (Trigger tg : triggers) {
				// 根据类型判断
				if ((tg instanceof CronTrigger) || (tg instanceof SimpleTrigger)) {
					// 恢复job运行
					scheduler.resumeJob(jobKey);
				}
			}
			scheduler.start();
		}
	}
}
```
## cron表达式语法
最后，补充下cron表达式的语法。  
Cron 表达式以 5 或 6 个空格隔开，分为 6 或 7 个域。  

### 域
1. Seconds（秒）：可出现", - * /"  
2. Minutes（分钟）：可出现", - * /"  
3. Hours（小时）:可出现", - * /"  
4. DayofMonth（日 of 月）:可出现", - * / ? LW C"  
5. Month（月）:可出现", - * /"  
6. DayofWeek（日 of 星期）:可出现", - * / ? L C #"，注意，1 表示星期天，2 表示星期一， 依次类推。  
7. Year（年）:可出现", - * /"，有效范围为 1970-2099 年  

### 字符含义
	1) * 表示匹配该域的任意值，假如在 Minutes 域使用*, 即表示每分钟都会触发事件。  

	2) ? 表示不指定值。只能用在 DayofMonth 和 DayofWeek 两个域。因为DayofMonth 和 DayofWeek 会相互影响。例如想在每月的 20 日触发调度，不管20 日到底是星期几，则只能使用如下写法： 13 13 15 20 * ?, 其中最后一位只能用？，而不能使用*，如果使用*表示不管星期几都会触发。  

	3) - 表示范围，例如在 Minutes 域使用 5-20，表示从 5 分到 20 分钟每分钟触发一次  

	4) / 表示起始时间开始触发，然后每隔固定时间触发一次，例如在 Minutes 域使用 5/20,则意味着 5 分钟触发一次，而 25，45 等分别触发一次  

	5) ， 表示列出枚举值值。例如：在 Minutes 域使用 5,20，则意味着在 5 和 20 分每分钟触发一次。  

	6) L 表示最后，只能出现在 DayofMonth 和 DayofWeek 域。如果在 DayofMonth写 L 表示这个月的最后一天，如果在 DayofWeek 写 L 表示每个星期的最后一天（星期六） 。如果在 DayofWeek 域使用 5L,意味着在最后的一个星期四触发。  

	7) W 表示最近有效工作日(周一到周五),只能出现在 DayofMonth 域，系统将在离指定日期的最近的有效工作日触发事件。例如：在 DayofMonth 使用 5W，如果 5 日是星期六，则将在最近的工作日：星期五，即 4 日触发。如果 5 日是星期天，则在 6 日(周一)触发；如果 5 日在星期一到星期五中的一天，则就在 5 日触发。另外一点，W 的最近寻找不会跨过月份  

	8) LW:这两个字符可以连用，表示在某个月最后一个工作日，即最后一个星期五  

	9) #:用于确定每个月第几个星期几，只能出现在 DayofWeek 域。例如在 4#2，表示某月的第二个星期三。  

### 示例
	"0 0 12 * * ?" 每天中午 12 点触发
	"0 15 10 ? * *" 每天上午 10:15 触发
	"0 15 10 * * ?" 每天上午 10:15 触发
	"0 15 10 * * ? *" 每天上午 10:15 触发
	"0 15 10 * * ? 2005" 2005 年的每天上午 10:15 触发
	"0 * 14 * * ?" 在每天下午 2 点到下午 2:59 期间的每 1 分钟触发
	"0 0/5 14 * * ?" 在每天下午 2 点到下午 2:55 期间的每 5 分钟触发
	"0 0/5 14,18 * * ?" 在每天下午 2 点到 2:55 期间和下午 6 点到 6:55 期间的每 5 分钟触发
	"0 0-5 14 * * ?" 在每天下午 2 点到下午 2:05 期间的每 1 分钟触发
	"0 10,44 14 ? 3 4" 每年三月的星期三的下午 2:10 和 2:44 触发
	"0 15 10 ? * MON-FRI" 周一至周五的上午 10:15 触发
	"0 15 10 15 * ?" 每月 15 日上午 10:15 触发
	"0 15 10 L * ?" 每月最后一日的上午 10:15 触发
	"0 15 10 ? * 6L" 每月的最后一个星期五上午 10:15 触发
	"0 15 10 ? * 6L 2002-2005" 2002 年至 2005 年的每月的最后一个星期五上午 10:15触发
	"0 15 10 ? * 6#3" 每月的第三个星期五上午 10:15 触发  

## 项目路径
[quartz-demo](https://github.com/ZhangZiSheng001/quartz-projects/tree/master/quartz-demo)   

[quartz-spring-demo](https://github.com/ZhangZiSheng001/quartz-projects/tree/master/quartz-spring-demo)   

[quartz-springboot-demo](https://github.com/ZhangZiSheng001/quartz-projects/tree/master/quartz-springboot-demo)   

## 参考资料
[dufyun's projects : learn_quartz](https://github.com/dufyun/quartz-core-learning/tree/master/learn_quartz)

> 学习使我快乐！！
