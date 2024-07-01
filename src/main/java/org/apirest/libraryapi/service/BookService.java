package org.apirest.libraryapi.service;

import org.apirest.libraryapi.model.entity.Book;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface BookService {

    Book save(Book book);

    Optional<Book> getBookById(Long id);

    void delete(Book book);

    Book update(Book book);
}
