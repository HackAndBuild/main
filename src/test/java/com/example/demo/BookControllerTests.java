package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@Transactional
class BookControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookRepository bookRepository;

    static MockWebServer mockWebServer;

    @DynamicPropertySource
    static void overrideGoogleBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "google.books.base-url",
                () -> mockWebServer.url("/").toString()
        );
    }

    @BeforeAll
    static void startServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void stopServer() throws Exception {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setup() {
        bookRepository.deleteAll();
        bookRepository.save(
                new Book("lRtdEAAAQBAJ", "Spring in Action", "Craig Walls", 520)
        );
        bookRepository.save(
                new Book("12muzgEACAAJ", "Effective Java", "Joshua Bloch", 412)
        );
    }

    @Test
    void testGetAllBooks() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring in Action"))
                .andExpect(jsonPath("$[1].title").value("Effective Java"));
    }

    @Test
    void testAddBookFromGoogle_success() throws Exception {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""
                                {
                                  "id": "cOYLEQAAQBAJ",
                                  "volumeInfo": {
                                    "title": "Take Control of Your Online Privacy, 5th Edition",
                                    "authors": ["Joe Kissell"],
                                    "pageCount": 137
                                  }
                                }
                                """)
        );

        mockMvc.perform(post("/books/cOYLEQAAQBAJ"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("cOYLEQAAQBAJ"))
                .andExpect(jsonPath("$.title")
                        .value("Take Control of Your Online Privacy, 5th Edition"))
                .andExpect(jsonPath("$.author").value("Joe Kissell"))
                .andExpect(jsonPath("$.pageCount").value(137));

        assertThat(bookRepository.findAll()).hasSize(3);
    }

    @Test
    void testAddBookFromGoogle_invalidId_returns400() throws Exception {

        mockWebServer.enqueue(
                new MockResponse().setResponseCode(404)
        );

        mockMvc.perform(post("/books/invalid-google-id"))
                .andExpect(status().isBadRequest());

        assertThat(bookRepository.findAll()).hasSize(2);
    }
}
