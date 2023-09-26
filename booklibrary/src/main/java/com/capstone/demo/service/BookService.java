package com.capstone.demo.service;

import com.capstone.demo.dao.BookDao;
import com.capstone.demo.model.Book;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookDao bookDao;

    public BookService(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    public List<Book> getAllBooks() {
        return bookDao.findAll();
    }


    public boolean addBook(Book book) {
        return bookDao.findByTitle(book.getTitle()) == null &&
                bookDao.add(book);
    }

    public boolean updateBook(Book book) {
        return bookDao.update(book);
    }

    public boolean deleteBook(String title) {
        return bookDao.delete(title);
    }
}
