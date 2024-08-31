package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface TEnmoDao {

   
    public Account getAccountByUserId(int userId);
    public Transfer sendTransfer(int fromUserId, int toUserId, BigDecimal amount);
    public List<Transfer> getTransferByUserId(int userId);
    public Transfer getTransferById(int transferId);
    public Transfer requestTransfer(int fromUserId, int toUserId, BigDecimal amount);
    public List<Transfer> getPendingTransferByUserId(int userId);
    public boolean updateTransferStatus(int transferId, int transferStatusId);
    public void approveTransfer(int transferId);
    public void rejectTransfer(int transferId);

}
