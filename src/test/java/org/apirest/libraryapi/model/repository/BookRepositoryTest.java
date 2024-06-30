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
        entityManager.persist(Book.builder()
                .title("As Crônicas de Nárnia")
                .author("C.S Lewis")
                .isbn(isbn).build());

        boolean exists = repository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando não encontrar 'isbn' na base de dados")
    public void returnFalseWhenDoesntFindIsbn(){
        String isbn = "";
        entityManager.persist(Book.builder()
                .title("As Crônicas de Nárnia")
                .author("C.S Lewis")
                .isbn(isbn).build());

        boolean exists = repository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isTrue();
    }
}
