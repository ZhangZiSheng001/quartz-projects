package cn.zzs.quartz;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
