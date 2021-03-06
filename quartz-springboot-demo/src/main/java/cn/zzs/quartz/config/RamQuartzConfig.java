package cn.zzs.quartz.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import cn.zzs.quartz.job.MyJob;

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
		Map<String, String> map = new HashMap<String, String>(4);
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
	@Bean(name = "ramScheduler")
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
