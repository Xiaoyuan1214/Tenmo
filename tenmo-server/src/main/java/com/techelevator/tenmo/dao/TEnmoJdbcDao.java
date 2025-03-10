package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TEnmoJdbcDao implements TEnmoDao {

    private JdbcTemplate template;

    public TEnmoJdbcDao(DataSource ds) {
        this.template = new JdbcTemplate(ds);
    }

    private Account mapRowToAccount(SqlRowSet rowSet) {
        Account account = new Account();
        account.setAccountId(rowSet.getInt("account_id"));
        account.setUserId(rowSet.getInt("user_id"));
        account.setBalance(rowSet.getBigDecimal("balance"));
        return account;
    }

    private Transfer mapRowToTransfer(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getInt("transfer_id"));
        transfer.setTransferTypeId(rowSet.getInt("transfer_type_id"));
        transfer.setTransferStatusId(rowSet.getInt("transfer_status_id"));
        transfer.setAccountFrom(rowSet.getInt("account_from"));
        transfer.setAccountTo(rowSet.getInt("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));
        return transfer;
    }
//    public TransferType getTransferTypeNameById(int typeId){
//        TransferType transferType = new TransferType();
//        String sql = "SELECT transfer_type_id, transfer_type_desc FROM transfer_type WHERE transfer_type_id = ?";
//        try{
//            SqlRowSet results = template.queryForRowSet(sql,typeId);
//            if(results.next()) {
//
//                transferType.setTransferTypeId(results.getInt("transfer_type_id"));
//                transferType.setTransferTypeDesc(results.getString("transfer_type_desc"));
//            }
//            return transferType;
//        } catch (CannotGetJdbcConnectionException e) {
//            throw new DaoException("Unable to connect to server or database", e);
//        } catch (DataIntegrityViolationException e) {
//            throw new DaoException("Data integrity violation", e);
//        }
//    }
//    public TransferStatus getTransferStatusNameById(int statusId){
//        TransferStatus transferStatus = new TransferStatus();
//        String sql = "SELECT ransfer_status_id, transfer_status_desc FROM transfer_status WHERE transfer_status_id = ?";
//        try{
//            SqlRowSet results = template.queryForRowSet(sql,statusId);
//            if(results.next()) {
//
//                transferStatus.setTransferStatusId(results.getInt("transfer_status_id"));
//                transferStatus.setTransferStatusDesc(results.getString("transfer_status_desc"));
//            }
//            return transferStatus;
//        } catch (CannotGetJdbcConnectionException e) {
//            throw new DaoException("Unable to connect to server or database", e);
//        } catch (DataIntegrityViolationException e) {
//            throw new DaoException("Data integrity violation", e);
//        }
//    }


    @Override
    public Account getAccountByUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        Account account = null;
        String sql = "SELECT account_id, user_id, balance FROM account WHERE user_id = ?";
        try {
            SqlRowSet results = template.queryForRowSet(sql, userId);
            if (results.next()) {
                account = mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return account;
    }

    @Override
    public Transfer sendTransfer(int fromUserId, int toUserId, BigDecimal amount) {
        if (fromUserId == toUserId) {
            throw new IllegalArgumentException("Cannot send money to yourself.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        Account senderAccount = getAccountByUserId(fromUserId);
        Account receiverAccount = getAccountByUserId(toUserId);

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new DaoException("Insufficient funds.");
        }

        Transfer transfer = new Transfer();
        transfer.setTransferTypeId(2);
        transfer.setTransferStatusId(2);
        transfer.setAccountFrom(senderAccount.getAccountId());
        transfer.setAccountTo(receiverAccount.getAccountId());
        transfer.setAmount(amount);

        String insertTransferSql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";
        try {
            SqlRowSet results = template.queryForRowSet(insertTransferSql, transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(),
                    transfer.getAccountTo(), transfer.getAmount());
            if (results.next()) {
                transfer.setTransferId(results.getInt("transfer_id"));
            }
            String senderSql = "UPDATE account SET balance = balance - ? WHERE account_id = ?";
            String receiverSql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
            template.update(senderSql, amount, senderAccount.getAccountId());
            template.update(receiverSql, amount, receiverAccount.getAccountId());
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (Exception e) {
            throw new DaoException("Failed to complete the transfer.", e);
        }
        return transfer;
    }

    @Override
    public List<Transfer> getTransferByUserId(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id OR t.account_to = a.account_id " +
                "WHERE a.user_id = ?";
        try {
            SqlRowSet results = template.queryForRowSet(sql, userId);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                transfers.add(transfer);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (Exception e) {
            throw new DaoException("Failed to retrieve transfers.", e);
        }
        return transfers;
    }

    @Override
    public Transfer getTransferById(int transferId) {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer WHERE transfer_id = ?";
        try {
            SqlRowSet results = template.queryForRowSet(sql, transferId);
            if (results.next()) {
                transfer = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (Exception e) {
            throw new DaoException("Failed to retrieve transfer.", e);
        }
        return transfer;
    }

    @Override
    public Transfer requestTransfer(int fromUserId, int toUserId, BigDecimal amount) {
        if (fromUserId == toUserId) {
            throw new IllegalArgumentException("Cannot request money from yourself.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        Account senderAccount = getAccountByUserId(fromUserId);
        Account receiverAccount = getAccountByUserId(toUserId);

        Transfer transfer = new Transfer();
        transfer.setTransferTypeId(1);
        transfer.setTransferStatusId(1);
        transfer.setAccountFrom(senderAccount.getAccountId());
        transfer.setAccountTo(receiverAccount.getAccountId());
        transfer.setAmount(amount);

        String insertTransferSql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";
        try {
            SqlRowSet results = template.queryForRowSet(insertTransferSql, transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(),
                    transfer.getAccountTo(), transfer.getAmount());
            if (results.next()) {
                transfer.setTransferId(results.getInt("transfer_id"));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (Exception e) {
            throw new DaoException("Failed to complete the transfer.", e);
        }
        return transfer;
    }

    @Override
    public List<Transfer> getPendingTransferByUserId(int userId) {
        List<Transfer> pendingTransfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id OR t.account_to = a.account_id " +
                "WHERE a.user_id = ? AND t.transfer_status_id = 1";  // 1 = Pending

        try {
            SqlRowSet results = template.queryForRowSet(sql, userId);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                pendingTransfers.add(transfer);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (Exception e) {
            throw new DaoException("Failed to retrieve pending transfers.", e);
        }

        return pendingTransfers;
    }

    @Override
    public boolean updateTransferStatus(int transferId, int transferStatusId) {
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
        int numberOfRows = 0;
        try {
            numberOfRows = template.update(sql, transferStatusId, transferId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return numberOfRows == 1;
    }

    @Override
    public void approveTransfer(int transferId) {
        Transfer transfer = getTransferById(transferId);
        if (transfer == null) {
            throw new DaoException("Transfer not found: " + transferId);
        }


        Account fromAccount = getAccountByAccountId(transfer.getAccountFrom());
        Account toAccount = getAccountByAccountId(transfer.getAccountTo());

        if (fromAccount.getBalance().compareTo(transfer.getAmount()) < 0) {
            throw new DaoException("Insufficient funds for transfer.");
        }


        String updateSenderSql = "UPDATE account SET balance = balance - ? WHERE account_id = ?";
        String updateReceiverSql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";

        try {
            template.update(updateSenderSql, transfer.getAmount(), fromAccount.getAccountId());
            template.update(updateReceiverSql, transfer.getAmount(), toAccount.getAccountId());
            updateTransferStatus(transferId, 2); // 2 = Approved
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (Exception e) {
            throw new DaoException("Failed to complete approval process", e);
        }
    }

    @Override
    public void rejectTransfer(int transferId) {
        if(!updateTransferStatus(transferId, 3)){
            throw new DaoException("Failed to update transfer status");
        }
    }

    public Account getAccountByAccountId(int accountId){
        Account account = null;
        String sql = "SELECT * FROM account WHERE account_id = ?";
        try {
            SqlRowSet result = template.queryForRowSet(sql, accountId);
            if (result.next()) {
                account = mapRowToAccount(result);
            }
            return account;
        }catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (Exception e) {
            throw new DaoException("Failed to retrieve pending transfers.", e);
        }

    }



}
