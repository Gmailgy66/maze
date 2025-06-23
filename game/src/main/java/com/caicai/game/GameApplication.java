package com.caicai.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,})
public class GameApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameApplication.class, args);
    }

}
