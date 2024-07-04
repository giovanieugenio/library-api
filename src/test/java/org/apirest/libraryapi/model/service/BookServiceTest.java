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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        return Book.builder()
                .title("As Crônicas de Nárnia")
                .author("C.S Lewis")
                .isbn("4963")
                .build();
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

    @Test
    @DisplayName("Deve buscar um livro pelo id")
    public void getBookByIdTest(){
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> findBook = service.getBookById(id);

        Assertions.assertThat(findBook.isPresent()).isTrue();
        Assertions.assertThat(findBook.get().getId()).isEqualTo(id);
        Assertions.assertThat(findBook.get().getTitle()).isEqualTo(book.getTitle());
        Assertions.assertThat(findBook.get().getAuthor()).isEqualTo(book.getAuthor());
        Assertions.assertThat(findBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar 'recurso não econtrado' ao buscar um livro pelo id")
    public void getBookNotFoundTest(){
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> findBook = service.getBookById(id);

        Assertions.assertThat(findBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro pelo id na base de dados")
    public void deleteBookByIdTest(){
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        assertDoesNotThrow(()-> service.delete(book));

        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar deletar um livro na base de dados")
    public void deleteInvalidBookTest(){
        Book book = new Book();

        assertThrows(IllegalArgumentException.class, ()-> service.delete(book));

        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro na base de dados")
    public void updateBookTest(){
        Long id = 1L;
        Book book = Book.builder().id(id).build();

        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        Mockito.when(repository.save(book)).thenReturn(updatedBook);

        Book book2 = service.update(book);

        Assertions.assertThat(book2.getId()).isEqualTo(updatedBook.getId());
        Assertions.assertThat(book2.getTitle()).isEqualTo(updatedBook.getTitle());
        Assertions.assertThat(book2.getAuthor()).isEqualTo(updatedBook.getAuthor());
        Assertions.assertThat(book2.getIsbn()).isEqualTo(updatedBook.getIsbn());

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar atualizar um livro na base de dados")
    public void updateInvalidBookTest(){
        Book book = new Book();

        assertThrows(IllegalArgumentException.class, ()-> service.update(book));

        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve filtrar um livro pela propriedade")
    public void filterBookTest(){
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<>(list, pageRequest, 1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

        Page<Book> result = service.findByFilter(book, pageRequest);

        Assertions.assertThat(result.getTotalElements()).isEqualTo(0);
        Assertions.assertThat(result.getContent()).isEqualTo(list);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro pelo 'isbn'")
    public void getBookByIsbnTest(){
        String isbn = "120";

        Mockito.when(repository.findByIsbn(isbn)).thenReturn(
                Optional.of(Book.builder()
                        .id(1L)
                        .isbn(isbn)
                        .build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        Assertions.assertThat(book.isPresent()).isTrue();
        Assertions.assertThat(book.get().getId()).isEqualTo(1L);
        Assertions.assertThat(book.get().getIsbn()).isEqualTo(isbn);

        Mockito.verify(repository, Mockito.times(1)).findByIsbn(isbn);
    }
}
