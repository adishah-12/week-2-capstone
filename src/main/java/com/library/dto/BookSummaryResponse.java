package com.library.dto;

import com.library.entity.Book;

import java.util.UUID;

public class BookSummaryResponse {

    private UUID bookId;
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private Integer publicationYear;
    private String description;
    private Integer totalCopies;
    private Integer availableCopies;
    private String status;

    public BookSummaryResponse(Book book) {
        this.bookId = book.getId();
        this.isbn = book.getIsbn();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.genre = book.getGenre();
        this.publicationYear = book.getPublicationYear();
        this.description = book.getDescription();
        this.totalCopies = book.getTotalCopies();
        this.availableCopies = book.getAvailableCopies();
        this.status = book.getStatus();
    }

    public UUID getBookId() {
        return bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public String getDescription() {
        return description;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public String getStatus() {
        return status;
    }
}