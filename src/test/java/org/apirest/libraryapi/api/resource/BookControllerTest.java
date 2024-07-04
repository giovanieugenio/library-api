package org.apirest.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apirest.libraryapi.api.dto.BookDto;
import org.apirest.libraryapi.exception.BusinessException;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.service.BookService;
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

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService bookService;

    @SneakyThrows
    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() {
        BookDto book = createNewBook();
        Book savedBook = Book.builder()
                .id(10L)
                .author("J.R.R Tolkien")
                .title("The Lord Of The Rings")
                .isbn("55475")
                .build();

        BDDMockito.given(bookService.save(BDDMockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(book);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));
    }

    private static BookDto createNewBook() {
        return BookDto.builder()
                .author("J.R.R Tolkien")
                .title("The Lord Of The Rings")
                .isbn("55475")
                .build();
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve lançar exceção ao tentar criar um novo registro")
    public void createInvalidBookTest() {
        BookDto dto = createNewBook();

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @SneakyThrows
    @Test
    @DisplayName("Lançar erro ao tentar cadastrar 'isbn' duplicado")
    public void createBookWithDuplicateIsbn() {
        BookDto dto = createNewBook();

        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.save(BDDMockito.any(Book.class)))
                .willThrow(new BusinessException("Livro com 'isbn' já cadastrado"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Livro com 'isbn' já cadastrado"));
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetailsTest() {
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(bookService.getBookById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve retornar 'recurso não encontrado'")
    public void bookNotFoundTest() {
        BDDMockito.given(bookService.getBookById(BDDMockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 4))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve deletar um livro da base de dados")
    public void deleteBookTest() {
        BDDMockito.given(bookService.getBookById(BDDMockito.anyLong()))
                .willReturn(Optional.of(Book.builder().id(1L).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve retornar recurso não encontrado quando buscar um livro da base de dados")
    public void deleteNonexistentBookTest() {
        BDDMockito.given(bookService.getBookById(BDDMockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve atualizar um livro da base de dados")
    public void updateBookTest() {
        Long id = 1L;
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book book = Book.builder()
                .id(1L)
                .author("J.R.R Tolkien")
                .title("The Lord Of The Rings")
                .isbn("55475")
                .build();
        BDDMockito.given(bookService.getBookById(id)).willReturn(Optional.of(book));
        Book updatedBook = Book.builder()
                .id(id)
                .author("J.R.R Tolkien")
                .title("The Lord Of The Rings")
                .isbn("55475")
                .build();
        BDDMockito.given(bookService.update(book)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value("55475"));
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve retornar 'recurso não encontrado' ao buscar um livro da base de dados")
    public void updateNotFoundBook() {
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given(bookService.getBookById(BDDMockito.anyLong()))
                .willReturn(Optional.empty());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve filtrar livros da base de dados")
    public void findFilterBook() {
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(bookService.findByFilter(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<>(Arrays.asList(book), PageRequest.of(0,100),1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }
}