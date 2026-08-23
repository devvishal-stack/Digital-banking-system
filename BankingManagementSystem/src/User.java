import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class User {

    private Connection connection;
    private Scanner scanner;

    public User(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void register() {

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (user_exist(email)) {

            System.out.println(
                    "User Already Exists for this Email Address!!"
            );

            return;
        }

        String registerQuery =
                "INSERT INTO User(full_name, email, password) " +
                "VALUES (?, ?, ?)";

        try {

            PreparedStatement ps =
                    connection.prepareStatement(registerQuery);

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, password);

            int affectedRows =
                    ps.executeUpdate();

            if (affectedRows > 0) {

                System.out.println(
                        "Registration Successful!"
                );

            } else {

                System.out.println(
                        "Registration Failed"
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public String login() {

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        String loginQuery =
                "SELECT * FROM User " +
                "WHERE email = ? AND password = ?";

        try {

            PreparedStatement ps =
                    connection.prepareStatement(loginQuery);

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {
                return email;
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }

    public boolean user_exist(String email) {

        String query =
                "SELECT * FROM User WHERE email = ?";

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
