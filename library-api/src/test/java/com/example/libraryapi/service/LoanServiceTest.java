package com.example.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.libraryapi.exceptions.BusinnesException;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.model.Loan;
import com.example.libraryapi.repository.LoanRepository;
import com.example.libraryapi.service.imp.LoanServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {
	
	LoanService service;
	
	@MockBean
	LoanRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new LoanServiceImpl(repository);
	}
	
	
	@Test
	@DisplayName("Deve salvar um empréstimo")
	public void saveLoanTest() {
		
		// cenario
		Book book = Book.builder().id(1l).build();
		String customer = "Fulano";
		
		Loan savingLoan = Loan.builder()
				.book(book)
				.customer(customer)
				.loanDate(LocalDate.now())
				.build();
		
		Loan savedLoan = Loan.builder()
				.id(1l)
				.book(book)
				.customer(customer)
				.loanDate(LocalDate.now())
				.build();
		
		Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
		Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);
		
		//ececucao
		Loan loan = service.save(savingLoan);
		
		//verificacao
		assertThat(loan.getId()).isEqualTo(savedLoan.getId());
		assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
		assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
		assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
	}
	
	
	@Test
	@DisplayName("Deve lançar um erro de negócio ao salvar um empréstimo com livro já emprestado.")
	public void loanedBookSaveTest() {
		//cenacio
		Book book = Book.builder().id(1l).build();
		String customer = "Fulano";
		
		Loan savingLoan = Loan.builder()
				.book(book)
				.customer(customer)
				.loanDate(LocalDate.now())
				.build();
	
		
		Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(true);
		
		
		// execução
		Throwable exception = Assertions.catchThrowable(() -> service.save(savingLoan));
		
		
		// verificação
		assertThat(exception)
			.isInstanceOf(BusinnesException.class)
			.hasMessage("Livro já emprestado");
		
		
		Mockito.verify(repository, Mockito.never()).save(savingLoan);
	}
}
