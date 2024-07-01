package org.apirest.libraryapi.model.repository;

import org.apirest.libraryapi.model.entity.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando encontrar 'isbn' na base de dados")
    public void returnTrueWhenFindIsbn(){
        String isbn = "4963";
        createNewBook(isbn);

        boolean exists = repository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isTrue();
    }

    private Book createNewBook(String isbn) {
        return entityManager.persist(Book.builder()
                .title("As Crônicas de Nárnia")
                .author("C.S Lewis")
                .isbn(isbn).build());
    }

    @Test
    @DisplayName("Deve retornar falso quando não encontrar 'isbn' na base de dados")
    public void returnFalseWhenDoesntFindIsbn(){
        String isbn = "";
        entityManager.persist(createNewBook(isbn));

        boolean exists = repository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar um livro pelo id na base de dados")
    public void findByIdTest(){
        String isbn = "120";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        Optional<Book> foundBook = repository.findById(book.getId());

        Assertions.assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro na base de dados")
    public void saveBookTest(){
        Book book = createNewBook("120");

        Book bookSave = repository.save(book);

        Assertions.assertThat(bookSave.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro da base de dados")
    public void deleteBookTest(){
        Book book = createNewBook("120");
        entityManager.persist(book);
        Book foundBook = entityManager.find(Book.class, book.getId());

        repository.delete(foundBook);

        foundBook = entityManager.find(Book.class, book.getId());

        Assertions.assertThat(foundBook).isNull();

    }
}
