package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface TEnmoDao {

    Account getAccountByUserId(int userId);

    User getUserByUserName(String username);

    Transfer sendTransfer(int fromUserId, int toUserId, BigDecimal amount);

    List<Transfer> getTransferByUserId(int userId);

    Transfer getTransferById(int transferId);

    Transfer requestTransfer(int fromUserId, int toUserId, BigDecimal amount);

    List<Transfer> getPendingTransferByUserId(int userId);

    boolean updateTransferStatus(int transferId, int transferStatusId);

    void approveTransfer(int transferId);

    void rejectTransfer(int transferId);

    List<User> getAllUsers();

    User getUserById(int userId);
}
