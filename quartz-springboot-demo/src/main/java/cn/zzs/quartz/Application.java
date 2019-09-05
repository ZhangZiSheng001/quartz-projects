package cn.zzs.quartz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

import cn.zzs.quartz.config.JDBCQuartzConfig;
import cn.zzs.quartz.config.RamQuartzConfig;

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
