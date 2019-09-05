package cn.zzs.quartz;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
