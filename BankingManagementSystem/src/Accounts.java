import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class Accounts {

    private Connection connection;
    private Scanner scanner;

    public Accounts(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public long open_account(String email) {

        if (account_exist(email)) {

            System.out.println(
                    "Account Already Exists."
            );

            return -1;
        }

        String query =
                "INSERT INTO Accounts " +
                "(account_number, full_name, email, balance, security_pin) " +
                "VALUES (?, ?, ?, ?, ?)";

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();

        System.out.print("Enter Initial Amount: ");
        double balance = scanner.nextDouble();
        scanner.nextLine();

        if (balance < 0) {

            System.out.println(
                    "Initial amount cannot be negative."
            );

            return -1;
        }

        System.out.print("Enter Security Pin: ");
        String securityPin = scanner.nextLine();

        long accountNumber =
                generateAccountNumber();

        try {

            PreparedStatement ps =
                    connection.prepareStatement(query);

            ps.setLong(1, accountNumber);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.setDouble(4, balance);
            ps.setString(5, securityPin);

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "Account Created Successfully."
                );

                return accountNumber;
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return -1;
    }

    public long getAccountNumber(String email) {

        String query =
                "SELECT account_number " +
                "FROM Accounts WHERE email = ?";

        try {

            PreparedStatement ps =
                    connection.prepareStatement(query);

            ps.setString(1, email);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("account_number");
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return -1;
    }

    private long generateAccountNumber() {

        Random random = new Random();

        return 1000000000L
                + random.nextInt(900000000);
    }

    public boolean account_exist(String email) {

        String query =
                "SELECT * FROM Accounts WHERE email = ?";

        try {

            PreparedStatement ps =
                    connection.prepareStatement(query);

            ps.setString(1, email);

            ResultSet rs =
                    ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return false;
    }
}
