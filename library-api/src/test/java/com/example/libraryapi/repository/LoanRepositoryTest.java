package com.example.libraryapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.libraryapi.model.Book;
import com.example.libraryapi.model.Loan;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {
	
	@Autowired
	TestEntityManager entityManager;
	
	@Autowired
	LoanRepository repository;
	
	@Test
	@DisplayName("deve verificar se existe empréstimo não devolvido para o livro.")
	public void existsByBookAndNotReturnedTest() {
		
		// cenário
		Book book = BookRepositoryTest.createdNewBook("123");	
		entityManager.persist(book);
		
		Loan loan = Loan.builder()
				.book(book)
				.customer("Fulano")
				.loanDate(LocalDate.now())
				.build();
		entityManager.persist(loan);
		
		
		//execucao
		boolean exists = repository.existsByBookAndNotReturned(book);
		assertThat(exists).isTrue();
	}
}
