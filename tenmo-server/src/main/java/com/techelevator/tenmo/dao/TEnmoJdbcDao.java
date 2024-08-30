package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
@Component

public class TEnmoJdbcDao implements TEnmoDao {
    private JdbcTemplate template;
    public TEnmoJdbcDao(DataSource ds){
        template = new JdbcTemplate(ds);
    }
    private Account mapRowToAccount(SqlRowSet rowSet){
        Account account = new Account();
        account.setAccountId(rowSet.getInt("account_id"));
        account.setUserId(rowSet.getInt("user_id"));
        account.setBalance(rowSet.getBigDecimal("balance"));

    return account;
    }
    private Transfer mapRowToTransfer(SqlRowSet rowSet){
        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getInt("transfer_id"));
        transfer.setTransferTypeId(rowSet.getInt("transfer_status_id"));
        transfer.setTransferStatusId(rowSet.getInt("account_from"));
        transfer.setAccountTo(rowSet.getInt("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));

        return transfer;
    }
    private User mapRowToUser(SqlRowSet rowSet){
        User user = new User();
        user.setId(rowSet.getInt("user_id"));
        user.setUsername(rowSet.getString("username"));
        user.setPassword(rowSet.getString("password_hash"));

        return user;
    }
    @Override
    public Account getAccountByUserId(int userId) {
        Account account = null;
        String sql = "SELECT account_id, user_id, balance FROM account WHERE user_id = ?";
        try {
            SqlRowSet results = template.queryForRowSet(sql, userId);
            if(results.next()) {
                account = mapRowToAccount(results);
            }
        }catch (CannotGetJdbcConnectionException e) {
            System.out.println("Problem connecting");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Data problems");
        }
        return account;
    }
    @Override
    public User getUserByUserName(String username) {
        User user = null;
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user WHERE username = ?";
        try {
            SqlRowSet results = template.queryForRowSet(sql, username);
            if (results.next()) {
                user = mapRowToUser(results);
            }
        }catch (CannotGetJdbcConnectionException e) {
            System.out.println("Problem connecting");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Data problems");
        }
        System.out.println(user);
        return user;
    }
    @Override
    public Transfer sendTransfer(int fromUserId, int toUserId, BigDecimal amount) {
        if (fromUserId ==toUserId) {
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
        if(receiverAccount == null){
            throw new DaoException("Receiver account not found");
        }
        if(senderAccount == null){
            throw new DaoException("Sender account not found.");
        }

        Transfer transfer = new Transfer();
        transfer.setTransferTypeId(2);
        transfer.setTransferStatusId(2);
        transfer.setAccountFrom(senderAccount.getAccountId());
        transfer.setAccountTo(receiverAccount.getAccountId());
        transfer.setAmount(amount);

        String insertTransferSql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";
        try{
            SqlRowSet results = template.queryForRowSet(insertTransferSql, transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(),
                    transfer.getAccountTo(), transfer.getAmount());
            if(results.next()){
                transfer.setTransferId(results.getInt("transfer_id"));
            }
            String senderSql = "UPDATE account SET balance = balance - ? WHERE account_id = ?";
            String receiverSql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";

            template.update(senderSql, amount, senderAccount.getAccountId());
            template.update(receiverSql, amount, receiverAccount.getAccountId());
        }catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (Exception e){
            throw new DaoException("Failed to complete the transfer.");
        }
        return transfer;
    }

    @Override
    public List<Transfer> getTransferByUserId(int userId) {
        List<Transfer> transfers= new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount " +
                    "FROM transfer t " +
                    "JOIN account a ON t.account_from = a.account_id OR t.account_to = a.account_id "+
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
            throw new DaoException("Failed to retrieve transfer", e);
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
            throw new DaoException("Failed to retrieve transfer", e);
        }
        return transfer;
    }

    @Override
    public Transfer requestTransfer(int fromUserId, int toUserId, BigDecimal amount) {
        if (fromUserId ==toUserId) {
            throw new IllegalArgumentException("Cannot request money to yourself.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        Account senderAccount = getAccountByUserId(fromUserId);
        Account receiverAccount = getAccountByUserId(toUserId);

        if(receiverAccount == null){
            throw new DaoException("Receiver account not found");
        }
        if(senderAccount == null){
            throw new DaoException("Sender account not found.");
        }

        Transfer transfer = new Transfer();
        transfer.setTransferTypeId(1);
        transfer.setTransferStatusId(1);
        transfer.setAccountFrom(senderAccount.getAccountId());
        transfer.setAccountTo(receiverAccount.getAccountId());
        transfer.setAmount(amount);

        String insertTransferSql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";
        try{
            SqlRowSet results = template.queryForRowSet(insertTransferSql, transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(),
                    transfer.getAccountTo(), transfer.getAmount());
            if(results.next()){
                transfer.setTransferId(results.getInt("transfer_id"));
            }

        }catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (Exception e){
            throw new DaoException("Failed to complete the transfer.");
        }
        return transfer;
    }


    @Override
    public List<Transfer> getPendingTransferByUserId(int userId) {
        List<Transfer> pendingTransfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id OR t.account_to = a.account_id " +
                "WHERE a.user_id = ? AND t.transfer_status_id = (SELECT transfer_status_id FROM transfer_status WHERE transfer_status_desc = 'Pending')";

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
            throw new DaoException("Failed to retrieve pending transfers", e);
        }

        return pendingTransfers;
    }

    @Override
    public boolean updateTransferStatus(int transferId, int transferStatusId) {
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
        int numberOfRow = 0;
        try{
            numberOfRow = template.update(sql, transferStatusId, transferId);
        }catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return numberOfRow == 1;
    }

    @Override
    public void approveTransfer(int transferId) {
      
        
//        String senderSql = "UPDATE account SET balance = balance - ?  WHERE account_id = ?";
//        String receiverSql = "UPDATE account SET balance = balance + ? status =  WHERE account_id = ?";
//
//        template.update(senderSql, amount, senderAccount.getAccountId());
//        template.update(receiverSql, amount, receiverAccount.getAccountId());

    }

    @Override
    public void rejectTransfer(int transferId) {

    }


}
