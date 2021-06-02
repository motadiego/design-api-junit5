package com.example.libraryapi.dto;

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
public class LoanDTO {
	
	private Long id;
	private String isbn;
	
	// cliente
	private String customer;
	private BookDTO book;
}
