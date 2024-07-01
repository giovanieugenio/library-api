package org.apirest.libraryapi.service.impl;

import org.apirest.libraryapi.exception.BusinessException;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.repository.BookRepository;
import org.apirest.libraryapi.service.BookService;
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
        repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        return repository.save(book);
    }

}
