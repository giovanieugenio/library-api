package org.apirest.libraryapi.service;

import org.apirest.libraryapi.api.dto.LoanFilterDto;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> findByFilter(LoanFilterDto loan, Pageable page);

    Page<Loan> getLoansByBook(Book book, Pageable pageable);

    List<Loan> getAllLateLoans();
}
