package com.techelevator.tenmo.dao;

import com.techelevator.dao.BaseDaoTests;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;

import static org.junit.Assert.*;

public class TEnmoJdbcDaoTest  extends BaseDaoTests {
    protected static final User USER_1 = new User(1001, "user1", "user1", "USER");
    protected static final User USER_2 = new User(1002, "user2", "user2", "USER");
    protected static final Account ACCOUNT_1 = new Account(3001, 1001, new BigDecimal(1000));
    protected static final Account ACCOUNT_2 = new Account(3001, 1002, new BigDecimal("2000.00"));

    protected static final Transfer TRANSFER_1 = new Transfer(1, 2, 2, 1, 2, new BigDecimal("50.00"));
    protected static final Transfer TRANSFER_2 = new Transfer(2, 1, 1, 2, 1, new BigDecimal("100.00"));

    private TEnmoJdbcDao tEnmoJdbcDao;

    @Before
    public void setup() {
        tEnmoJdbcDao = new TEnmoJdbcDao(dataSource);
    }
    @Test(expected = IllegalArgumentException.class)
    public void getAccountByUserId_given_null_throws_exception(){
        tEnmoJdbcDao.getAccountByUserId(0);
    }
    @Test
    public void getAccountByUserId_given_invalid_id_return_null(){
        Assert.assertNull(tEnmoJdbcDao.getAccountByUserId(2000));
    }
    @Test
    public void getAccountByUserId_given_valid_id_return_account(){
        Account account = tEnmoJdbcDao.getAccountByUserId(USER_1.getId());
    }
}


