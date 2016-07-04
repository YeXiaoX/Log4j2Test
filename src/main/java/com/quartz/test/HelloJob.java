package com.quartz.test;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ivan on 2016/7/1.
 */
public class HelloJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(HelloJob.class );

    /**
     * Job，Job需要一个公有的构造函数，否则Factory无法构建
     */
    public HelloJob() {
    }

    /**
     * 实现execute方法
     */
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        for (int i=0;i<10;i++){
            logger.info("{}",i);
            logger.error("");
            logger.debug("");
            logger.trace("");
            logger.warn("");
        }
    }

}