package cn.zzs.quartz.job;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

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
