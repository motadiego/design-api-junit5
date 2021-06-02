package com.example.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

import com.example.libraryapi.dto.LoanFilterDTO;
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
		
		Loan savingLoan =  Loan.builder()
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
	
	@Test
	@DisplayName("Deve obter as informações de um empréstimo pelo ID")
	public void getLoanDetaisTest() {
		
		// cenario
		Long id = 1L;
		Loan loan = createLoan();
		loan.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));
	
		
		// execucao
		Optional<Loan> foundLoan = service.getById(id);
		
		
		//verificacao
		assertThat(foundLoan.isPresent()).isTrue();
		assertThat(foundLoan.get().getId()).isEqualTo(loan.getId());
		assertThat(foundLoan.get().getCustomer()).isEqualTo(loan.getCustomer());
		assertThat(foundLoan.get().getBook()).isEqualTo(loan.getBook());
		assertThat(foundLoan.get().getLoanDate()).isEqualTo(loan.getLoanDate());
		
		verify(repository , Mockito.times(1)).findById(id);
	}
	
	@Test
	@DisplayName("Deve atualizar um empréstimo")
	public void updateLoanTest() {
		
		// cenario
		Loan loan = createLoan();
		loan.setReturned(true);

		Mockito.when(repository.save(loan)).thenReturn(loan);
		
		// execucao
		Loan updatedLoan = service.update(loan);
		
		
		// verificacao
		assertThat(updatedLoan.isReturned()).isTrue();

		verify(repository , Mockito.times(1)).save(loan);
		
	}

	
	public static Loan createLoan() {
		Book book = Book.builder().id(1l).build();
		String customer = "Fulano";

		return Loan.builder()
				.book(book)
				.customer(customer)
				.loanDate(LocalDate.now())
			.build();
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Deve filtrar empréstimos pelas propriedades")
	public void findLoanTest() {
		
		// cenario
		LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
		
		Loan loan = createLoan();
		loan.setId(1L);
		
		PageRequest pageRequest = PageRequest.of(0, 10);

		
		List<Loan> lista = Arrays.asList(loan);
		Page<Loan> page = new PageImpl<Loan>(lista , pageRequest , lista.size());
		Mockito.when(repository.findByBookIsbnOrCustomer(
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.any(PageRequest.class)))
			.thenReturn(page);
		
		// execucao
		Page<Loan> result = service.find(loanFilterDTO, pageRequest);
		
		
		
		//verficacao
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(lista);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
	}
}
