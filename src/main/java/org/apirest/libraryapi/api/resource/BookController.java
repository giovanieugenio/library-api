package org.apirest.libraryapi.api.resource;

import jakarta.validation.Valid;
import org.apirest.libraryapi.api.dto.BookDto;
import org.apirest.libraryapi.api.dto.LoanDto;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.entity.Loan;
import org.apirest.libraryapi.service.BookService;
import org.apirest.libraryapi.service.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/books")
public class BookController {

    private final BookService bookService;

    private final LoanService loanService;

    private final ModelMapper modelMapper;

    public BookController(BookService bookService, ModelMapper modelMapper, LoanService loanService){
        this.bookService = bookService;
        this.modelMapper = modelMapper;
        this.loanService = loanService;
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

    @GetMapping
    public Page<BookDto> findBookByFilter(BookDto dto, Pageable pageable){
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = bookService.findByFilter(filter, pageable);
        List<BookDto> list = result.getContent().stream().map(
                entity -> modelMapper.map(entity, BookDto.class)).collect(Collectors.toList());
        return new PageImpl<>(list, pageable, result.getTotalElements());
    }

    @GetMapping("/{id}/loans")
    public Page<LoanDto> loansByBook(@PathVariable Long id, Pageable pageable) {
        Book book = bookService.getBookById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageable);
        List<LoanDto> list = result.getContent().stream().map(
                loan -> {
                    Book loanBook = loan.getBook();
                    BookDto bookDto = modelMapper.map(loanBook, BookDto.class);
                    LoanDto loanDto = modelMapper.map(loan, LoanDto.class);
                    loanDto.setBookDto(bookDto);
                    return loanDto;
                }).collect(Collectors.toList());
        return new PageImpl<>(list, pageable, result.getTotalElements());
    }
}
