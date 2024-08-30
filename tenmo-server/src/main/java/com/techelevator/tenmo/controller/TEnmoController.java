package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TEnmoDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/tenmo/")
public class TEnmoController {
    @Autowired
    private TEnmoDao tEnmoDao;
//3.
    @RequestMapping(path="account", method = RequestMethod.GET)
    public Account getBalance( Principal principal){
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        return tEnmoDao.getAccountByUserId(user.getId());
    }
//4.
    @RequestMapping(path = "sendmoney", method = RequestMethod.POST)
    public Transfer sendTransfer(@RequestBody Transfer transfer, Principal principal){
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        int fromUserID = user.getId();
        return tEnmoDao.sendTransfer(fromUserID,transfer.getAccountTo(),transfer.getAmount());
    }
//5.
    @RequestMapping(path="transfers", method = RequestMethod.GET)
    public List<Transfer> getAllTransfer(Principal principal){
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        return tEnmoDao.getTransferByUserId(user.getId());
    }
//6.
    @RequestMapping(path="transfer/{id}", method = RequestMethod.GET)
    public Transfer getTransferById(@PathVariable int transferId){
        return tEnmoDao.getTransferById(transferId);
    }
//7.request a transfer
    @RequestMapping(path="requestmoney", method = RequestMethod.POST)
    public Transfer requestTransfer(@RequestBody Transfer transfer, Principal principal){
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        int toUserID = user.getId();
        return tEnmoDao.requestTransfer(transfer.getAccountFrom(), toUserID, transfer.getAmount());
    }
//8.
    @RequestMapping(path="transfers/pending", method = RequestMethod.GET)
    public List<Transfer> getPending(Principal principal){
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        int userId = user.getId();
        return tEnmoDao.getPendingTransferByUserId(userId);
    }

}
