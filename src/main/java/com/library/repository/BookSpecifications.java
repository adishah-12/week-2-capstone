package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecifications {

    private BookSpecifications() {
    }

    public static Specification<Book> titleOrAuthorContains(String query) {
        String like = "%" + query.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("author")), like)
        );
    }

    public static Specification<Book> genreEquals(String genre) {
        return (root, cq, cb) -> cb.equal(root.get("genre"), genre);
    }

    public static Specification<Book> isbnEquals(String isbn) {
        return (root, cq, cb) -> cb.equal(root.get("isbn"), isbn);
    }

    public static Specification<Book> availableOnly() {
        return (root, cq, cb) -> cb.greaterThan(root.get("availableCopies"), 0);
    }
}