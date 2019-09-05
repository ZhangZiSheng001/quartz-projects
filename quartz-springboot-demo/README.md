
# quartz-springboot-demo

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
测试`SpringBoot`整合`quartz`采用不同存储方式(`RAM`和`JDBC`)执行定时任务，并且测试从数据库中恢复定时任务。  

## 工程环境
JDK：1.8.0_201  

maven：3.6.1  

IDE：Spring Tool Suites4 for Eclipse  

mysql：5.7  

quartz：2.3.1  

Spring：5.1.9.RELEASE


## 主要步骤
1. 编写需要执行什么任务：继承`QuartzJobBean`；  
 
2. 配置`JobDetail`，将任务绑定到它上面；  
 
3. 配置`Trigger`，即任务的触发规则；  
 
4. 配置`Scheduler`，将`Trigger`绑定到这个调度容器里；  
 
5. 启动`SpringBoot加载配置`，任务自动执行。  


## 创建表
官方给出了具体的sql脚本，在本项目`resources/scheme`目录下已提供。这里涉及10个表。每个表的解释可以参照一下博客（包括他的系列文章都写得非常好，可以看看）：  

[精进 Quartz—Quartz大致介绍（一）](https://blog.csdn.net/u010648555/article/details/54863144)  

## 创建项目
项目类型`Maven Project`，打包方式`jar`  

## 引入依赖
这里补充一点，测试插件我配置了支持多线程，不然测试不了定时任务。  

```xml
	<!-- Spring Boot启动器父类 -->
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.7.RELEASE</version>
		<relativePath /> <!-- lookup parent from repository -->
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<!-- quartz启动器 -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-quartz</artifactId>
		</dependency>
		<!-- c3p0 -->
		<dependency>
			<groupId>com.mchange</groupId>
			<artifactId>c3p0</artifactId>
			<version>0.9.5.4</version>
		</dependency>
		<!-- 数据库驱动 -->
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-configuration-processor</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-jdbc</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
		</dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
			<!-- 配置测试的插件 -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<version>2.20</version>
				<configuration>
					<!-- 可以采用多线程测试用例 -->
					<parallel>methods</parallel>
					<threadCount>10</threadCount>
				</configuration>
			</plugin>
		</plugins>
	</build>
```

## 编写application.properties
RAM方式可以不用application.properties文件，JDBC方式也可以不用，但如果数据库还没有相关的表，可以配置如下参数：  

```properties
# 每次启动重新初始化数据库中Quartz相关的表，如果不需要每次启动服务都重新创建表，下面两项可以不配置，事先在数据库中创建好Quartz相关的表
spring.quartz.jdbc.initialize-schema=always
# 初始化脚本
spring.quartz.jdbc.schema=classpath:schema/tables_mysql.sql
```

## 编写quartz.properties
这里测试两种存储方式，分别为RAM和JDBC，所有项目里有两个properties文件。  

### RAM方式的配置
这种方式配置比较简单，只要在`properties`文件中配置`org.quartz.jobStore.class`就可以了。当然，还可以配置`Schedule`。  

```properties
org.quartz.jobStore.class:org.quartz.simpl.RAMJobStore
```

其实，RAM方式不配置`properties`文件也可以。quartz默认采用的存储方式就是RAM。    
  
### JDBC方式的配置
JDBC方式除了要配置`org.quartz.jobStore.class`，还可以选择配置数据源的参数，但是由于采用的是c3p0连接池，所以我选择在Spring中配置连接池，这样就可以直接加载`c3p0-config.xml`。  

## 编写MyJob
注意，Spring配置`JobDetail`一般有两种方式：  

一种是`JobDetailFactoryBean`，指定**执行哪个Class对象的executeInternal方法**。采用该方式定义Job时需要继承`QuartzJobBean`。  

第二种是`MethodInvokingFactoryBean`，指定**执行哪个对象的哪个方法**。采用这种方式，定义Job时可以不继承`QuartzJobBean`，方法名也可以自定义。  

MethodInvokingFactoryBean是方法级别的，看起来挺好，但是它有一个缺点，不支持JDBC存储，使用时JDBC存储方式会报以下异常：  
```
java.io.NotSerializableException: Unable to serialize JobDataMap for insertion into database because the value of property 'methodInvoker' is not serializable:   
```  

所以，整个测试中不管是RAM还是JDBC，我都采用`JobDetailFactoryBean`来定义`JobDetail`。  

```java
/**
 * @ClassName: MyJob
 * @Description: 定时任务
 * @author: zzs
 * @date: 2019年9月2日 上午8:05:44
 */
public class MyJob extends QuartzJobBean {
	private static Logger logger = LoggerFactory.getLogger(MyJob.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		if(context==null) {
			logger.warn("JobExecutionContext为空");
			return;
		}
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		if(jobDataMap==null||jobDataMap.size()==0) {
			logger.warn("jobDataMap为空");
		}
		logger.info("jobDataMap存放的信息 :  "+jobDataMap.getString("测试"));
	}
}
```

## 编写配置类
其实，我还是推荐使用Quartz原生的方法定义这几个Bean，而不是采用Spring的`FactoryBean`，相关Bean的创建可以参考[quartz-demo](https://github.com/ZhangZiSheng001/quartz-projects/tree/master/quartz-demo)   

### RAM配置类 
```java
/**
 * @ClassName: QuartzConfig
 * @Description: Quartz的相关配置-存储方式为RAM
 * @author: zzs
 * @date: 2019年9月4日 下午9:49:35
 */
@Configuration
public class RamQuartzConfig {
	/**
	 * 配置任务描述类对象
	 */
	@Bean(name = "ramJobDetail")
	public JobDetailFactoryBean getJobDetail() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setName("ram jobDetail");//job的name
		jobDetailFactoryBean.setGroup("ram jobDetailGroup");//job的group
		jobDetailFactoryBean.setJobClass(MyJob.class);//调用的哪个类
		//定义一个jobDataMap,可以封装各种数据到任务的JobExecutionContext里
		Map<String,String> map = new HashMap<String,String>(4);
		map.put("测试", "定时任务收到的消息");
		jobDetailFactoryBean.setJobDataAsMap(map);
		return jobDetailFactoryBean;
	}

	/**
	 * 配置触发器对象
	 */
	@Bean(name = "ramTrigger")
	public CronTriggerFactoryBean getCronTrigger(JobDetailFactoryBean firstJobDetail) {
		CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
		triggerFactoryBean.setName("ram trigger");//trigger的name
		triggerFactoryBean.setGroup("ram triggerGroup");//trigger的group
		triggerFactoryBean.setDescription("this is my ram trigger");//trigger的描述 
		triggerFactoryBean.setCronExpression("0/20 * * * * ? *");//20秒触发一次
		triggerFactoryBean.setJobDetail(firstJobDetail.getObject());//任务描述类对象
		return triggerFactoryBean;
	}

	/**
	 * 配置任务调度管理容器
	 */
	@Bean(name="ramScheduler")
	public SchedulerFactoryBean getSchedulerFactoryBean(CronTriggerFactoryBean firstTrigger) {
		//配置任务调度管理容器
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		//加载quartz配置文件
		Resource resource = new ClassPathResource("quartz-ram.properties");
		schedulerFactoryBean.setConfigLocation(resource);
		schedulerFactoryBean.setTriggers(firstTrigger.getObject());//将触发器放入容器
		return schedulerFactoryBean;
	}
}
```
### JDBC配置类-存储定时任务
```java
/**
 * @ClassName: JDBCQuartzConfig
 * @Description: Quartz的相关配置-存储方式为JDBC
 * @author: zzs
 * @date: 2019年9月4日 下午9:49:35
 */
@Configuration
public class JDBCQuartzConfig {

	/**
	 * 配置任务描述类对象
	 */
	@Bean(name = "jdbcJobDetail")
	public JobDetailFactoryBean getJobDetail() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setDurability(true);//JDBC方式最好开启
		jobDetailFactoryBean.setName("jdbc jobDetail");//job的name
		jobDetailFactoryBean.setGroup("jdbc jobDetailGroup");//job的group
		jobDetailFactoryBean.setJobClass(MyJob.class);//调用的哪个类
		//定义一个jobDataMap,可以封装各种数据到任务的JobExecutionContext里
		Map<String,String> map = new HashMap<String,String>(4);
		map.put("测试", "定时任务收到的消息");
		jobDetailFactoryBean.setJobDataAsMap(map);
		return jobDetailFactoryBean;
	}

	/**
	 * 配置触发器对象
	 */
	@Bean(name = "jdbcTrigger")
	public CronTriggerFactoryBean getCronTrigger(JobDetailFactoryBean firstJobDetail) {
		CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
		triggerFactoryBean.setName("jdbc trigger");//trigger的name
		triggerFactoryBean.setGroup("jdbc triggerGroup");//trigger的group
		triggerFactoryBean.setDescription("this is my jdbc trigger");//trigger的描述 
		triggerFactoryBean.setCronExpression("0/20 * * * * ? *");//20秒触发一次
		triggerFactoryBean.setJobDetail(firstJobDetail.getObject());//任务描述类对象
		return triggerFactoryBean;
	}

	/**
	 * 配置任务调度管理容器
	 */
	@Bean(name="jdbcScheduler")
	public SchedulerFactoryBean getSchedulerFactoryBean(CronTriggerFactoryBean firstTrigger,DataSource c3p0DataSource) {
		//配置任务调度管理容器
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		//加载quartz配置文件
		Resource resource = new ClassPathResource("quartz-jdbc.properties");
		schedulerFactoryBean.setConfigLocation(resource);
		schedulerFactoryBean.setTriggers(firstTrigger.getObject());//将触发器放入容器
		schedulerFactoryBean.setOverwriteExistingJobs(true);//启动时更新己存在的Job
		schedulerFactoryBean.setDataSource(c3p0DataSource);//连接池
		return schedulerFactoryBean;
	}
	
	/**
	 * 配置连接池
	 */
	@Bean(name = "c3p0DataSource")
	public DataSource getDataSource() {
		return new ComboPooledDataSource();
	}
}
```

### JDBC配置类-从数据库恢复定时任务
```java
/**
 * @ClassName: JDBCResumeQuartzConfig
 * @Description: Quartz的相关配置-存储方式为JDBC,从数据库中恢复任务
 * @author: zzs
 * @date: 2019年9月4日 下午9:49:35
 */
@Configuration
public class JDBCResumeQuartzConfig {

	/**
	 * 配置任务调度管理容器
	 */
	@Bean(name="jdbcResumeScheduler")
	public SchedulerFactoryBean getResumeSchedulerFactoryBean(DataSource c3p0DataSource) {
		//配置任务调度管理容器
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		//加载quartz配置文件
		Resource resource = new ClassPathResource("quartz-jdbc.properties");
		schedulerFactoryBean.setConfigLocation(resource);
		schedulerFactoryBean.setOverwriteExistingJobs(true);//启动时更新己存在的Job
		schedulerFactoryBean.setDataSource(c3p0DataSource);//连接池
		return schedulerFactoryBean;
	}
	
	/**
	 * 配置连接池
	 */
	@Bean(name = "c3p0DataSource")
	public DataSource getDataSource() {
		return new ComboPooledDataSource();
	}
}
```

## 编写启动类
这里利用注解排除加载不同的配置。  
RAM和JDBC存储的可以直接在启动类运行。  

```java
@SpringBootApplication
//@ComponentScan(excludeFilters={@Filter(type=FilterType.ASSIGNABLE_TYPE, classes={JDBCQuartzConfig.class,JDBCResumeQuartzConfig.class})})
//@ComponentScan(excludeFilters={@Filter(type=FilterType.ASSIGNABLE_TYPE, classes={RamQuartzConfig.class,JDBCResumeQuartzConfig.class})})
//@ConfigurationProperties(value = "application-jdbc.properties")
@ComponentScan(excludeFilters={@Filter(type=FilterType.ASSIGNABLE_TYPE, classes={JDBCQuartzConfig.class,RamQuartzConfig.class})})
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```
JDBC从数据库恢复定时任务的话，我采用新建测试类来测试。  
注意，这里不能直接使用junit测试。  
应该使用maven test来运行。  

```java
/**
 * @ClassName: QuartzTest
 * @Description: 测试Spring整合Quartz
 * @author: zzs
 * @date: 2019年9月2日 上午8:07:45
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { Application.class })
public class QuartzTest {
	@Autowired
	private Scheduler scheduler;

	@Test
	public void testResumeJob() throws Exception {
		resumeJob(scheduler);
		Thread.sleep(100000);
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
	public void resumeJob(Scheduler scheduler) throws SchedulerException {
		JobKey jobKey = new JobKey("jdbc jobDetail", "jdbc jobDetailGroup");
		List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
		// 重新恢复在firstGroup组中，名为firstJob的 job的触发器运行
		if (triggers != null) {
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
