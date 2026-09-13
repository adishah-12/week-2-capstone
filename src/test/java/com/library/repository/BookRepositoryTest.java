package com.library.repository;

import com.library.config.JpaAuditingConfig;
import com.library.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book book(String isbn, String title, String author, String genre, int year, int available) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setPublicationYear(year);
        book.setDescription("A book");
        book.setTotalCopies(5);
        book.setAvailableCopies(available);
        return book;
    }

    @BeforeEach
    void seedBooks() {
        entityManager.persistAndFlush(book("111", "Clean Code", "Robert Martin", "Technology", 2008, 2));
        entityManager.persistAndFlush(book("222", "Refactoring", "Martin Fowler", "Technology", 2018, 0));
        entityManager.persistAndFlush(book("333", "Dune", "Frank Herbert", "Fiction", 1965, 3));
    }

    @Test
    void searchByTitleOrAuthor_isCaseInsensitive() {
        var results = bookRepository.findAll(BookSpecifications.titleOrAuthorContains("clean"), PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void filterByGenre_returnsOnlyMatchingGenre() {
        var results = bookRepository.findAll(BookSpecifications.genreEquals("Technology"), PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(2);
    }

    @Test
    void filterByIsbn_returnsExactMatch() {
        var results = bookRepository.findAll(BookSpecifications.isbnEquals("333"), PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Dune");
    }

    @Test
    void availableOnly_excludesZeroCopyBooks() {
        var results = bookRepository.findAll(BookSpecifications.availableOnly(), PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).noneMatch(b -> b.getAvailableCopies() == 0);
    }

    @Test
    void combinedFilters_genreAndAvailableOnly() {
        Specification<Book> spec = BookSpecifications.genreEquals("Technology").and(BookSpecifications.availableOnly());
        var results = bookRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void sortByPublicationYear_descending() {
        var results = bookRepository.findAll((Specification<Book>) null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publicationYear")));

        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Refactoring");
        assertThat(results.getContent().get(2).getTitle()).isEqualTo("Dune");
    }

    @Test
    void pagination_returnsCorrectMetadata() {
        var results = bookRepository.findAll((Specification<Book>) null, PageRequest.of(0, 2));

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getTotalElements()).isEqualTo(3);
        assertThat(results.getTotalPages()).isEqualTo(2);
        assertThat(results.isLast()).isFalse();
    }

    @Test
    void duplicateIsbn_violatesUniqueConstraint() {
        Book duplicate = book("111", "Another Title", "Someone Else", "Fiction", 2020, 1);
        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate)).isInstanceOf(Exception.class);
    }
}