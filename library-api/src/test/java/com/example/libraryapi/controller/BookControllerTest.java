package com.example.libraryapi.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.example.libraryapi.dto.BookDTO;
import com.example.libraryapi.exceptions.BusinnesException;
import com.example.libraryapi.exceptions.EntityNotFoundException;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

	static String BOOK_API = "/api/books";

	@Autowired
	MockMvc mvc;

	@MockBean
	BookService service;

	@Test
	@DisplayName("Deve criar um livro com sucesso.")
	public void createBookTest() throws Exception {

		BookDTO dto = BookDTO.builder().tittle("Artur").author("Asaventuras").isbn("001").build();
		Book savedBook = Book.builder().id(10L).tittle("Artur").author("Asaventuras").isbn("001").build();

		BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

		String json = new ObjectMapper().writeValueAsString(dto);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
		 .andExpect(status().isCreated())
		 .andExpect(jsonPath("id").value(10L))
		 .andExpect(jsonPath("tittle").value(dto.getTittle()))
		 .andExpect(jsonPath("author").value(dto.getAuthor()))
		 .andExpect(jsonPath("isbn").value(dto.getIsbn()));
	}

	@Test
	@DisplayName("Deve lançar erro de validação quando não houver dados suficientes.")
	public void createInvalidBookTest() throws Exception {

		String json = new ObjectMapper().writeValueAsString(new BookDTO());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
		 .andExpect(status().isBadRequest())
		 .andExpect(jsonPath("erros").exists());
	}

	@Test
	@DisplayName("Deve lançar erro ao tentar cadastrar livro com isbn já cadastrado")
	public void createBookWithDuplicatedIsbn() throws Exception {

		BookDTO dto = createNewBookDTO();
		String json = new ObjectMapper().writeValueAsString(dto);

		String mensagemErro = "Isbn já cadastrado";
		BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinnesException(mensagemErro));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
		 .andExpect(status().isBadRequest())
		 .andExpect(jsonPath("error").exists())
		 .andExpect(jsonPath("message").value(mensagemErro));

	}
	
	
	@Test
	@DisplayName("Deve obter informações de um livro")
	public void getBookDetaisTest() throws Exception {
		
		// cenario (given)
		Long id =  1L;
		
		BookDTO bookDTO = createNewBookDTO();
		Book book = Book.builder()
					.id(id)
					.tittle(bookDTO.getTittle())
					.author(bookDTO.getAuthor())
					.isbn(bookDTO.getIsbn())
				.build();
		
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));
		
		
		// execucao (when)
		MockHttpServletRequestBuilder request = 
			MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);
		
		
		// verificacao
		mvc.perform(request)
		 .andExpect(status().isOk())
		 .andExpect(jsonPath("id").value(id))
		 .andExpect(jsonPath("tittle").value(bookDTO.getTittle()))
		 .andExpect(jsonPath("author").value(bookDTO.getAuthor()))
		 .andExpect(jsonPath("isbn").value(bookDTO.getIsbn()));	
		
	}
	
	
	@Test
	@DisplayName("Deve lançar erro ao buscar por um livro não cadastrado")
	public void bookNotFound() throws Exception {
		
		// cenario (given)
		Long id =  1L;
		
		String mensagemErro = "Livro não encontrado";
		BDDMockito.given(service.getById(Mockito.anyLong())).willThrow(new EntityNotFoundException(mensagemErro));

		// execucao (when)
		MockHttpServletRequestBuilder request = 
			MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);
		
		
		mvc.perform(request)
		 .andExpect(status().isNotFound())
		 .andExpect(jsonPath("error").exists())
		 .andExpect(jsonPath("message").value(mensagemErro));
	}
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() throws Exception {
		// cenario (given)
		Long id =  1L;
		
		BDDMockito.given(service.getById(1L)).willReturn(Optional.of(Book.builder().id(1L).build()));
		
		// execucao (when)
		MockHttpServletRequestBuilder request = 
			MockMvcRequestBuilders.delete(BOOK_API.concat("/" + id));
	
		
		// verificacao
		mvc.perform(request)
		.andExpect(status().isNoContent());
		
	}
	
	@Test
	@DisplayName("Deve retornar resource not foud quando não encontrar o livro para deletar")
	public void deleteInexistentBookTest() throws Exception {
		// cenario (given)
		Long id =  1L;
		
		String mensagemErro = "Livro não encontrado";
		BDDMockito.given(service.getById(Mockito.anyLong())).willThrow(new EntityNotFoundException(mensagemErro));

		// execucao (when)
		MockHttpServletRequestBuilder request = 
			MockMvcRequestBuilders.delete(BOOK_API.concat("/" + id));
		
		mvc.perform(request)
		 .andExpect(status().isNotFound())
		 .andExpect(jsonPath("error").exists())
		 .andExpect(jsonPath("message").value(mensagemErro));
	}
	
	
	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() throws Exception{
		// cenario (given)
		Long id =  1L;
		
		// json com os novos dados do livro a ser atualizado
		String json = new ObjectMapper().writeValueAsString(createNewBookDTO()); 
			
		
		// livro retornado na busca pelo getById(id) (livro que está no banco)
		Book updatingBook = Book.builder()
				.id(1L)
				.tittle("some title")
				.author("some author")
				.isbn("321")
			.build();
		
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));
		
		// livro com os dados atualizados
		Book updatedBook = Book.builder().id(id).tittle("Artur").author("As aventuras").isbn("321").build();
		BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);
		
		
		// execucao (when)
		MockHttpServletRequestBuilder request =
			MockMvcRequestBuilders.put(BOOK_API.concat("/" + id))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		
		mvc.perform(request)
		 .andExpect(status().isOk())
		 .andExpect(jsonPath("id").value(id))
		 .andExpect(jsonPath("tittle").value(createNewBookDTO().getTittle()))
		 .andExpect(jsonPath("author").value(createNewBookDTO().getAuthor()))
		 .andExpect(jsonPath("isbn").value("321"));
		
	}
	
	@Test
	@DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente")
	public void updateInexistentBookTest() throws Exception {
		// cenario (given)
		Long id =  1L;
		
		String mensagemErro = "Livro não encontrado";
		BDDMockito.given(service.getById(Mockito.anyLong())).willThrow(new EntityNotFoundException(mensagemErro));

		// execucao (when)
		MockHttpServletRequestBuilder request = 
			MockMvcRequestBuilders.put(BOOK_API.concat("/" + id));
		
		mvc.perform(request)
		 .andExpect(status().isNotFound())
		 .andExpect(jsonPath("error").exists())
		 .andExpect(jsonPath("message").value(mensagemErro));
		
	}
	
	@Test
	@DisplayName("Deve filtrar livros")
	public void findBooksTest() throws Exception{
		// cenario (given)
		Long id =  1L;
		
		Book book = Book.builder()
				.id(id)
				.tittle(createNewBookDTO().getTittle())
				.author(createNewBookDTO().getAuthor())
				.isbn(createNewBookDTO().getIsbn())
			.build();
		
		BDDMockito.given( service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)) )
        .willReturn( new PageImpl<Book>( Arrays.asList(book), PageRequest.of(0,100), 1 ));

		String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTittle(), book.getAuthor());
		
		// execucao (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);
		
	    mvc
        .perform( request )
        .andExpect( status().isOk() )
        .andExpect( jsonPath("content", Matchers.hasSize(1)))
        .andExpect( jsonPath("totalElements").value(1) )
        .andExpect( jsonPath("pageable.pageSize").value(100) )
        .andExpect( jsonPath("pageable.pageNumber").value(0))
        ;
		
	}
	

	private BookDTO createNewBookDTO() {
		return BookDTO.builder().tittle("Artur").author("As aventuras").isbn("001").build();
	}

}
