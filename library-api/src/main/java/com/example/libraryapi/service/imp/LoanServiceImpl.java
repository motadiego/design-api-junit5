package com.example.libraryapi.service.imp;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.libraryapi.dto.LoanFilterDTO;
import com.example.libraryapi.exceptions.BusinnesException;
import com.example.libraryapi.model.Loan;
import com.example.libraryapi.repository.LoanRepository;
import com.example.libraryapi.service.LoanService;

public class LoanServiceImpl implements LoanService {

	private LoanRepository repository;
	
	public LoanServiceImpl(LoanRepository repository) {
		this.repository = repository;
	}
	
	
	@Override
	public Loan save(Loan loan) {
		if(repository.existsByBookAndNotReturned(loan.getBook())) {
			throw new BusinnesException("Livro já emprestado");
		}
		
		return repository.save(loan);
	}


	@Override
	public Optional<Loan> getById(Long id) {
		return repository.findById(id);
	}


	@Override
	public Loan update(Loan loan) {
		return repository.save(loan);
	}


	@Override
	public Page<Loan> find(LoanFilterDTO loanFilterDTO, Pageable pageable) {
		return repository.findByBookIsbnOrCustomer(loanFilterDTO.getIsbn(), loanFilterDTO.getCustomer(), pageable);
	}
	
}
