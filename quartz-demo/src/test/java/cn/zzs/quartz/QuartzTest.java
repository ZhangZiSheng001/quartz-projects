package cn.zzs.quartz;

import java.util.Date;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
