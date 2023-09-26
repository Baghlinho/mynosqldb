package com.capstone.node;

import com.capstone.node.core.DatabaseManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NoSQLNodeApplication {

	public static void main(String[] args) {
		DatabaseManager.initialize();
		SpringApplication.run(NoSQLNodeApplication.class, args);
	}

}
