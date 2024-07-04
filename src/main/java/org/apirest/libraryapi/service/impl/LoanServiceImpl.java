package org.apirest.libraryapi.service.impl;

import org.apirest.libraryapi.api.dto.LoanFilterDto;
import org.apirest.libraryapi.exception.BusinessException;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.entity.Loan;
import org.apirest.libraryapi.model.repository.LoanRepository;
import org.apirest.libraryapi.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {

    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository){
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if (repository.existsBookAlreadyLoaned(loan.getBook())){
            throw new BusinessException("Book already loaned");
        }
        return repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> findByFilter(LoanFilterDto loan, Pageable page) {
        return repository.findByBookIsbnOrCustomer(loan.getIsbn(), loan.getCustomer(), page);
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
        return repository.findByBook(book, pageable);
    }

    @Override
    public List<Loan> getAllLateLoans() {
        final Integer loansDays = 4;
        LocalDate closeToExpiration = LocalDate.now().minusDays(loansDays);
        return repository.findByLateLoans(closeToExpiration);
    }
}
