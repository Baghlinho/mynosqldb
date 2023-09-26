package com.capstone.node;

import com.capstone.node.core.DatabaseManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkerApplication {

	public static void main(String[] args) {
		DatabaseManager.initialize();
		SpringApplication.run(WorkerApplication.class, args);
	}

}
