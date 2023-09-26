package com.capstone.demo.config;

import com.capstone.driver.MyNoSQLDriver;
import com.capstone.driver.core.JsonType;
import com.capstone.driver.core.Role;
import com.capstone.driver.core.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class MyNoSQLConfig {

    @Value("${mynosql.dbname}")
    private String databaseName;
    @Value("${mynosql.index}")
    private String indexProperty;
    @Value("${mynosql.username}")
    private String username;
    @Value("${mynosql.password}")
    private String password;

    @Bean
    public MyNoSQLDriver myNoSQLDriver() throws InterruptedException {
        MyNoSQLDriver myNoSQLDriver = new MyNoSQLDriver();
        File file = new File("node.txt");
        if(!file.exists() && !myNoSQLDriver.auth().register(username, password, Role.ADMIN))
            throw new RuntimeException("User already exists");
        if(!myNoSQLDriver.auth().login(username, password)) {
            if(!file.delete())
                throw new RuntimeException("Failed to delete file");
            myNoSQLDriver.auth().register(username, password, Role.ADMIN);
            myNoSQLDriver.auth().login(username, password);
        }
        Schema schema = Schema.create()
                .addProperty("title", JsonType.STRING)
                .addProperty("price", JsonType.NUMBER)
                .addProperty("original", JsonType.BOOLEAN)
                .addProperty("author", JsonType.OBJECT)
                .addProperty("reviews", JsonType.ARRAY);
        myNoSQLDriver.database().create(databaseName, schema);
        myNoSQLDriver.index().create(databaseName, indexProperty);
        return myNoSQLDriver;
    }
}
