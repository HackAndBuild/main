package com.example.demo.google;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final GoogleBookService googleBookService;

    public BookService(BookRepository bookRepository, GoogleBookService googleBookService) {
        this.bookRepository = bookRepository;
        this.googleBookService = googleBookService;
    }

    public Book addBookFromGoogle(String googleId) {
        GoogleBook.Item item;
        try {
            item = googleBookService.getBookById(googleId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Google Book ID"
            );
        }

        if (item == null || item.volumeInfo() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Google Book ID"
            );
        }

        GoogleBook.VolumeInfo v = item.volumeInfo();
        String author = (v.authors() != null && !v.authors().isEmpty()) ? v.authors().get(0) : null;

        Book book = new Book(item.id(), v.title(), author, v.pageCount());
        return bookRepository.save(book);
    }
}
