package com.techelevator.tenmo.dao;

import com.techelevator.dao.BaseDaoTests;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

public class TEnmoJdbcDaoTest extends BaseDaoTests {

    private TEnmoJdbcDao tEnmoJdbcDao;

    @Before
    public void setup() {
        tEnmoJdbcDao = new TEnmoJdbcDao(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("DELETE FROM transfer");
        jdbcTemplate.update("DELETE FROM account");
        jdbcTemplate.update("DELETE FROM tenmo_user");

        jdbcTemplate.update("INSERT INTO tenmo_user (user_id, username, password_hash, role) VALUES (1001, 'user1', 'password', 'USER')");
        jdbcTemplate.update("INSERT INTO tenmo_user (user_id, username, password_hash, role) VALUES (1002, 'user2', 'password', 'USER')");
        jdbcTemplate.update("INSERT INTO account (account_id, user_id, balance) VALUES (3001, 1001, 1000.00)");
        jdbcTemplate.update("INSERT INTO account (account_id, user_id, balance) VALUES (3002, 1002, 2000.00)");

        jdbcTemplate.update("INSERT INTO transfer (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (1, 2, 2, 3001, 3002, 50.00)");
    }



    @Test
    public void getAccountByUserId_given_valid_userId_returns_account() {
        Account account = tEnmoJdbcDao.getAccountByUserId(1001);
        Assert.assertNotNull(account);
        Assert.assertEquals(3001, account.getAccountId());
        Assert.assertEquals(1001, account.getUserId());
        Assert.assertEquals(new BigDecimal("1000.00"), account.getBalance());
    }

    @Test
    public void sendTransfer_creates_transfer_and_updates_balances() {
        BigDecimal amount = new BigDecimal("100.00");
        Transfer transfer = tEnmoJdbcDao.sendTransfer(1001, 1002, amount);

        Assert.assertNotNull(transfer);
        Assert.assertTrue(transfer.getTransferId() > 0);
        Assert.assertEquals(3001, transfer.getAccountFrom());
        Assert.assertEquals(3002, transfer.getAccountTo());
        Assert.assertEquals(amount, transfer.getAmount());

        Account senderAccount = tEnmoJdbcDao.getAccountByUserId(1001);
        Account receiverAccount = tEnmoJdbcDao.getAccountByUserId(1002);

        Assert.assertEquals(new BigDecimal("900.00"), senderAccount.getBalance());
        Assert.assertEquals(new BigDecimal("2100.00"), receiverAccount.getBalance());
    }

    @Test
    public void getTransferById_returns_correct_transfer() {
        Transfer transfer = tEnmoJdbcDao.getTransferById(1);
        Assert.assertNotNull(transfer);
        Assert.assertEquals(1, transfer.getTransferId());
        Assert.assertEquals(new BigDecimal("50.00"), transfer.getAmount());
        Assert.assertEquals(3001, transfer.getAccountFrom());
        Assert.assertEquals(3002, transfer.getAccountTo());
        Assert.assertEquals(2, transfer.getTransferTypeId());
    }
        @Test
    public void requestTransfer_creates_pending_transfer() {
        BigDecimal amount = new BigDecimal("150.00");
        Transfer transfer = tEnmoJdbcDao.requestTransfer(1001, 1002, amount);

        Assert.assertNotNull(transfer);
        Assert.assertTrue(transfer.getTransferId() > 0);
        Assert.assertEquals(1, transfer.getTransferTypeId());
        Assert.assertEquals(1, transfer.getTransferStatusId());
        Assert.assertEquals(3001, transfer.getAccountFrom());
        Assert.assertEquals(3002, transfer.getAccountTo());
        Assert.assertEquals(amount, transfer.getAmount());
    }

    @Test
    public void getTransferByUserId_returns_list_of_transfers() {
        List<Transfer> transfers = tEnmoJdbcDao.getTransferByUserId(1001);
        Assert.assertNotNull(transfers);
        Assert.assertFalse(transfers.isEmpty());
    }

    @Test
    public void getPendingTransferByUserId_returns_list_of_pending_transfers() {
        List<Transfer> pendingTransfers = tEnmoJdbcDao.getPendingTransferByUserId(1001);
        Assert.assertNotNull(pendingTransfers);
    }

    @Test
    public void approveTransfer_updates_transfer_status_and_balances() {
        tEnmoJdbcDao.approveTransfer(1);

        Transfer transfer = tEnmoJdbcDao.getTransferById(1);
        Assert.assertEquals(2, transfer.getTransferStatusId());

        Account senderAccount = tEnmoJdbcDao.getAccountByUserId(1001);
        Account receiverAccount = tEnmoJdbcDao.getAccountByUserId(1002);

        Assert.assertEquals(new BigDecimal("950.00"), senderAccount.getBalance());
        Assert.assertEquals(new BigDecimal("2050.00"), receiverAccount.getBalance());
    }

    @Test
    public void rejectTransfer_updates_transfer_status() {
        tEnmoJdbcDao.rejectTransfer(1);

        Transfer transfer = tEnmoJdbcDao.getTransferById(1);
        Assert.assertEquals(3, transfer.getTransferStatusId());
    }

    @Test
    public void getAccountByAccountId_returns_correct_account() {
        Account account = tEnmoJdbcDao.getAccountByAccountId(3001);
        Assert.assertNotNull(account);
        Assert.assertEquals(3001, account.getAccountId());
        Assert.assertEquals(1001, account.getUserId());
        Assert.assertEquals(new BigDecimal("1000.00"), account.getBalance());
    }
}
