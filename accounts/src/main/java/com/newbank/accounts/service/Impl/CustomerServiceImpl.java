package com.newbank.accounts.service.Impl;

import com.newbank.accounts.dto.AccountsDto;
import com.newbank.accounts.dto.CardsDto;
import com.newbank.accounts.dto.CustomerDetailsDto;
import com.newbank.accounts.dto.LoansDto;
import com.newbank.accounts.entity.Accounts;
import com.newbank.accounts.entity.Customer;
import com.newbank.accounts.exception.ResourceNotFoundException;
import com.newbank.accounts.mapper.AccountsMapper;
import com.newbank.accounts.mapper.CustomerMapper;
import com.newbank.accounts.repository.AccountsRepository;
import com.newbank.accounts.repository.CustomerRepository;
import com.newbank.accounts.service.ICustomerService;
import com.newbank.accounts.service.client.CardsFeignClient;
import com.newbank.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements ICustomerService {
    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private LoansFeignClient loansFeignClient;
    private CardsFeignClient cardsFeignClient;

    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(correlationId, mobileNumber);
        customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(correlationId, mobileNumber);
        customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());

        return customerDetailsDto;

    }
}
