package com.quartz.test;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by Ivan on 2016/7/1.
 */
public class TestQuartz {
    Logger log = LoggerFactory.getLogger(TestQuartz.class);

    public void run() throws Exception {
        log.info("------- Initializing ----------------------");
        SchedulerFactory sf = new StdSchedulerFactory();

// 2、通过SchedulerFactory构建Scheduler对象
        Scheduler sched = null;
        try {
            sched = sf.getScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        log.info("------- Initialization Complete -----------");
      // org.quartz.DateBuilder.evenMinuteDate  -- 通过DateBuilder构建Date
        Date runTime = evenMinuteDate(new Date());
        log.info("------- Scheduling Job  -------------------");
      // org.quartz.JobBuilder.newJob <下一分钟> --通过JobBuilder构建Job
        JobDetail job = newJob(HelloJob.class).withIdentity("job1", "group1").build();


        /*
        通过TriggerBuilder进行构建Trigger
        通过cron表达式定义任务执行时间和执行频率（这里是任务触发后十五秒后执行，每两分钟执行一次
        startAt设置了定时器的启动时间
        * */

        Trigger trigger = newTrigger().withIdentity("trigger1", "group1").withSchedule(cronSchedule("15 0/2 * * * ?"))
                .startAt(runTime).build();


        // 工厂模式，组装各个组件<JOB，Trigger>
        try {
            sched.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        log.info(job.getKey() + " will run at: " + runTime);
        // start
        try {
            sched.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        log.info("------- Started Scheduler -----------------");
        log.info("------- Waiting 65 seconds... -------------");
        try {
            Thread.sleep(65L * 1000L);
        } catch (Exception e) {
        }

      // 通过Scheduler销毁内置的Trigger和Job
   //        try {
   //            sched.shutdown(true);
   //        } catch (SchedulerException e) {
   //            e.printStackTrace();
   //        }
    }

    public static void main(String args[]) {
        TestQuartz tq = new TestQuartz();
        try {
            tq.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
