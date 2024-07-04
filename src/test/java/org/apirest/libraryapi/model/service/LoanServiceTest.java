package org.apirest.libraryapi.model.service;

import org.apirest.libraryapi.api.dto.LoanFilterDto;
import org.apirest.libraryapi.exception.BusinessException;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.entity.Loan;
import org.apirest.libraryapi.model.repository.LoanRepository;
import org.apirest.libraryapi.service.LoanService;
import org.apirest.libraryapi.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new LoanServiceImpl(repository);
    }

    public static Loan createLoan(){
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .build();
        return Loan.builder()
                .book(book)
                .customer("Bob")
                .loanDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Deve salvar um emprestimo na base de dados")
    public void saveLoadTest(){
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .build();
        Loan loan = Loan.builder()
                .book(book)
                .customer("Bob")
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(id)
                .book(book)
                .customer("Bob")
                .loanDate(LocalDate.now())
                .build();

        Mockito.when(repository.existsBookAlreadyLoaned(book)).thenReturn(false);
        Mockito.when(repository.save(loan)).thenReturn(savedLoan);
        Loan loan1 = service.save(loan);

        Assertions.assertThat(loan1.getId()).isEqualTo(savedLoan.getId());
        Assertions.assertThat(loan1.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        Assertions.assertThat(loan1.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
        Assertions.assertThat(loan1.getCustomer()).isEqualTo(savedLoan.getCustomer());
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer um emprestimo de um livro ja emprestado")
    public void loanedBookSaveTest(){
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .build();
        Loan loan = Loan.builder()
                .book(book)
                .customer("Bob")
                .loanDate(LocalDate.now())
                .build();
        Mockito.when(repository.existsBookAlreadyLoaned(book)).thenReturn(true);

        Throwable e = catchThrowable(()-> service.save(loan));

        Assertions.assertThat(e).isInstanceOf(BusinessException.class).hasMessage("Book already loaned");
        Mockito.verify(repository, Mockito.never()).save(loan);
    }

    @Test
    @DisplayName("Deve obter informações de um emprestimo pelo id")
    public void getLoanDetailsTest(){
        Long id = 1L;

        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = service.getById(id);

        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get().getId()).isEqualTo(id);
        Assertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        Assertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());
    }

    @Test
    @DisplayName("Deve obter informações de um emprestimo pelo id")
    public void updateLoanTest(){
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);

        Mockito.when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        Assertions.assertThat(updatedLoan.getReturned()).isTrue();

        Mockito.verify(repository).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar um livro pela propriedade")
    public void filterBookTest(){
        LoanFilterDto dto = LoanFilterDto.builder()
                .isbn("120")
                .customer("Bob")
                .build();
        Loan loan = createLoan();
        loan.setId(1L);

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Loan> list = Arrays.asList(loan);
        Page<Loan> page = new PageImpl<>(list, pageRequest, list.size());
        Mockito.when(repository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class)))
        .thenReturn(page);

        Page<Loan> result = service.findByFilter(dto, pageRequest);

        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(list);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }
}
