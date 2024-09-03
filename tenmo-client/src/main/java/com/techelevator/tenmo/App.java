package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;
import com.techelevator.tenmo.services.UserService;

import java.math.BigDecimal;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/tenmo/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private AccountService accountService;
    private TransferService transferService;
    private UserService userService;

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            initializeServices();
            mainMenu();
        }
    }

    private void initializeServices() {
        accountService = new AccountService(API_BASE_URL, currentUser);
        transferService = new TransferService(API_BASE_URL, currentUser);
        userService = new UserService(API_BASE_URL, currentUser);
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        BigDecimal balance = accountService.getBalance();
        System.out.println("Your current account balance is: $" + balance);
    }

    private void viewTransferHistory() {
        List<Transfer> transfers = transferService.getTransferHistory();
        if (transfers == null || transfers.isEmpty()) {
            consoleService.printErrorMessage();
        } else {
            consoleService.printTransfers(transfers);
            int transferId = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
            if (transferId != 0) {
                Transfer transfer = transferService.getTransferById(transferId);
                if (transfer != null) {
                    consoleService.printTransferDetails(transfer);
                } else {
                    consoleService.printErrorMessage();
                }
            }
        }
    }

    private void viewPendingRequests() {
        List<Transfer> pendingTransfers = transferService.getPendingTransfers();
        if (pendingTransfers == null || pendingTransfers.isEmpty()) {
            System.out.println("There is no pending transaction!");
        } else {
            consoleService.printPendingTransfers(pendingTransfers);
            int transferId = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel): ");
            if (transferId != 0) {
                handlePendingTransfer(transferId);
            }
        }
    }

    private void sendBucks() {
        List<User> users = userService.getAllUsers();
        if (users == null || users.isEmpty()) {
            consoleService.printErrorMessage();
            return;
        }
        consoleService.printUsers(users);
        int userId = consoleService.promptForInt("Enter ID of user you are sending to (0 to cancel): ");
        if (userId == 0) return;

        BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");
        if (transferService.sendBucks(userId, amount)) {
            System.out.println("Transfer successful!");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void requestBucks() {
        List<User> users = userService.getAllUsers();
        if (users == null || users.isEmpty()) {
            consoleService.printErrorMessage();
            return;
        }
        consoleService.printUsers(users);
        int userId = consoleService.promptForInt("Enter ID of user you are requesting from (0 to cancel): ");
        if (userId == 0) return;

        BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");
        if (transferService.requestBucks(userId, amount)) {
            System.out.println("Request successful!");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handlePendingTransfer(int transferId) {
        Transfer transfer = transferService.getTransferById(transferId);
        if (transfer != null) {
            consoleService.printTransferDetails(transfer);
            int selection = consoleService.promptForMenuSelection("1: Approve\n2: Reject\n0: Don't approve or reject\nPlease choose an option: ");
            if (selection == 1) {
                if (transferService.approveTransfer(transferId)) {
                    System.out.println("Transfer approved!");
                } else {
                    consoleService.printErrorMessage();
                }
            } else if (selection == 2) {
                if (transferService.rejectTransfer(transferId)) {
                    System.out.println("Transfer rejected!");
                } else {
                    consoleService.printErrorMessage();
                }
            }
        } else {
            consoleService.printErrorMessage();
        }
    }
}
