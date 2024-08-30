package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TEnmoDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/tenmo")
public class TEnmoController {
    @Autowired
    private TEnmoDao tEnmoDao;
    @RequestMapping(path="/account", method = RequestMethod.GET)
    public Account account( Principal principal){
        String userName = principal.getName();
        User user = tEnmoDao.getUserByUserName(userName);
        return tEnmoDao.getAccountByUserId(user.getId());
    }

    @RequestMapping(path = "sendmoney", method = RequestMethod.POST)
    public Transfer sendTransfer(){

        return null;
    }

}
