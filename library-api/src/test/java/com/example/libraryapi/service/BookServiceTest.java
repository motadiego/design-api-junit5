package com.example.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.libraryapi.exceptions.BusinnesException;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.repository.BookRepository;
import com.example.libraryapi.service.impl.BookServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {
	
	BookService service;
	
	@MockBean
	BookRepository repository;

	@BeforeEach
	public void setUp() {
		this.service = new BookServiceImpl(repository);
	}
	
	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBookTest() {
		//cenario
		Book book = createValidBook();
		
		// simulando o repository
		Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
		Mockito.when(service.save(book)).thenReturn(
				Book.builder()
					.id(1L)
					.isbn("123")
					.tittle("As aventuras")
					.author("Fulano")
				.build());
		
		
		// execucao
		Book savedBook = service.save(book);
		
		// verificacao
		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getIsbn()).isEqualTo("123");
		assertThat(savedBook.getTittle()).isEqualTo("As aventuras");
		assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
				
	}

	
	@Test
	@DisplayName("Deve lançar exceção de  negócio ao tentar cadastrar livro com isbn duplicado")
	public void shouldNotSaveBookWithDuplicatedISBN() {
		//cenario
		Book book = createValidBook();
		Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
		
		// execucao
		Throwable exception =  Assertions.catchThrowable(() -> service.save(book)); 
		
		//verificacoes
		assertThat(exception)
			.isInstanceOf(BusinnesException.class)
			.hasMessage("Isbn já cadastrado");
		
		// verificar que o metodo save nunca irá ser chamado
		Mockito.verify(repository, Mockito.never()).save(book);
	}
	
	@Test
	@DisplayName("Deve obter um livro por id")
	public void getByIdTest() {
		//cenario
		Long id = 1L;
		
		Book book = createValidBook();
		book.setId(id);
		Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));
		
		//execucao
		Optional<Book> foundBook = service.getById(id);
		
		// verificacao
		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(id);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getTittle()).isEqualTo(book.getTittle());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
	}
	
	@Test
	@DisplayName("Deve retornar vazio ao obter um livro por Id quando ele não existe na base.")
	public void bookNotFoundByIdTest() {
		 Long id = 1l;
	     Mockito.when( repository.findById(id) ).thenReturn(Optional.empty());

	     //execucao
	     Optional<Book> book = service.getById(id);

	     //verificacoes
	      assertThat( book.isPresent() ).isFalse();
	}
	
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() {
		// cenario
		Book book = Book.builder().id(1L).build();
		
		// execucao
		org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> service.delete(book) );
		
		// verificacao
		Mockito.verify(repository, Mockito.times(1)).delete(book);
	}
	
	@Test
	@DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente.")
	public void deleteInvalidBookTest(){
		// cenario
		Book book = new Book();
		
		// execucao
		Throwable exception =  Assertions.catchThrowable(() -> service.delete(book));
	
		//verificacoes
		assertThat(exception)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Livro não pode ser null");
				
		Mockito.verify(repository, Mockito.never()).delete(book);
	}
	
	
	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() {
		// cenario
		Long id = 1L;
		
		//livro a atualizar
		Book updatingBook  = Book.builder().id(id).build();
		
	    //simulacao
        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);
		
		
		// execucao
        Book book = repository.save(updatingBook);
		
		// verificacao
		assertThat(book.getId()).isEqualTo(id);
		assertThat(book.getAuthor()).isEqualTo(book.getAuthor());
		assertThat(book.getTittle()).isEqualTo(book.getTittle());
		assertThat(book.getIsbn()).isEqualTo(book.getIsbn());
	}
	
	
	@Test
	@DisplayName("Deve lançar exceção IllegalArgumentException ao tentar atualizar um livro inexistente")
	public void updateInvalidBookTest(){
		// cenario
		Book book = new Book();
		
		// execucao
		Throwable exception =  Assertions.catchThrowable(() -> service.update(book));
	
		//verificacoes
		assertThat(exception)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Livro não pode ser null");
				
		Mockito.verify(repository, Mockito.never()).save(book);
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Deve filtrar livros pelas propriedades")
	public void findBookTest() {
		
		// cenario
		Book book = createValidBook();
		
		PageRequest pageRequest = PageRequest.of(0, 10);

		
		List<Book> lista = Arrays.asList(book);
		Page<Book> page = new PageImpl<Book>(lista , pageRequest , 1);
		Mockito.when(repository.findAll(Mockito.any(Example.class) , Mockito.any(PageRequest.class)))
			.thenReturn(page);
		
		// execucao
		Page<Book> result = service.find(book, pageRequest);
		
		
		
		//verficacao
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(lista);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
	}
	
	@Test
	@DisplayName("Deve obter um livro pelo isbn")
	public void getBookByIsbnTest() {
		
		// cenario
		String isbn = "1230";
		Mockito.when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));
		
		
		//execucao
		Optional<Book> book = service.getBookByIsbn(isbn);
		
		
		//verificacacao
		assertThat(book.isPresent()).isTrue();
		assertThat(book.get().getId()).isEqualTo(1l);
		assertThat(book.get().getIsbn()).isEqualTo(isbn);
		
		verify( repository , times(1)).findByIsbn(isbn);
		
	}
	
	
	private Book createValidBook() {
		return Book.builder().isbn("123").author("Fulano").tittle("As aventuras").build();
	}
	
}
