package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TEnmoDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/tenmo")
public class TEnmoController {

    @Autowired
    private TEnmoDao tEnmoDao;
    @Autowired
    private UserDao userDao;

    @RequestMapping(path = "/account", method = RequestMethod.GET)
    public Account getAccount(Principal principal) {
        String userName = principal.getName();
        User user = userDao.getUserByUsername(userName);
        return tEnmoDao.getAccountByUserId(user.getId());
    }
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> getUsers(){
        return userDao.getUsers();
    }
    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<Transfer> getTransferHistory(Principal principal) {
        String userName = principal.getName();
        User user = userDao.getUserByUsername(userName);
        return tEnmoDao.getTransferByUserId(user.getId());
    }

    @RequestMapping(path = "/sendmoney", method = RequestMethod.POST)
    public Transfer sendTransfer(@RequestBody Transfer transfer, Principal principal) {
        String userName = principal.getName();
        User user = userDao.getUserByUsername(userName);
        return tEnmoDao.sendTransfer(user.getId(), transfer.getAccountTo(), transfer.getAmount());
    }

    @RequestMapping(path = "/requestmoney", method = RequestMethod.POST)
    public Transfer requestTransfer(@RequestBody Transfer transfer, Principal principal) {
        String userName = principal.getName();
        User user = userDao.getUserByUsername(userName);
        return tEnmoDao.requestTransfer(transfer.getAccountFrom(), user.getId(), transfer.getAmount());
    }
    @RequestMapping(path="/transfers/pending", method = RequestMethod.GET)
    public List<Transfer> getPending(Principal principal){
        String userName = principal.getName();
        User user = userDao.getUserByUsername(userName);
        return tEnmoDao.getPendingTransferByUserId(user.getId());
    }

    @RequestMapping(path = "/transfers/{transferId}", method = RequestMethod.GET)
    public Transfer getTransferById(@PathVariable int transferId) {
        return tEnmoDao.getTransferById(transferId);
    }

    @RequestMapping(path = "/transfers/{transferId}/approve", method = RequestMethod.PUT)
    public void approveTransfer( @RequestBody Transfer transfer, @PathVariable int transferId) {
        tEnmoDao.approveTransfer(transfer.getTransferId());
    }

    @RequestMapping(path = "/transfers/{transferId}/reject", method = RequestMethod.PUT)
    public void rejectTransfer(@RequestBody Transfer transfer, @PathVariable int transferId) {
        tEnmoDao.rejectTransfer(transferId);
    }

}
