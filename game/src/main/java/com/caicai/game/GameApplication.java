package com.caicai.game;

import com.caicai.game.common.PathFinder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,})
@EnableConfigurationProperties
public class GameApplication  {

    public static void main(String[] args) {
        SpringApplication.run(GameApplication.class, args);
    }

}
