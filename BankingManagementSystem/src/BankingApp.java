import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class BankingApp {

    private static final String URL =
            "jdbc:mysql://localhost:3306/banking_system";

    private static final String USERNAME = "root";
    private static final String PASSWORD = "801303";

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {

            System.err.println(
                    "Driver Load Error: " + e.getMessage()
            );

            return;
        }

        try (
                Connection connection =
                        DriverManager.getConnection(
                                URL,
                                USERNAME,
                                PASSWORD
                        );

                Scanner scanner = new Scanner(System.in)
        ) {

            User user = new User(connection, scanner);

            Accounts accounts =
                    new Accounts(connection, scanner);

            AccountManager accountManager =
                    new AccountManager(connection, scanner);

            boolean running = true;

            while (running) {

                System.out.println();
                System.out.println(
                        "*** WELCOME TO BANKING SYSTEM ***"
                );

                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");

                int choice =
                        readIntInput(
                                scanner,
                                "Enter your choice: "
                        );

                switch (choice) {

                    case 1:
                        user.register();
                        break;

                    case 2:
                        handleUserSession(
                                user,
                                accounts,
                                accountManager,
                                scanner
                        );
                        break;

                    case 3:
                        System.out.println();
                        System.out.println(
                                "THANK YOU FOR USING THE BANKING SYSTEM!"
                        );

                        System.out.println(
                                "Exiting System..."
                        );

                        running = false;
                        break;

                    default:
                        System.out.println(
                                "Invalid option! Please enter 1-3."
                        );
                        break;
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Database Connection Failed: "
                            + e.getMessage()
            );
        }
    }

    private static void handleUserSession(
            User user,
            Accounts accounts,
            AccountManager accountManager,
            Scanner scanner
    ) throws SQLException {

        String email = user.login();

        if (email == null) {

            System.out.println(
                    "Incorrect Email or Password!"
            );

            return;
        }

        System.out.println(
                "\nUser Logged In Successfully!"
        );

        long accountNumber;

        if (!accounts.account_exist(email)) {

            System.out.println(
                    "\nNo bank account associated with this login."
            );

            System.out.println(
                    "1. Open a new Bank Account"
            );

            System.out.println(
                    "2. Cancel / Log Out"
            );

            int choice =
                    readIntInput(
                            scanner,
                            "Enter choice: "
                    );

            if (choice == 1) {

                accountNumber =
                        accounts.open_account(email);

                if (accountNumber <= 0) {

                    System.out.println(
                            "Account Creation Failed!"
                    );

                    return;
                }

                System.out.println(
                        "Account Created Successfully!"
                );

                System.out.println(
                        "Account Number: "
                                + accountNumber
                );

            } else {

                System.out.println(
                        "Logged Out Successfully!"
                );

                return;
            }

        } else {

            accountNumber =
                    accounts.getAccountNumber(email);

            if (accountNumber <= 0) {

                System.out.println(
                        "Unable to find account."
                );

                return;
            }
        }

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println();
            System.out.println(
                    "*** BANKING OPERATIONS ***"
            );

            System.out.println("1. Debit Money");
            System.out.println("2. Credit Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Log Out");

            int choice =
                    readIntInput(
                            scanner,
                            "Enter your choice: "
                    );

            switch (choice) {

                case 1:
                    accountManager.debitMoney(
                            accountNumber
                    );
                    break;

                case 2:
                    accountManager.creditMoney(
                            accountNumber
                    );
                    break;

                case 3:
                    accountManager.transferMoney(
                            accountNumber
                    );
                    break;

                case 4:
                    accountManager.getBalance(
                            accountNumber
                    );
                    break;

                case 5:
                    System.out.println(
                            "Logged Out Successfully!"
                    );

                    loggedIn = false;
                    break;

                default:
                    System.out.println(
                            "Invalid Choice! Please select 1-5."
                    );
                    break;
            }
        }
    }

    private static int readIntInput(
            Scanner scanner,
            String prompt
    ) {

        while (true) {

            System.out.print(prompt);

            try {

                int input = scanner.nextInt();
                scanner.nextLine();

                return input;

            } catch (InputMismatchException e) {

                System.out.println(
                        "Invalid input! Please enter a valid number."
                );

                scanner.nextLine();
            }
        }
    }
}
