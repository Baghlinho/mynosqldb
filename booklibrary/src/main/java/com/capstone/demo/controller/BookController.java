package com.capstone.demo.controller;

import com.capstone.demo.model.Book;
import com.capstone.demo.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @DeleteMapping
    public String deleteBook(@RequestParam String title) {
        return bookService.deleteBook(title) ?
                "Book deleted successfully" :
                "Failed to delete book";
    }

    @PutMapping
    public String updateBook(@RequestBody Book book) {
        return bookService.updateBook(book) ?
                "Book updated successfully" :
                "Failed to update book";
    }

    @PostMapping
    public String addBook(@RequestBody Book book) {
        return bookService.addBook(book) ?
                "Book added successfully" :
                "Failed to add book";
    }
}
