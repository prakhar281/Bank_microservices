package com.newbank.accounts.service.Impl;

import com.newbank.accounts.constants.AccountConstants;
import com.newbank.accounts.dto.AccountsDto;
import com.newbank.accounts.dto.CustomerDto;
import com.newbank.accounts.entity.Accounts;
import com.newbank.accounts.entity.Customer;
import com.newbank.accounts.exception.CustomerAlreadyExistException;
import com.newbank.accounts.exception.ResourceNotFoundException;
import com.newbank.accounts.mapper.AccountsMapper;
import com.newbank.accounts.mapper.CustomerMapper;
import com.newbank.accounts.repository.AccountsRepository;
import com.newbank.accounts.repository.CustomerRepository;
import com.newbank.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService {
    private CustomerRepository customerRepository;
    private AccountsRepository accountsRepository;


    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        Optional<Customer> existingCustomer = customerRepository.findByMobileNumber(customer.getMobileNumber());
        if (existingCustomer.isPresent()){
            throw new CustomerAlreadyExistException("Customer with mobile number "+customerDto.getMobileNumber()+" already exists");
        }
//            customer.setCreatedAt(LocalDateTime.now());
//            customer.setCreatedBy("Anonymous");
            Customer savedCustomer = customerRepository.save(customer);
            accountsRepository.save(createAccountForCustomer(savedCustomer));

    }


    private Accounts createAccountForCustomer(Customer customer){
        Accounts accounts = new Accounts();
        accounts.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);
        accounts.setAccountNumber(randomAccNumber);
        accounts.setAccountType(AccountConstants.SAVINGS);
        accounts.setBranchAddress(AccountConstants.ADDRESS);
//        accounts.setCreatedAt(LocalDateTime.now());
//        accounts.setCreatedBy("Anonymous");
        return accounts;
    }

    /**
     * @param mobileNumber
     * @return
     */
    @Override
    public CustomerDto fetchAccountDetails(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber",mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", String.valueOf(customer.getCustomerId()))
        );
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
        return customerDto;
    }

//    {
//        "name": "Aryan Mittal",
//            "email": "armittal1@gmail.com",
//            "mobileNumber": "7782936472",
//            "accountsDto": {
//        "accountNumber": 1720865155,
//                "accountType": "Savings",
//                "branchAddress": "123 Main Street, New York"
//    }
//    }

    /**
     * @param customerDto
     * @return
     */
    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;
        Accounts accounts;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto != null && accountsDto.getAccountNumber() != null) {

            accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "accountNumber", String.valueOf(accountsDto.getAccountNumber()))
            );
            AccountsMapper.mapToAccounts(accountsDto, accounts);
            accounts = accountsRepository.save(accounts);

            Long customerId = accounts.getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "customerId", String.valueOf(customerId))
            );
            CustomerMapper.mapToCustomer(customerDto, customer);
            customerRepository.save(customer);
            isUpdated = true;
        }
        return isUpdated;
    }

    /**
     * @param mobileNumber
     * @return
     */
    @Override
    public boolean deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer","mobileNumber",mobileNumber)
        );
        Optional<Accounts> accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
        if(accounts.isPresent()){
            accountsRepository.deleteById(accounts.get().getAccountNumber());
            customerRepository.deleteById(customer.getCustomerId());
            return true;
        }
        return false;
    }
}
