package com.example.libraryapi.dto;

import javax.validation.constraints.NotEmpty;

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
	@NotEmpty
	private String isbn;
	@NotEmpty
	private String customer;
	@NotEmpty
	private String email;
	private BookDTO book;
}
