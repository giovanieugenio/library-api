package org.apirest.libraryapi.service;

import lombok.RequiredArgsConstructor;
import org.apirest.libraryapi.model.entity.Loan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

    private final LoanService loanService;

    private final EmailService emailService;

    @Value("${application.mail.lateloans.message}")
    private String message;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendMailToLateLoans(){
        List<Loan> listLateLoans = loanService.getAllLateLoans();
        List<String> mailsList = listLateLoans.stream().map(Loan::getCustomer).toList();
        emailService.sendMails(message, mailsList);
    }

}
