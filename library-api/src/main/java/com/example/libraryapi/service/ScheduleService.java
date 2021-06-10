package com.example.libraryapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.libraryapi.model.Loan;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;



@Service
@RequiredArgsConstructor
@Log4j
public class ScheduleService {
	
	/**
	 * Usar o site http://www.cronmaker.com/ para gerar CRON
	 */
	
	private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";
	
	private final LoanService loanService;
	private final EmailService emailService;
	
	@Value("${application.mail.lateloans.message}")
	private String message;
	
	
	@Scheduled( cron = CRON_LATE_LOANS)
	public void sendMailLateLoans() {
		List<Loan> allLateLoans = loanService.getAllLateLoans();
		List<String> mailsList = allLateLoans.stream()
				.map(l -> l.getCustomerEmail())
				.collect(Collectors.toList());
		
		if(existeEmailParaEnvio(mailsList)) {
			emailService.sendMails(message , mailsList);
			log.info("EMAILS ENVIADOS");
		}
	}


	private boolean existeEmailParaEnvio(List<String> mailsList) {
		return mailsList.size() > 0;
	}
	
}
