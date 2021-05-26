package com.example.libraryapi.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.example.libraryapi.dto.BookDTO;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private ModelMapper modelMapper;
	
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookDTO create(@RequestBody @Valid BookDTO dto) {
		Book entity = modelMapper.map(dto, Book.class);
	
		entity = bookService.save(entity);
		
		return modelMapper.map(entity , BookDTO.class);
	}
	
	@GetMapping("{id}")
	@ResponseStatus(HttpStatus.OK)
	public BookDTO findById(@PathVariable Long id) {
		return bookService
				.getById(id)
				.map( book -> modelMapper.map(book , BookDTO.class))
				.get();
	}
	
	
	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		Book book = bookService.getById(id).get();
		bookService.delete(book);
	}
	
	@PutMapping("{id}")
	@ResponseStatus(HttpStatus.OK)
	public BookDTO update(@PathVariable Long id , BookDTO dto) {
		Book book = bookService.getById(id).get();
		
		book.setAuthor(dto.getAuthor());
		book.setTittle(dto.getTittle());
		book = bookService.update(book);
		
		return modelMapper.map(book, BookDTO.class);
	}
	
	
	@GetMapping
	public Page<BookDTO> find(BookDTO bookDTO , Pageable pageRequest){
		Book filter = modelMapper.map(bookDTO , Book.class);
		Page<Book> result = bookService.find(filter, pageRequest);
		List<BookDTO> list = result.getContent()
			.stream()
			.map(entity -> modelMapper.map(entity , BookDTO.class)) 
			.collect(Collectors.toList());
		return new PageImpl<BookDTO>(list , pageRequest , result.getTotalElements() );
	}
}
