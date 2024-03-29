package com.capstone.node.core;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class Node implements Serializable {

    private static final long serialVersionUID = 3L;

    private int id;
    private String address;
    private int numDocuments;
    private int numUsers;


    public Node(int id, String address, int numDocuments, int numUsers) {
        this.id = id;
        this.address = address;
        this.numDocuments = numDocuments;
        this.numUsers = numUsers;
    }

    public void incNumDocuments() {
        numDocuments++;
    }
    public void decNumDocuments() {
        numDocuments--;
    }
    public void incNumUsers() { numUsers++; }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", numDocuments=" + numDocuments +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return numDocuments == node.numDocuments && id == node.id && address.equals(node.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address, numDocuments);
    }


}
