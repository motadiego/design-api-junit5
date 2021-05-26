package com.example.libraryapi.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

/***
 * Empréstimo 
 */

public class Loan {

	private Long id;
	
	private String customer;
	
	private Book book;
	
	private LocalDate loanDate;
	
	private boolean returned;
}
