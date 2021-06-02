package com.example.libraryapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.libraryapi.model.Book;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {
	
	@Autowired
	TestEntityManager entityManager;
	
	@Autowired
	BookRepository bookRepository;
	
	@Test
	@DisplayName("Deve retornar TRUE quando existir um  livro na base com isbn informado")
	public void returnTrueWhenIsbnExists() {
		// cenario
		String  isbn = "123";
		Book book = createdNewBook(isbn);
		entityManager.persist(book);
	
		
		//execucao
		boolean exists = bookRepository.existsByIsbn(isbn);
		
		// verificacao
		assertThat(exists).isTrue();
		
	}
	
	
	@Test
	@DisplayName("Deve retornar FALSE quando não existir um  livro na base com isbn informado")
	public void returnFalseWhenIsbnDoesntExists() {
		// cenario
		String  isbn = "123";
		
		//execucao
		boolean exists = bookRepository.existsByIsbn(isbn);
		
		// verificacao
		assertThat(exists).isFalse();
	}
	
	
	@Test
	@DisplayName("Deve obter um livro por id")
	public void findByIdTest() {
		// cenario
		String  isbn = "123";
		Book book = createdNewBook(isbn);
		entityManager.persist(book);
		
		// execucao
		Optional<Book> foundBook = bookRepository.findById(book.getId());
		
		// verificacao
		assertThat(foundBook.isPresent()).isTrue();
	}
	
	
	public static Book createdNewBook(String isbn) {
		return Book.builder().author("Fulano").tittle("As aventuras").isbn(isbn).build();
	}
	
	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBook() {
		// cenario
		String  isbn = "123";
		Book book = createdNewBook(isbn);
		
		// execucao
		Book savedBook = bookRepository.save(book);
		
		// verificacao
		assertThat(savedBook.getId()).isNotNull();
	}
	
	@Test
	@DisplayName("Deve excluir um livro")
	public void deleteBook() {
		// cenario
		String  isbn = "123";
		Book book = createdNewBook(isbn);
		entityManager.persist(book);
	
		Book bookFound = entityManager.find(Book.class, book.getId());
		
		// execucao
		bookRepository.delete(bookFound);
		Book bookDeleted =  entityManager.find(Book.class, book.getId());
		
		// verificacao
		assertThat(bookDeleted).isNull();
	}
	
}
