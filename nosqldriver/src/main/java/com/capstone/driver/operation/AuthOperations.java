package com.capstone.driver.operation;

import com.capstone.driver.core.Role;

public interface AuthOperations {
    boolean login(String username, String password);

    boolean register(String username, String password, Role role) throws InterruptedException;

    boolean logout();
}
