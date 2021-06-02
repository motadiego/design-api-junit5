package com.example.libraryapi.service;

import java.util.Optional;

import com.example.libraryapi.model.Loan;

public interface LoanService {
	Loan save(Loan loan);

	Optional<Loan> getById(Long id);

	Loan update(Loan loan);
}
