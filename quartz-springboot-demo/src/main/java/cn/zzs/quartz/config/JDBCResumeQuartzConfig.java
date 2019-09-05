package cn.zzs.quartz.config;

import java.util.List;

import javax.sql.DataSource;

import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.mchange.v2.c3p0.ComboPooledDataSource;

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
	@Bean(name = "jdbcResumeScheduler")
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
