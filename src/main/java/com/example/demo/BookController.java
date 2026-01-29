package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;
import com.example.demo.google.BookService;
import com.example.demo.google.GoogleBook;
import com.example.demo.google.GoogleBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookController {
    private final BookRepository bookRepository;
    private final GoogleBookService googleBookService;
    private final BookService bookService;


    @Autowired
    public BookController(BookRepository bookRepository, GoogleBookService googleBookService,BookService bookService) {
        this.bookRepository = bookRepository;
        this.googleBookService = googleBookService;
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/google")
    public GoogleBook searchGoogleBooks(@RequestParam("q") String query,
                                        @RequestParam(value = "maxResults", required = false) Integer maxResults,
                                        @RequestParam(value = "startIndex", required = false) Integer startIndex) {
        return googleBookService.searchBooks(query, maxResults, startIndex);
    }

    @PostMapping("/books/{googleId}")
    public Book addBookFromGoogle(@PathVariable String googleId) {
        return bookService.addBookFromGoogle(googleId);
    }

}
