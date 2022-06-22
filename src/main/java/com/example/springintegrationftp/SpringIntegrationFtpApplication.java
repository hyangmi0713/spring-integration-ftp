package com.example.springintegrationftp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.List;

@SpringBootApplication
public class SpringIntegrationFtpApplication {
    @Value("${cmd.directory}")
    public String directory;

    public static void main(String[] args) {
        SpringApplication.run(SpringIntegrationFtpApplication.class, args);
    }

    @Bean
    public ApplicationRunner runner(FTPConfiguration.GateFile gateFile) {
        return args -> {
            List<File> files = gateFile.mget(directory);
            for (File file : files) {
                System.out.println("File" + file.getAbsolutePath());
            }
        };
    }
}
