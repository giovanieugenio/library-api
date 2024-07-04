package org.apirest.libraryapi.model.repository;

import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.entity.Loan;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.apirest.libraryapi.model.repository.BookRepositoryTest.createNewBook;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LoanRepository repository;

    public Loan createAndPersistLoan(LocalDate localDate){
        Book book = createNewBook("120");
        entityManager.persist(book);
        Loan loan = Loan.builder()
                .book(book)
                .customer("Bob")
                .loanDate(localDate)
                .build();
        entityManager.persist(loan);
        return loan;
    }

    @Test
    @DisplayName("Deve verificar se um livro ja está emprestado")
    public void existsBookAlreadyLoaned(){
        Loan loan = createAndPersistLoan(LocalDate.now());
        Book book = loan.getBook();

        entityManager.persist(book);
        entityManager.persist(loan);

        boolean exists = repository.existsBookAlreadyLoaned(book);

        Assertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
    public void findByBookIsbnOrCustomer() {
        Loan loan = createAndPersistLoan(LocalDate.now());

        Page<Loan> result = repository.findByBookIsbnOrCustomer("120", "Bob", PageRequest.of(0, 10));

        Assertions.assertThat(result.getContent()).contains(loan);
        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve obter empréstimos cujo a data está proximo ao vencimento")
    public void findByLateLoansTest() {
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = repository.findByLateLoans(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Deve retornar vazio quando os emprestimos não estiverem sido retornados")
    public void findByLateNotReturnedLoansTest() {
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = repository.findByLateLoans(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).isEmpty();
    }
}
