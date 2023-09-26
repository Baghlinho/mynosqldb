package com.capstone.demo.dao;

import com.capstone.demo.model.Book;
import com.capstone.driver.MyNoSQLDriver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class BookDaoImpl implements BookDao {

    @Value("${mynosql.dbname}")
    private String databaseName;
    @Value("${mynosql.index}")
    private String indexProperty;
    private final MyNoSQLDriver myNoSQLDriver;
    private final ObjectMapper objectMapper;

    public BookDaoImpl(MyNoSQLDriver myNoSQLDriver) {
        this.myNoSQLDriver = myNoSQLDriver;
        objectMapper = new ObjectMapper();
    }

    @Override
    public Book findByTitle(String title) {
        String bookJSON = myNoSQLDriver.document()
                .findAny(
                        databaseName,
                        Map.entry(indexProperty, title)
                );
        try {
            if(bookJSON.isEmpty()) return null;
            return objectMapper.readValue(bookJSON, Book.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Book> findAll() {
        String booksJSON = myNoSQLDriver.document()
                .findAll(databaseName);
        try {
            return Arrays.asList(objectMapper.readValue(booksJSON, Book[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean add(Book book) {
        return myNoSQLDriver.document()
                .add(
                        databaseName,
                        objectMapper.convertValue(book, new TypeReference<>() {})
                );
    }

    @Override
    public boolean update(Book book) {
        return myNoSQLDriver.document()
                .update(
                        databaseName,
                        Map.entry(indexProperty, book.getTitle()),
                        objectMapper.convertValue(book, new TypeReference<>() {})
                );
    }

    @Override
    public boolean delete(String title) {
        return myNoSQLDriver.document()
                .delete(
                        databaseName,
                        Map.entry(indexProperty, title)
                );
    }
}
