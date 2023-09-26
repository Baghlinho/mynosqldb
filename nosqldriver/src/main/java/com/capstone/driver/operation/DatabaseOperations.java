package com.capstone.driver.operation;

import com.capstone.driver.core.Schema;

public interface DatabaseOperations {
    boolean create(String name, Schema schema);

    boolean delete(String name);
}
