package org.apirest.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apirest.libraryapi.api.dto.LoanDto;
import org.apirest.libraryapi.api.dto.LoanFilterDto;
import org.apirest.libraryapi.api.dto.ReturnedLoanDto;
import org.apirest.libraryapi.exception.BusinessException;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.entity.Loan;
import org.apirest.libraryapi.model.service.LoanServiceTest;
import org.apirest.libraryapi.service.BookService;
import org.apirest.libraryapi.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @MockBean
    BookService bookService;

    @MockBean
    LoanService loanService;

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    @DisplayName("Deve realizar um empréstimo")
    public void createLoanTest(){
        LoanDto dto = LoanDto.builder()
                .isbn("001")
                .email("bob@gmail.com")
                .customer("Bob")
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);
        Book book = Book.builder()
                .id(1L)
                .isbn("001")
                .build();

        BDDMockito.given(bookService.getBookByIsbn("001")).willReturn(Optional.of(book));
        Loan loan = Loan.builder()
                .id(1L)
                .customer("Bob")
                .book(book)
                .loanDate(LocalDate.now()).build();

        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
            .andExpect(content().string("1"));
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve retornar erro ao tentar fazer um empréstimo de um livro inexistente")
    public void invalidIsbnLoanTest(){
        LoanDto dto = LoanDto.builder()
                .isbn("001")
                .customer("Bob")
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("001")).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Isbn non existent"));
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve retornar erro ao tentar fazer um empréstimo de um livro ja emprestado")
    public void loanedBookErrorTest(){
        LoanDto dto = LoanDto.builder()
                .isbn("001")
                .customer("Bob")
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder()
                .id(1L)
                .isbn("001")
                .build();
        BDDMockito.given(bookService.getBookByIsbn("001")).willReturn(Optional.of(book));
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willThrow(
                new BusinessException("Book already loaned")
        );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve retornar um livro")
    public void returnBookTest(){
        ReturnedLoanDto dto = ReturnedLoanDto.builder()
                .returned(true)
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Loan loan = Loan.builder().id(1L).build();
        BDDMockito.given(loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.of(loan));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isOk());

        Mockito.verify(loanService, Mockito.times(1)).update(loan);
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve retornar recurso não encontrado quando tentar devolver um livro inexistente")
    public void returnNotFoundBookTest(){
        ReturnedLoanDto dto = ReturnedLoanDto.builder()
                .returned(true)
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve filtrar emprestimos de livros da base de dados")
    public void findFilterLoan() {
        Long id = 1L;

        Loan loan = LoanServiceTest.createLoan();
        loan.setId(id);
        Book book = Book.builder().id(id).isbn("10").build();
        loan.setBook(book);

        BDDMockito.given(loanService.findByFilter(Mockito.any(LoanFilterDto.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<>(Arrays.asList(loan), PageRequest.of(0,10),1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                book.getIsbn(), loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }
}
