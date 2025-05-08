package org.commons;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "org.commons.*.mapper")
public class CommonExImportApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonExImportApplication.class, args);
    }

}
