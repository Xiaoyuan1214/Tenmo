package com.techelevator.tenmo.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class AccountService {

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AuthenticatedUser currentUser;

    public AccountService(String baseUrl, AuthenticatedUser currentUser) {
        this.baseUrl = baseUrl;
        this.currentUser = currentUser;
    }

    public BigDecimal getBalance() {
        HttpEntity<Void> entity = createAuthEntity();
        String url = baseUrl + "tenmo/account";
        Account account = restTemplate.exchange(url, HttpMethod.GET, entity, Account.class).getBody();
        return account.getBalance();
    }

    public List<Transfer> getTransferHistory() {
        HttpEntity<Void> entity = createAuthEntity();
        String url = baseUrl + "tenmo/transfers";
        ResponseEntity<Transfer[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Transfer[].class);
        Transfer[] transfers = response.getBody();
        return transfers != null ? Arrays.asList(transfers) : List.of();
    }

    private HttpEntity<Void> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(headers);
    }
}
