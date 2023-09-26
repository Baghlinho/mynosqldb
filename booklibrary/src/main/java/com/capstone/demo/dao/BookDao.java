package com.capstone.demo.dao;

import com.capstone.demo.model.Book;

import java.util.List;

public interface BookDao {

    Book findByTitle(String title);
    List<Book> findAll();
    boolean add(Book book);
    boolean update(Book book);
    boolean delete(String title);
}
