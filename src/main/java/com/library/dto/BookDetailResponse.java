package com.library.dto;

import com.library.entity.Book;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookDetailResponse {

    private UUID bookId;
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private Integer publicationYear;
    private String description;
    private String publisher;
    private Integer pageCount;
    private String language;
    private Integer totalCopies;
    private Integer availableCopies;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookDetailResponse(Book book) {
        this.bookId = book.getId();
        this.isbn = book.getIsbn();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.genre = book.getGenre();
        this.publicationYear = book.getPublicationYear();
        this.description = book.getDescription();
        this.publisher = book.getPublisher();
        this.pageCount = book.getPageCount();
        this.language = book.getLanguage();
        this.totalCopies = book.getTotalCopies();
        this.availableCopies = book.getAvailableCopies();
        this.status = book.getStatus();
        this.createdAt = book.getCreatedAt();
        this.updatedAt = book.getUpdatedAt();
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

    public String getPublisher() {
        return publisher;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public String getLanguage() {
        return language;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}