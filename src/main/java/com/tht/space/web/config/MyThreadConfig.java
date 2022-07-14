package com.tht.space.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class MyThreadConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolExecutorConfigProperties properties){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                properties.getCoreSize(), //核心线程数
                properties.getMaxSize(),    //最大线程数
                properties.getKeepAliveTime(),     //空闲存活时间
                TimeUnit.SECONDS,   //时间单位
                new LinkedBlockingDeque<>(20), //阻塞队列
                Executors.defaultThreadFactory(), //创建工厂
                new ThreadPoolExecutor.AbortPolicy());  //拒绝策略
        return threadPoolExecutor;
    }
}
