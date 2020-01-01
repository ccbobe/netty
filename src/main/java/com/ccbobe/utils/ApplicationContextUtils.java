package com.ccbobe.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * 获取spring Ioc
 * @author ccbobe
 */
@Component
public class ApplicationContextUtils  implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils.applicationContext = applicationContext;
    }

    public static  <T> T getBean(Class<T> requiredType){
        return (T) applicationContext.getBean(requiredType);
    }
}
