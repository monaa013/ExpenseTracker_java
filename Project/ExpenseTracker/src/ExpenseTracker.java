import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ExpenseTracker extends JFrame {
    private static Map<String, String> credentials = new HashMap<>();
    private static Map<String, Double> expenses = new HashMap<>();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/java";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "blackpanda";

    private JLabel statusLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton signupButton;
    private JButton addExpenseButton;
    private JButton viewExpenseButton;
    private JButton exitButton;

    public ExpenseTracker() {
        setTitle("Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLayout(new GridLayout(4, 2));

        statusLabel = new JLabel("", JLabel.CENTER);

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        loginButton = new JButton("Login");
        signupButton = new JButton("Sign Up");

        add(new JLabel("Username: "));
        add(usernameField);
        add(new JLabel("Password: "));
        add(passwordField);
        add(loginButton);
        add(signupButton);
        add(new JLabel());
        add(statusLabel);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        signupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                signup();
            }
        });

        // Load credentials and expenses from the database
        loadCredentialsFromDatabase();
        loadExpensesFromDatabase();
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (credentials.containsKey(username) && credentials.get(username).equals(password)) {
            statusLabel.setText("Login successful!");
            showMainMenu();
        } else {
            statusLabel.setText("Invalid username or password.");
        }
    }

    private void signup() {
        String newUsername = usernameField.getText();
        String newPassword = new String(passwordField.getPassword());

        if (credentials.containsKey(newUsername)) {
            statusLabel.setText("Username already exists. Choose a different username.");
        } else {
            credentials.put(newUsername, newPassword);
            saveCredentialsToDatabase();
            statusLabel.setText("User created successfully!");
        }
    }

    private void showMainMenu() {
        getContentPane().removeAll();
        setLayout(new GridLayout(4, 1));

        addExpenseButton = new JButton("Add Expense");
        viewExpenseButton = new JButton("View Summary");
        exitButton = new JButton("Exit");

        add(addExpenseButton);
        add(viewExpenseButton);
        add(exitButton);
        add(statusLabel);

        addExpenseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayExpenseAdder();
            }
        });

        viewExpenseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayExpenseSummary();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusLabel.setText("Thank you!");
                System.exit(0);
            }
        });

        revalidate();
        repaint();
    }

    private void displayExpenseAdder() {
        getContentPane().removeAll();
        setLayout(new GridLayout(5, 2));

        JLabel categoryLabel = new JLabel("Category: ");
        JTextField categoryField = new JTextField();
        JLabel amountLabel = new JLabel("Amount: ");
        JTextField amountField = new JTextField();
        JButton addExpense = new JButton("Add");

        add(categoryLabel);
        add(categoryField);
        add(amountLabel);
        add(amountField);
        add(addExpense);
        add(new JLabel());
        add(new JLabel());
        add(new JLabel());
        add(statusLabel);

        addExpense.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String category = categoryField.getText();
                double amount = Double.parseDouble(amountField.getText());
                expenses.put(category, expenses.getOrDefault(category, 0.0) + amount);
                saveExpensesToDatabase();
                statusLabel.setText("Expense added successfully!");
                showMainMenu();
            }
        });

        revalidate();
        repaint();
    }

    private void displayExpenseSummary() {
        getContentPane().removeAll();
        setLayout(new GridLayout(expenses.size() + 2, 1));

        JLabel summaryLabel = new JLabel("Expense Summary:");
        add(summaryLabel);

        double totalExpense = 0;
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            JLabel expenseLabel = new JLabel(entry.getKey() + ": $" + entry.getValue());
            add(expenseLabel);
            totalExpense += entry.getValue();
        }

        JLabel totalLabel = new JLabel("Total: $" + totalExpense);
        add(totalLabel);

        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });
        add(backButton);
        add(statusLabel);

        revalidate();
        repaint();
    }

    private void loadCredentialsFromDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM credentials")) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                credentials.put(username, password);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveCredentialsToDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("DELETE FROM credentials");

            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                String username = entry.getKey();
                String password = entry.getValue();
                statement.executeUpdate("INSERT INTO credentials (username, password) VALUES ('" + username + "', '" + password + "')");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadExpensesFromDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM expenses")) {

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                double amount = resultSet.getDouble("amount");
                expenses.put(category, amount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveExpensesToDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("DELETE FROM expenses");

            for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                String category = entry.getKey();
                double amount = entry.getValue();
                statement.executeUpdate("INSERT INTO expenses (category, amount) VALUES ('" + category + "', " + amount + ")");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExpenseTracker tracker = new ExpenseTracker();
            tracker.setVisible(true);
        });
    }
}
