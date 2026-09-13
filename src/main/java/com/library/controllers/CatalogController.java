package com.library.controllers;

import com.library.dto.BookDetailResponse;
import com.library.dto.BookSummaryResponse;
import com.library.dto.PagedResponse;
import com.library.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/catalog/books")
public class CatalogController {

    private final BookService bookService;

    public CatalogController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public PagedResponse<BookSummaryResponse> listBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String isbn,
            @RequestParam(defaultValue = "false") boolean availableOnly) {
        return bookService.listBooks(page, size, sortBy, sortOrder, query, genre, isbn, availableOnly);
    }

    @GetMapping("/{bookId}")
    public BookDetailResponse getBook(@PathVariable UUID bookId) {
        return bookService.getBookDetail(bookId);
    }
}