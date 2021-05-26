package com.example.libraryapi.controller;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.libraryapi.dto.LoanDTO;
import com.example.libraryapi.exceptions.EntityNotFoundException;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.model.Loan;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

	private final LoanService loanService;
	private final BookService bookService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long create(@RequestBody LoanDTO  dto) {
		Book book = bookService
				.getBookByIsbn(dto.getIsbn())
				.orElseThrow( () -> new EntityNotFoundException("Livro não encontrado para o isbn"));

		Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();
		
		entity = loanService.save(entity);
		return entity.getId();
	}
	
}
