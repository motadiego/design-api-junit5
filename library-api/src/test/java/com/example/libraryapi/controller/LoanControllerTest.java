package com.example.libraryapi.controller;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.libraryapi.dto.LoanDTO;
import com.example.libraryapi.exceptions.BusinnesException;
import com.example.libraryapi.exceptions.EntityNotFoundException;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.model.Loan;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {
	
	static String LOAN_API = "/api/loans";
	
	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private BookService bookService;
	
	@MockBean
	private LoanService loanService;
	
	
	@Test
	@DisplayName("Deve realizar um empréstimo")
	public void createLoanTest() throws Exception {
		// cenario
		LoanDTO dto = creanteNewLoanDTO();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		Book book = Book.builder().id(1L).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
		
		Loan loan = Loan.builder().id(1L).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
		BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);
		
		// execucao
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders.post(LOAN_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		
		// verificacao
		   mvc.perform(request )
           .andExpect( status().isCreated() )
           .andExpect( content().string("1"));
	}
	
	@Test
	@DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro inexistente.")
	public void invalidIsbnCreateLoanTest() throws Exception  {
		// cenario
		LoanDTO dto = creanteNewLoanDTO();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		String mensagemErro = "Livro não encontrado para o isbn";
		BDDMockito.given(bookService.getBookByIsbn("123")).willThrow(new EntityNotFoundException(mensagemErro));
		
		// execucao
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders.post(LOAN_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
		 .andExpect(status().isNotFound())
		 .andExpect(jsonPath("error").exists())
		 .andExpect(jsonPath("message").value(mensagemErro));
	}
	
	
	

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro  emprestado.")
	public void loanedBookErrorCreateLoanTest() throws Exception  {
		// cenario
		LoanDTO dto = creanteNewLoanDTO();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		Book book = Book.builder().id(1L).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
		
		String mensagemErro = "Livro já emprestado";
		BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willThrow(new BusinnesException(mensagemErro));
		
		// execucao
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders.post(LOAN_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
		 .andExpect(status().isBadRequest())
		 .andExpect(jsonPath("error").exists())
		 .andExpect(jsonPath("message").value(mensagemErro));
	}
	
	
	public LoanDTO creanteNewLoanDTO() {
		return LoanDTO.builder().isbn("123").customer("Fulano").build();
	}
	
}
