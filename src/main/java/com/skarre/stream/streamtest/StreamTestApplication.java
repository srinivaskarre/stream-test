package com.skarre.stream.streamtest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.skarre")
public class StreamTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamTestApplication.class, args);
	}

}
