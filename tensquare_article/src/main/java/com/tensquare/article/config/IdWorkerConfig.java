package com.tensquare.article.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import util.IdWorker;

@Configuration
public class IdWorkerConfig {

    @Bean
    public IdWorker create(){
        return new IdWorker(1, 1);
    }
}
