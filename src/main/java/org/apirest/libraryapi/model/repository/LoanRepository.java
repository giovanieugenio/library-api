package org.apirest.libraryapi.model.repository;

import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query(value = " select case when ( count( l.id ) > 0 ) then true else false end " +
            " from Loan l where l.book = :book and ( l.returned is null or l.returned is false ) ")
    boolean existsBookAlreadyLoaned(@Param("book") Book book);

    @Query(value = " select l from Loan as l join l.book as b where b.isbn = :isbn or l.customer =:customer ")
    Page<Loan> findByBookIsbnOrCustomer(@Param("isbn") String isbn, @Param("customer") String customer, Pageable pageable);

    Page<Loan> findByBook(Book book, Pageable pageable);

    @Query(" select l from Loan l where l.loanDate <= :closeToExpiration and ( l.returned is null or l.returned is false ) ")
    List<Loan> findByLateLoans(@Param("closeToExpiration") LocalDate closeToExpiration);
}
