package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;


import java.util.Arrays;
import java.util.List;

public class TransferService {

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AuthenticatedUser currentUser;

    public TransferService(String baseUrl, AuthenticatedUser currentUser) {
        this.baseUrl = baseUrl;
        this.currentUser = currentUser;
    }


    public List<Transfer> getTransferHistory() {
        HttpEntity<Void> entity = createAuthEntity();
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "transfers", HttpMethod.GET, entity, Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers != null ? Arrays.asList(transfers) : null;
    }

    public Transfer getTransferById(int transferId) {
        HttpEntity<Void> entity = createAuthEntity();
        Transfer transfer = null;
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(baseUrl + "transfer/" + transferId, HttpMethod.GET, entity, Transfer.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    public boolean sendBucks(int recipientUserId, BigDecimal amount) {
        Transfer transfer = new Transfer(currentUser.getUser().getId(), recipientUserId, amount);
        return createTransfer(transfer);
    }

    public boolean requestBucks(int senderUserId, BigDecimal amount) {
        Transfer transfer = new Transfer(senderUserId, currentUser.getUser().getId(), amount);
        return createTransfer(transfer);
    }

    public List<Transfer> getPendingTransfers() {
        HttpEntity<Void> entity = createAuthEntity();
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "transfers/pending", HttpMethod.GET, entity, Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers != null ? Arrays.asList(transfers) : null;
    }

    public boolean approveTransfer(int transferId) {
        return updateTransferStatus(transferId, "approved");
    }

    public boolean rejectTransfer(int transferId) {
        return updateTransferStatus(transferId, "rejected");
    }

    private boolean updateTransferStatus(int transferId, String status) {
        HttpEntity<Void> entity = createAuthEntity();
        boolean success = false;
        try {
            restTemplate.exchange(baseUrl + "transfers/" + transferId + "/status/" + status, HttpMethod.PUT, entity, Void.class);
            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

    private boolean createTransfer(Transfer transfer) {
        HttpEntity<Transfer> entity = createTransferEntity(transfer);
        boolean success = false;
        try {
            restTemplate.exchange(baseUrl + "transfers", HttpMethod.POST, entity, Void.class);
            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

    private HttpEntity<Void> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Transfer> createTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(transfer, headers);
    }
}
