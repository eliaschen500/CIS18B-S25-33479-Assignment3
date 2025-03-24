import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ============================
// Custom Exception Classes
// ============================

// Thrown when a user tries to deposit a negative amount
class NegativeDepositException extends Exception {
    public NegativeDepositException(String msg) {
        super(msg);
    }
}

// Thrown when a user tries to withdraw more than they have
class OverdrawException extends Exception {
    public OverdrawException(String msg) {
        super(msg);
    }
}

// Thrown when trying to use an account that's been closed
class InvalidAccountOperationException extends Exception {
    public InvalidAccountOperationException(String msg) {
        super(msg);
    }
}

// ============================
// Observer Pattern
// ============================

// Interface for observers to implement
interface Observer {
    void update(String message); // gets called when something happens
}

// This logs every transaction
class TransactionLogger implements Observer {
    @Override
    public void update(String message) {
        System.out.println(">> LOG: " + message); // Logs the transaction message
    }
}

// ============================
// BankAccount Class (Subject)
// ============================
class BankAccount {
    protected String accountNumber; // Unique identifier for the account
    protected double balance;       // Current balance of the account
    protected boolean isActive;    // Status of the account (active/closed)
    private List<Observer> observers = new ArrayList<>(); // List of observers

    // Constructor to initialize the account
    public BankAccount(String accNum, double initialBalance) {
        this.accountNumber = accNum;
        this.balance = initialBalance;
        this.isActive = true; // Account is active by default
    }

    // Method to add an observer
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    // Method to notify all observers with a message
    protected void notifyObservers(String message) {
        for (Observer obs : observers) {
            obs.update(message);
        }
    }

    // Method to deposit money into the account
    public void deposit(double amount) throws NegativeDepositException, InvalidAccountOperationException {
        if (!isActive) {
            throw new InvalidAccountOperationException("Account is closed. Can't deposit.");
        }
        if (amount < 0) {
            throw new NegativeDepositException("No negative deposits allowed!");
        }
        balance += amount; // Add the amount to the balance
        notifyObservers("Deposited $" + amount); // Notify observers
    }

    // Method to withdraw money from the account
    public void withdraw(double amount) throws OverdrawException, InvalidAccountOperationException {
        if (!isActive) {
            throw new InvalidAccountOperationException("Account is closed. Can't withdraw.");
        }
        if (amount > balance) {
            throw new OverdrawException("Not enough funds. Balance: $" + balance);
        }
        balance -= amount; // Deduct the amount from the balance
        notifyObservers("Withdrew $" + amount); // Notify observers
    }

    // Method to get the current balance
    public double getBalance() {
        return balance;
    }

    // Method to close the account
    public void closeAccount() {
        isActive = false; // Mark the account as closed
        notifyObservers("Account has been closed."); // Notify observers
    }

    // Method to display account details
    public void displayAccountDetails() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Initial Balance: $" + balance);
    }
}

// ============================
// Decorator Pattern
// ============================

// Abstract decorator class to add additional functionality
abstract class BankAccountDecorator extends BankAccount {
    protected BankAccount account; // Reference to the wrapped account

    // Constructor to initialize the decorator
    public BankAccountDecorator(BankAccount account) {
        super(account.accountNumber, account.getBalance());
        this.account = account;
    }

    @Override
    public void deposit(double amount) throws NegativeDepositException, InvalidAccountOperationException {
        account.deposit(amount); // Deposit to the wrapped account
    }
    
    @Override
    public void withdraw(double amount) throws OverdrawException, InvalidAccountOperationException {
        account.withdraw(amount); // Withdrawal to the wrapped account
    }

    @Override
    public double getBalance() {
        return account.getBalance(); // Balance retrieval to the wrapped account
    }

    @Override
    public void closeAccount() {
        account.closeAccount(); // Account closure to the wrapped account
    }

    @Override
    public void addObserver(Observer observer) {
        account.addObserver(observer); // Observer addition to the wrapped account
    }
}

// Adds a security rule: max $500 per withdrawal
class SecureBankAccount extends BankAccountDecorator {
    // Constructor to initialize the secure account
    public SecureBankAccount(BankAccount account) {
        super(account);
    }

    @Override
    public void withdraw(double amount) throws OverdrawException, InvalidAccountOperationException {
        if (amount > 500) {
            throw new OverdrawException("Withdrawal limit is $500."); // Enforce withdrawal limit
        }
        super.withdraw(amount); // Withdrawal to the wrapped account
    }
}

// ============================
// Main Program
// ============================

public class BankAccountTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Create account with user input
            System.out.print("Enter initial balance: ");
            double initialBalance = scanner.nextDouble();

            // Create a new bank account
            BankAccount myAccount = new BankAccount("123456", initialBalance);

            // Display account details
            myAccount.displayAccountDetails();

            // Add logger to observe transactions
            TransactionLogger logger = new TransactionLogger();
            myAccount.addObserver(logger);

            // Wrap with SecureBankAccount to limit withdrawals
            SecureBankAccount secureAccount = new SecureBankAccount(myAccount);

            // Do a deposit
            System.out.print("Enter amount to deposit: ");
            double depositAmount = scanner.nextDouble();
            secureAccount.deposit(depositAmount);

            // Do a withdrawal
            System.out.print("Enter amount to withdraw: ");
            double withdrawAmount = scanner.nextDouble();
            secureAccount.withdraw(withdrawAmount);

            // Show final balance
            System.out.println("Final balance: $" + secureAccount.getBalance());

            // Option to close account
            System.out.print("Close account? (yes/no): ");
            String closeInput = scanner.next();
            if (closeInput.equalsIgnoreCase("yes")) {
                secureAccount.closeAccount();
                System.out.println("Account closed.");
            }

        } catch (NegativeDepositException | OverdrawException | InvalidAccountOperationException e) {
            System.out.println("Banking Error: " + e.getMessage()); // Handle banking-specific errors
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage()); // Handle generic errors
        } finally {
            scanner.close(); // Close the scanner to release resources
        }
    }
}