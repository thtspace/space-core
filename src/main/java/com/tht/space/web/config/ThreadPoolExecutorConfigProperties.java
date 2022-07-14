package com.tht.space.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "my.thread")
@Component
@Data
public class ThreadPoolExecutorConfigProperties {
    private Integer coreSize = 20;
    private Integer maxSize = 100;
    private Integer keepAliveTime = 10;
}
