package bankaccount;

import bank.*;
import util.BankingSystem;
import util.TimeFormatter;

import java.time.ZonedDateTime;
import java.util.*;

import static util.Time.getCurrentDateTime;

public class KbKookminBankAccount extends BankAccount {

    private String grade;

    public KbKookminBankAccount(String name, String id, String password, String bankName, String accountNumber, long balance) {
        super(name, id, password, bankName, accountNumber, balance, 2.536783e-9, 500); // 8.0%
    }

    @Override // 송금
    public void transfer() {
        System.out.println("============================================================================================");

        List<Bank> bankList = new ArrayList<>(Arrays.asList(
                TossBank.getInstance(), KbKookminBank.getInstance(),
                ShinhanBank.getInstance(), WooriBank.getInstance(),
                HanaBank.getInstance()
        ));
        printBankList();
        String bankNumber = scanner.nextLine();
        Bank bank = chooseBankInstance(bankList, bankNumber);
        System.out.println("The bank I'm trying to transfer money to is " + bank.getName());
        KbKookminBank instance = KbKookminBank.getInstance();
        if (bank.equals(instance)) {
            System.out.println("There is no fee between the same banks");
            System.out.println("Please enter your own account");
            BankAccount accountSend = validation(instance);

            System.out.print("Please enter the account number to transfer: ");
            String dstNum = scanner.nextLine();

            String[] pattern = bank.getAccountNumberRegex();

            if (dstNum.matches(pattern[1])) {
                dstNum = dstNum.replace("-", "");
            } else if (!dstNum.matches(pattern[0])) {
                throw new RuntimeException("Invalid input.");
            }
            dstNum = bank.formatAccountNumber(dstNum);

            Bank dstBank = BankingSystem.setDstBank(bankNumber);
            BankAccount accountReceive = dstBank.getBankAccount(dstNum);
            System.out.print("Please enter the amount: ");
            long amount = Long.parseLong(scanner.nextLine());

            if (checkBalance(amount)) {
                receive(accountSend, accountReceive, amount);
                ZonedDateTime zonedDateTime = getCurrentDateTime();
                addTransactionData(new TransactionData(TimeFormatter.format(zonedDateTime), getAccountNumber(), false, amount, getBalance(), "KB Bank " + accountReceive.getAccountNumber() + " " + accountReceive.getName()));
            }
        } else {
            System.out.println("You will be charged a fee for sending money to other banks");
            System.out.println("Please enter your own account");
            BankAccount accountSend = validation(instance);

            System.out.print("Please enter the account number to transfer: ");
            String dstNum = scanner.nextLine();

            String[] pattern = bank.getAccountNumberRegex();

            if (dstNum.matches(pattern[1])) {
                dstNum = dstNum.replace("-", "");
            } else if (!dstNum.matches(pattern[0])) {
                throw new RuntimeException("Invalid input.");
            }

            Bank dstBank = BankingSystem.setDstBank(bankNumber);
            BankAccount accountReceive = dstBank.getBankAccount(dstNum);

            System.out.print("Please enter the amount: ");
            long amount = Long.parseLong(scanner.nextLine());

            if (checkBalance(amount)) {
                int transferFee = getTransferFee();

                setBalance(accountSend.getBalance() - transferFee);
                addTransactionData(new TransactionData(TimeFormatter.format(getCurrentDateTime()), getAccountNumber(), false, transferFee, accountSend.getBalance(), "Other bank fee"));

                receive(accountSend, accountReceive, amount);
                ZonedDateTime zonedDateTime = getCurrentDateTime();
                addTransactionData(new TransactionData(TimeFormatter.format(zonedDateTime), getAccountNumber(), false, amount, getBalance(), accountReceive.getBankName() + " " + accountReceive.getAccountNumber() + " " + accountReceive.getName()));
            }
        }
        System.out.println("============================================================================================");
    }

    private BankAccount validation(Bank bank) {
        System.out.print("Please enter your ID: ");
        String id = scanner.nextLine();
        System.out.print("Please enter your Password: ");
        String pw = scanner.nextLine();
        return getBankAccount(id, pw, bank);
    }

    private BankAccount getBankAccount(String id, String pw, Bank bank) {
        Map<String, List<BankAccount>> idAccountListMap = bank.getIdBankAccountListMap();

        if (idAccountListMap.containsKey(id)) {
            BankAccount bankAccount = idAccountListMap.get(id).get(0);
            if (pw.equals(bankAccount.getPassword())) {
                return bankAccount;
            } else {
                System.out.println("this is incorrect password");
            }
        } else {
            System.out.println("This Id does not exist");
        }
        throw new NoSuchElementException("Invalid element");
    }

    private void printBankList() {
        System.out.println("============================================================================================");
        System.out.println("Please select the bank you want to transfer money");
        System.out.println("Currently, ATM is Kookmin Bank, and a fee is charged when transferring money to other banks");
        System.out.printf("%n======================%n");
        System.out.println("|   Available banks   |");
        System.out.println("----------------------");
        System.out.println("| 1. Toss Bank       |");
        System.out.println("| 2. KB Kookmin Bank |");
        System.out.println("| 3. Shinhan Bank    |");
        System.out.println("| 4. Woori Bank      |");
        System.out.println("| 5. Hana Bank       |");
        System.out.println("| 6. Exit            |");
        System.out.println("=======================");
        System.out.print("Please select a bank [1 ~ 6]: ");
    }

    private Bank chooseBankInstance(List<Bank> bankList, String num) {
        if (num.equals("1")) {
            return bankList.get(0);
        } else if (num.equals("2")) {
            return bankList.get(1);
        } else if (num.equals("3")) {
            return bankList.get(2);
        } else if (num.equals("4")) {
            return bankList.get(3);
        } else if (num.equals("5")) {
            return bankList.get(4);
        } else if (num.equals("6")) {
            System.out.println("stop program.");
            return null;
        }
        throw new NoSuchElementException("Invalid element");

    }

    private boolean checkBalance(long amount) {
        if (amount > getBalance()) {
            System.out.println("It's more than the balance in the account");
            System.out.println("Let's go back to the beginning");
            return false;
        }
        return true;
    }
}
