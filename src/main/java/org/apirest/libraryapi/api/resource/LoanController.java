package org.apirest.libraryapi.api.resource;

import org.apirest.libraryapi.api.dto.BookDto;
import org.apirest.libraryapi.api.dto.LoanDto;
import org.apirest.libraryapi.api.dto.LoanFilterDto;
import org.apirest.libraryapi.api.dto.ReturnedLoanDto;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.entity.Loan;
import org.apirest.libraryapi.service.BookService;
import org.apirest.libraryapi.service.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/loans")
public class LoanController {

    private final BookService bookService;

    private final LoanService loanService;

    private final ModelMapper modelMapper;

    public LoanController(BookService bookService, LoanService loanService, ModelMapper modelMapper){
        this.bookService = bookService;
        this.loanService = loanService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDto dto){
        Book book = bookService.getBookByIsbn(dto.getIsbn()).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Isbn non existent")
        );
        Loan loan = Loan.builder()
                .customer(dto.getCustomer())
                .book(book)
                .loanDate(LocalDate.now())
                .build();
        loan = loanService.save(loan);
        return loan.getId();
    }

    @PatchMapping("/{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDto dto){
        Loan loan = loanService.getById(id).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        loan.setReturned(dto.getReturned());
        loanService.update(loan);
    }

    @GetMapping
    public Page<LoanDto> findByFilter(LoanFilterDto dto, Pageable page){
        Page<Loan> result = loanService.findByFilter(dto, page);
        List<LoanDto> loans = result.getContent().stream().map(
                entity -> {
                    Book book = entity.getBook();
                    BookDto bookDto = modelMapper.map(book, BookDto.class);
                    LoanDto loanDto = modelMapper.map(entity, LoanDto.class);
                    loanDto.setBookDto(bookDto);
                    return loanDto;
                }).collect(Collectors.toList());
        return new PageImpl<>(loans, page, result.getTotalElements());
    }


}
