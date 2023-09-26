package com.capstone.driver;

import com.capstone.driver.core.JsonType;
import com.capstone.driver.core.Role;
import com.capstone.driver.core.Schema;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MyNoSQLDriverTest {

    @Test
    public void operations() throws InterruptedException {
        MyNoSQLDriver noSQLDriver = new MyNoSQLDriver();

        String username = "test_admin";
        String password = "123";
        File file = new File("node.txt");
        if(!file.exists() && !noSQLDriver.auth().register(username, password, Role.ADMIN))
            throw new RuntimeException("User already exists");
        if(!noSQLDriver.auth().login(username, password)) {
            if(!file.delete())
                throw new RuntimeException("Failed to delete file");
            assertTrue(noSQLDriver.auth().register(username, password, Role.ADMIN));
            assertTrue(noSQLDriver.auth().login(username, password));
        }

        Schema schema = Schema.create().addProperty("test_prop", JsonType.STRING);
        assertTrue(noSQLDriver.database().create("test_db", schema));

        assertTrue(noSQLDriver.index().create("test_db", "test_prop"));

        assertTrue(noSQLDriver.document().add("test_db", Map.of("test_prop", "test_val")));
        assertTrue(noSQLDriver.document().update("test_db", Map.entry("test_prop", "test_val"), Map.of("test_prop", "test_val_updated")));
        System.out.println(noSQLDriver.document().findAny("test_db", Map.entry("test_prop", "test_val_updated"), List.of("test_prop")));
        assertTrue(noSQLDriver.document().delete("test_db", Map.entry("test_prop", "test_val_updated")));
        System.out.println(noSQLDriver.document().findAll("test_db"));

        assertTrue(noSQLDriver.index().delete("test_db", "test_prop"));

        assertTrue(noSQLDriver.database().delete("test_db"));

        assertTrue(noSQLDriver.auth().logout());
    }
}