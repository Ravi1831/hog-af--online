package com.ravi.hogwartsartifact;

import com.ravi.hogwartsartifact.artifact.utils.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HogwartsArtifactApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(HogwartsArtifactApplication.class, args);}


    @Bean
    public IdWorker idWorker(){
        return new IdWorker(1,1);
    }
}
