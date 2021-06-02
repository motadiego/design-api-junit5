package com.example.libraryapi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.libraryapi.dto.LoanFilterDTO;
import com.example.libraryapi.model.Loan;

public interface LoanService {
	Loan save(Loan loan);

	Optional<Loan> getById(Long id);

	Loan update(Loan loan);

	Page<Loan> find(LoanFilterDTO filter, Pageable pageRequest);
}
