package com.nullptr.files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(scanBasePackages = "com.nullptr.files")
public class FilesApplication {

	public static void main(String[] args) {
		SpringApplication.run(FilesApplication.class, args);
	}
}
