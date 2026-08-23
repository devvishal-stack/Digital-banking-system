import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountManager {

    private Connection connection;
    private Scanner scanner;

    public AccountManager(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    // Check Balance
    public double getBalance(long accountNumber) throws SQLException {

        String query =
                "SELECT balance FROM Accounts WHERE account_number = ?";

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setLong(1, accountNumber);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            double balance = rs.getDouble("balance");
            System.out.println("Current Balance: " + balance);
            return balance;
        }

        System.out.println("Account not found!");
        return 0;
    }

    // Credit Money
    public void creditMoney(long accountNumber) throws SQLException {

        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println("Amount must be greater than 0.");
            return;
        }

        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();

        String check =
                "SELECT * FROM Accounts " +
                "WHERE account_number = ? AND security_pin = ?";

        PreparedStatement ps = connection.prepareStatement(check);
        ps.setLong(1, accountNumber);
        ps.setString(2, pin);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            String update =
                    "UPDATE Accounts " +
                    "SET balance = balance + ? " +
                    "WHERE account_number = ?";

            PreparedStatement ps2 =
                    connection.prepareStatement(update);

            ps2.setDouble(1, amount);
            ps2.setLong(2, accountNumber);

            int rows = ps2.executeUpdate();

            if (rows > 0) {
                System.out.println(
                        "Money Credited Successfully."
                );
            }

        } else {
            System.out.println("Invalid Security Pin!");
        }
    }

    // Debit Money
    public void debitMoney(long accountNumber) throws SQLException {

        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println("Amount must be greater than 0.");
            return;
        }

        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();

        double balance = getBalanceWithoutPrinting(accountNumber);

        if (balance < amount) {
            System.out.println("Insufficient Balance!");
            return;
        }

        String check =
                "SELECT * FROM Accounts " +
                "WHERE account_number = ? AND security_pin = ?";

        PreparedStatement ps = connection.prepareStatement(check);
        ps.setLong(1, accountNumber);
        ps.setString(2, pin);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            String update =
                    "UPDATE Accounts " +
                    "SET balance = balance - ? " +
                    "WHERE account_number = ?";

            PreparedStatement ps2 =
                    connection.prepareStatement(update);

            ps2.setDouble(1, amount);
            ps2.setLong(2, accountNumber);

            int rows = ps2.executeUpdate();

            if (rows > 0) {
                System.out.println(
                        "Money Debited Successfully."
                );
            }

        } else {
            System.out.println("Invalid Security Pin!");
        }
    }

    // Transfer Money
    public void transferMoney(long senderAccountNumber)
            throws SQLException {

        System.out.print("Enter Receiver Account Number: ");
        long receiverAccountNumber = scanner.nextLong();
        scanner.nextLine();

        if (senderAccountNumber == receiverAccountNumber) {
            System.out.println(
                    "You cannot transfer money to the same account!"
            );
            return;
        }

        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println(
                    "Amount must be greater than 0."
            );
            return;
        }

        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();

        // Check sender account and PIN
        String senderCheck =
                "SELECT balance FROM Accounts " +
                "WHERE account_number = ? AND security_pin = ?";

        PreparedStatement senderPs =
                connection.prepareStatement(senderCheck);

        senderPs.setLong(1, senderAccountNumber);
        senderPs.setString(2, pin);

        ResultSet senderRs = senderPs.executeQuery();

        if (!senderRs.next()) {
            System.out.println("Invalid Security Pin!");
            return;
        }

        double senderBalance =
                senderRs.getDouble("balance");

        if (senderBalance < amount) {
            System.out.println("Insufficient Balance!");
            return;
        }

        // Check receiver account
        String receiverCheck =
                "SELECT account_number FROM Accounts " +
                "WHERE account_number = ?";

        PreparedStatement receiverPs =
                connection.prepareStatement(receiverCheck);

        receiverPs.setLong(1, receiverAccountNumber);

        ResultSet receiverRs =
                receiverPs.executeQuery();

        if (!receiverRs.next()) {
            System.out.println(
                    "Receiver Account Not Found!"
            );
            return;
        }

        // Start transaction
        try {

            connection.setAutoCommit(false);

            // Deduct from sender
            String debitQuery =
                    "UPDATE Accounts " +
                    "SET balance = balance - ? " +
                    "WHERE account_number = ?";

            PreparedStatement debitPs =
                    connection.prepareStatement(debitQuery);

            debitPs.setDouble(1, amount);
            debitPs.setLong(2, senderAccountNumber);

            int debitRows =
                    debitPs.executeUpdate();

            // Add to receiver
            String creditQuery =
                    "UPDATE Accounts " +
                    "SET balance = balance + ? " +
                    "WHERE account_number = ?";

            PreparedStatement creditPs =
                    connection.prepareStatement(creditQuery);

            creditPs.setDouble(1, amount);
            creditPs.setLong(2, receiverAccountNumber);

            int creditRows =
                    creditPs.executeUpdate();

            if (debitRows > 0 && creditRows > 0) {

                connection.commit();

                System.out.println(
                        "Money Transferred Successfully!"
                );

                System.out.println(
                        "Transferred Amount: " + amount
                );

                System.out.println(
                        "Receiver Account: "
                                + receiverAccountNumber
                );

            } else {

                connection.rollback();

                System.out.println(
                        "Transfer Failed!"
                );
            }

        } catch (SQLException e) {

            connection.rollback();

            System.out.println(
                    "Transfer Failed: " + e.getMessage()
            );

        } finally {

            connection.setAutoCommit(true);
        }
    }

    // Get balance internally without printing
    private double getBalanceWithoutPrinting(long accountNumber)
            throws SQLException {

        String query =
                "SELECT balance FROM Accounts " +
                "WHERE account_number = ?";

        PreparedStatement ps =
                connection.prepareStatement(query);

        ps.setLong(1, accountNumber);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getDouble("balance");
        }

        return 0;
    }
}
