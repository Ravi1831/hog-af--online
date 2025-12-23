package com.ravi.hogwartsartifact;

import com.ravi.hogwartsartifact.artifact.ArtifactService;
import com.ravi.hogwartsartifact.artifact.utils.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HogwartsArtifactApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(HogwartsArtifactApplication.class, args);
        Object artifactService = run.getBean(ArtifactService.class);

        System.out.println("artifactService = " + artifactService);
        int beanDefinitionCount = run.getBeanDefinitionCount();
        System.out.println("total bean count "+beanDefinitionCount);
        String[] beanDefinitionNames = run.getBeanDefinitionNames();
        for (String st:beanDefinitionNames){
            System.out.println("context "+st);
        }
    }


    @Bean
    public IdWorker idWorker(){
        return new IdWorker(1,1);
    }
}
