package com.example.libraryapi.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.libraryapi.dto.BookDTO;
import com.example.libraryapi.dto.LoanDTO;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.model.Loan;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
@Slf4j
public class BookController {
	
	private final BookService service;
	private final LoanService loanService;
	private final ModelMapper modelMapper;
	
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Creates ebook")
	public BookDTO create(@RequestBody @Valid BookDTO dto) {
		log.info("Creating a Book for isbn: {} " , dto.getIsbn());
		
		Book entity = modelMapper.map(dto, Book.class);
	
		entity = service.save(entity);
		
		return modelMapper.map(entity , BookDTO.class);
	}
	
	@GetMapping("{id}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Obtains a book details by id")
	public BookDTO get(@PathVariable Long id) {
		
		log.info("Obtaining details for book id : {} ", id);
		
	    return service
                .getById(id)
                .map( book -> modelMapper.map(book, BookDTO.class)  )
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
	}
	
	
	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation("Deletes a book by id")
	@ApiResponses({
		@ApiResponse( code = 204 , message = "Book succesfully deleted")
	})
	public void delete(@PathVariable Long id) {
		
		log.info("Deleting book of id : {} ", id);
		
		Book book = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
		 service.delete(book);
	}
	
	@PutMapping("{id}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Updates a book")
	public BookDTO update(@PathVariable Long id ,@RequestBody @Valid BookDTO dto) {
		
		log.info("Updating book of id : {} ", id);
		
		return service.getById(id).map( book -> {
	            book.setAuthor(dto.getAuthor());
	            book.setTittle(dto.getTittle());
	            book = service.update(book);
	            return modelMapper.map(book, BookDTO.class);

	     }).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
	}
	
	
	@GetMapping
	@ApiOperation("Find books by params")
	public Page<BookDTO> find(BookDTO bookDTO , Pageable pageRequest){
		Book filter = modelMapper.map(bookDTO , Book.class);
		Page<Book> result = service.find(filter, pageRequest);
		List<BookDTO> list = result.getContent()
			.stream()
			.map(entity -> modelMapper.map(entity , BookDTO.class)) 
			.collect(Collectors.toList());
		return new PageImpl<BookDTO>(list , pageRequest , result.getTotalElements() );
	}
	
	@GetMapping("/{id}/loans")
	public Page<LoanDTO> loansByBook(@PathVariable Long id , Pageable pageRequest) {
		Book book = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Page<Loan> result = loanService.findLoansByBook(book , pageRequest);
		List<LoanDTO> list = result.getContent()
				.stream()
				.map(loan -> {
					BookDTO bookDTO = modelMapper.map(loan.getBook(), BookDTO.class);
					LoanDTO loanDTO = modelMapper.map(loan , LoanDTO.class);
					loanDTO.setBook(bookDTO);
					return loanDTO;
				}).collect(Collectors.toList());
		
		return new PageImpl<LoanDTO>(list , pageRequest , result.getTotalElements() );
	}
}
