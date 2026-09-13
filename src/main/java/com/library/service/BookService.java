package com.library.service;

import com.library.dto.BookDetailResponse;
import com.library.dto.BookSummaryResponse;
import com.library.dto.PagedResponse;
import com.library.entity.Book;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BookSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookService {

    // Never pass a raw client-supplied string straight into Sort.by().
    private static final Set<String> SORTABLE_FIELDS = Set.of("title", "author", "publicationYear");

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public PagedResponse<BookSummaryResponse> listBooks(int page, int size, String sortBy, String sortOrder,
                                                        String query, String genre, String isbn,
                                                        boolean availableOnly) {
        String sortField = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "title";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));

        Specification<Book> spec = null;
        if (query != null && !query.isBlank()) {
            spec = BookSpecifications.titleOrAuthorContains(query);
        }
        if (genre != null && !genre.isBlank()) {
            spec = spec == null ? BookSpecifications.genreEquals(genre) : spec.and(BookSpecifications.genreEquals(genre));
        }
        if (isbn != null && !isbn.isBlank()) {
            spec = spec == null ? BookSpecifications.isbnEquals(isbn) : spec.and(BookSpecifications.isbnEquals(isbn));
        }
        if (availableOnly) {
            spec = spec == null ? BookSpecifications.availableOnly() : spec.and(BookSpecifications.availableOnly());
        }

        Page<Book> result = bookRepository.findAll(spec, pageRequest);
        List<BookSummaryResponse> content = result.getContent().stream()
                .map(BookSummaryResponse::new)
                .toList();

        return new PagedResponse<>(content, result);
    }

    public BookDetailResponse getBookDetail(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
        return new BookDetailResponse(book);
    }
}