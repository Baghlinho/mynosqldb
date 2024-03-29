package com.capstone.node.core;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {

    private static final long serialVersionUID = 4L;

    public enum Role {Admin, Viewer, Editor}

    private String username;
    private String passwordHash;
    private Role role;
    private int nodeId;


    public User(String username, String passwordHash, Role role, int nodeId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.nodeId = nodeId;
    }

    public static Role getRole(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                return Role.Admin;
            case "viewer":
                return Role.Viewer;
            case "editor":
                return Role.Editor;
        }
        return null;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", role=" + role +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(passwordHash, user.passwordHash) && role == user.role && Objects.equals(nodeId, user.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, passwordHash, role, nodeId);
    }
}
