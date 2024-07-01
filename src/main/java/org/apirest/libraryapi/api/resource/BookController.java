package org.apirest.libraryapi.api.resource;

import jakarta.validation.Valid;
import org.apirest.libraryapi.api.BookDto;
import org.apirest.libraryapi.api.exceptions.ApiErrors;
import org.apirest.libraryapi.exception.BusinessException;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api/books")
public class BookController {

    private final BookService bookService;

    private final ModelMapper modelMapper;

    public BookController(BookService bookService, ModelMapper modelMapper){
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto create(@RequestBody @Valid BookDto dto){
        Book entity = modelMapper.map(dto, Book.class);
        entity = bookService.save(entity);
        return modelMapper.map(entity, BookDto.class);
    }

    @GetMapping("/{id}")
    public BookDto getBook(@PathVariable Long id){
        return bookService.getBookById(id).map(book -> modelMapper.map(book, BookDto.class)).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        Book book = bookService.getBookById(id).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable Long id, BookDto dto){
        Book book = bookService.getBookById(id).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        return modelMapper.map(bookService.update(book), BookDto.class);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        return new ApiErrors(bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessException(BusinessException e){
        return new ApiErrors(e);
    }
}
