package org.apirest.libraryapi.service.impl;

import org.apirest.libraryapi.exception.BusinessException;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.repository.BookRepository;
import org.apirest.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository repository;

    public BookServiceImpl(BookRepository repository){
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if (repository.existsByIsbn(book.getIsbn())){
            throw new BusinessException("Livro com 'isbn' já cadastrado");
        }
        return repository.save(book);
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if (book.getId() == null){
            throw new IllegalArgumentException("The Book can't be null");
        }
        this.repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if (book.getId() == null){
            throw new IllegalArgumentException("The Book can't be null");
        }
        return repository.save(book);
    }

    @Override
    public Page<Book> findByFilter(Book filter, Pageable page) {
        Example<Book> example = Example.of(filter, ExampleMatcher.matching()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return repository.findAll(example, page);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return repository.findByIsbn(isbn);
    }

}
