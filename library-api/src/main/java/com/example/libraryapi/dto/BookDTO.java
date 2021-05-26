package com.example.libraryapi.dto;

import org.hibernate.validator.constraints.NotEmpty;

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
public class BookDTO {
	
	private Long id;
	
	@NotEmpty(message="Preenchimento obrigatório")
	private String tittle;
	
	@NotEmpty(message="Preenchimento obrigatório")
	private String author;
	
	@NotEmpty(message="Preenchimento obrigatório")
	private String isbn;
}
