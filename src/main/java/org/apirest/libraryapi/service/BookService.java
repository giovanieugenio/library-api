package org.apirest.libraryapi.service;

import org.apirest.libraryapi.model.entity.Book;
import org.springframework.stereotype.Service;

@Service
public interface BookService {

    Book save(Book book);
}
