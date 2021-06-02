package com.example.libraryapi.service.imp;

import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.libraryapi.exceptions.BusinnesException;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.repository.BookRepository;
import com.example.libraryapi.service.BookService;

@Service
public class BookServiceImpl implements BookService {
	
	private BookRepository repository;

	public BookServiceImpl(BookRepository repository) {
		this.repository = repository;;
	}
	
	
	@Override
	public Book save(Book book) {
		if(repository.existsByIsbn(book.getIsbn())) {
			throw new BusinnesException("Isbn já cadastrado");
		}
		
		return repository.save(book);
	}


	@Override
	public Optional<Book> getById(Long id) {
		return repository.findById(id);
	}


	@Override
	public void delete(Book book) {
		if(book == null || book.getId() == null) {
			throw new IllegalArgumentException("Livro não pode ser null");
		}
		
		this.repository.delete(book);
	}


	@Override
	public Book update(Book book) {
		if(book == null || book.getId() == null) {
			throw new IllegalArgumentException("Livro não pode ser null");
		}
		
		return this.repository.save(book);
	}


	@Override
	public Page<Book> find(Book filter, Pageable pageRequest) {
		Example<Book> example = Example.of(filter ,
					ExampleMatcher
						.matching()
						.withIgnoreCase()
						.withIgnoreNullValues()
						.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
		);
		
		return this.repository.findAll(example, pageRequest);
	}


	@Override
	public Optional<Book> getBookByIsbn(String isbn) {
		return this.repository.findByIsbn(isbn);
	}
	
	
	
}
