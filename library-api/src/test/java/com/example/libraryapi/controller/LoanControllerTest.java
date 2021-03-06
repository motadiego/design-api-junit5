package com.example.libraryapi.controller;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.hamcrest.Matchers;
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

import com.example.libraryapi.dto.LoanDTO;
import com.example.libraryapi.dto.LoanFilterDTO;
import com.example.libraryapi.dto.ReturnedLoanDTO;
import com.example.libraryapi.exceptions.BusinnesException;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.model.Loan;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;
import com.example.libraryapi.service.LoanServiceTest;
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
	@DisplayName("Deve realizar um empr??stimo")
	public void createLoanTest() throws Exception {
		// cenario
		LoanDTO loanDTO = creanteNewLoanDTO();
		loanDTO.setEmail("customer@email.com");
		String json = new ObjectMapper().writeValueAsString(loanDTO);
		
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
		 LoanDTO dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
	     String json = new ObjectMapper().writeValueAsString(dto);

	     BDDMockito.given( bookService.getBookByIsbn("123") ).willReturn( Optional.empty() );

	     MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
	                .accept(MediaType.APPLICATION_JSON)
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(json);

	      mvc.perform( request )
	         .andExpect( status().isNotFound())
	         .andExpect(jsonPath("error").exists())
	  		 .andExpect(jsonPath("message").value("Livro n??o encontrado para o isbn informado."));
	}
	
	
	

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro  emprestado.")
	public void loanedBookErrorCreateLoanTest() throws Exception  {
		// cenario
		LoanDTO dto = creanteNewLoanDTO();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		Book book = Book.builder().id(1L).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
		
		String mensagemErro = "Livro j?? emprestado";
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
	
	@Test
	@DisplayName("Deve retornar um empr??stimo")
	public void returnBookTest() throws Exception {
		// cenario
		ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		Loan loan = Loan.builder().id(1L).build();
		BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));
		
		
		// execucao
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders.patch(LOAN_API + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		
		// verificacao
		mvc.perform(request).andExpect(status().isOk());
		
		Mockito.verify(loanService , Mockito.times(1)).update(loan);
	}
	
	
	@Test
	@DisplayName("Deve retornar 404 quando tentar devolver um empr??stimo inexistente")
	public void returnInexistentBookTest() throws Exception {
		//cen??rio
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        mvc.perform(
        		MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect( status().isNotFound())
         .andExpect(jsonPath("error").exists())
         .andExpect(jsonPath("message").value("Empr??stimo n??o encontrado."));
   }
	
	
	@Test
	@DisplayName("Deve filtrar empr??stimos")
	public void findLoansTest() throws Exception{
		// cenario (given)
		Long id =  1L;
		
		Loan loan = LoanServiceTest.createLoan();
		loan.setId(id);
	
		Book book = Book.builder().id(1L).isbn("321").build();
		loan.setBook(book);
		
		BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
        .willReturn( new PageImpl<Loan>( Arrays.asList(loan), PageRequest.of(0,10), 1 ));

		String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10", book.getIsbn(), loan.getCustomer() );
		
		// execucao (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(LOAN_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);
		
	    mvc
        .perform( request )
        .andExpect( status().isOk() )
        .andExpect( jsonPath("content", Matchers.hasSize(1)))
        .andExpect( jsonPath("totalElements").value(1) )
        .andExpect( jsonPath("pageable.pageSize").value(10) )
        .andExpect( jsonPath("pageable.pageNumber").value(0));
	}
}
