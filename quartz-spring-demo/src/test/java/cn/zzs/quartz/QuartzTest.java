package cn.zzs.quartz;

import java.util.List;

import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import cn.zzs.quartz.config.JDBCQuartzConfig;

/**
 * @ClassName: QuartzTest
 * @Description: 测试Spring整合Quartz
 * @author: zzs
 * @date: 2019年9月2日 上午8:07:45
 */

public class QuartzTest {
	
	@SuppressWarnings({"unused","resource"})
	public static void main(String[] args) throws Exception {
		//内存方式
		//ApplicationContext applicationContext = new AnnotationConfigApplicationContext(RamQuartzConfig.class);
		//JDBC方式
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(JDBCQuartzConfig.class);
		//测试从数据库中恢复任务
		//ApplicationContext applicationContext = new AnnotationConfigApplicationContext(JDBCResumeQuartzConfig.class);
		//resumeJob((Scheduler)applicationContext.getBean("jdbcResumeScheduler"));
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
	public static void resumeJob(Scheduler scheduler) throws SchedulerException {
		JobKey jobKey = new JobKey("jdbc jobDetail", "jdbc jobDetailGroup");
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
