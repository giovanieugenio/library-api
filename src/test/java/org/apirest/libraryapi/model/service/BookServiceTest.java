package org.apirest.libraryapi.model.service;

import org.apirest.libraryapi.exception.BusinessException;
import org.apirest.libraryapi.model.entity.Book;
import org.apirest.libraryapi.model.repository.BookRepository;
import org.apirest.libraryapi.service.BookService;
import org.apirest.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(
                Book.builder()
                        .id(1L)
                        .title("As Crônicas de Nárnia")
                        .author("C.S Lewis")
                        .isbn("4963").build()
        );

        Book saveBook = service.save(book);

        Assertions.assertThat(saveBook.getId()).isNotNull();
        Assertions.assertThat(saveBook.getTitle()).isEqualTo("As Crônicas de Nárnia");
        Assertions.assertThat(saveBook.getAuthor()).isEqualTo("C.S Lewis");
        Assertions.assertThat(saveBook.getIsbn()).isEqualTo("4963");

    }

    private static Book createValidBook() {
        return Book.builder().title("As Crônicas de Nárnia").author("C.S Lewis").isbn("4963").build();
    }

    @Test
    @DisplayName("Deve lançar um erro ao tentar cadastrar um livro com 'isbn' duplicado")
    public void tryToSaveBookWithDuplicatedIsbnTest(){
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable ex = Assertions.catchThrowable(()-> service.save(book));

        Assertions.assertThat(ex)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Livro com 'isbn' já cadastrado");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

}
