package com.log4j2.test;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.MDC;

/**
 * Created by Ivan on 2016/6/28.
 */
public class Test {

    private static final Logger logger = LogManager.getLogger(Test.class);

    public static void main(String[] args) {

        MDC.put("username", "admin");
        MDC.put("sessionID", "1234");

        if (logger.isTraceEnabled()) {
            logger.debug("log4j trace message");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("log4j debug message");
        }
        if (logger.isInfoEnabled()) {
            logger.debug("log4j info message");
        }
        if(logger.isErrorEnabled()){
            logger.error(null,"log4j error message");
        }
    }
}
