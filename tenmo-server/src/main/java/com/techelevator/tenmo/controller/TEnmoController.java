package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TEnmoDao;
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

    @RequestMapping(path = "/account", method = RequestMethod.GET)
    public Account getAccount(Principal principal) {
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        return tEnmoDao.getAccountByUserId(user.getId());
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<Transfer> getTransferHistory(Principal principal) {
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        return tEnmoDao.getTransferByUserId(user.getId());
    }

    @RequestMapping(path = "/sendmoney", method = RequestMethod.POST)
    public Transfer sendTransfer(@RequestParam int toUserId, @RequestParam BigDecimal amount, Principal principal) {
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        return tEnmoDao.sendTransfer(user.getId(), toUserId, amount);
    }

    @RequestMapping(path = "/requestmoney", method = RequestMethod.POST)
    public Transfer requestTransfer(@RequestParam int fromUserId, @RequestParam BigDecimal amount, Principal principal) {
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        return tEnmoDao.requestTransfer(user.getId(), fromUserId, amount);
    }

    @RequestMapping(path = "/transfers/{transferId}", method = RequestMethod.GET)
    public Transfer getTransferById(@PathVariable int transferId) {
        return tEnmoDao.getTransferById(transferId);
    }

    @RequestMapping(path = "/transfers/{transferId}/approve", method = RequestMethod.PUT)
    public void approveTransfer(@PathVariable int transferId) {
        tEnmoDao.approveTransfer(transferId);
    }

    @RequestMapping(path = "/transfers/{transferId}/reject", method = RequestMethod.PUT)
    public void rejectTransfer(@PathVariable int transferId) {
        tEnmoDao.rejectTransfer(transferId);
    }
}
