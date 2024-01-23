import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ExpenseTracker extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/expense_tracker";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "blackpanda";

    private Map<String, String> credentials = new HashMap<>();
    private Map<String, Double> expenses = new HashMap<>();
    private String currentUser;

    private JLabel statusLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton signupButton;

    private JTextField nameField;
    private JTextField ageField;
    private JTextField salaryField;
    private JTextField contactField;
    private JTextField dobField;

    private JButton addExpenseButton;
    private JButton viewExpenseButton;
    private JButton logoutButton;

    private JFrame addExpenseFrame;
    private JTextField categoryField;
    private JTextField amountField;
    private JButton saveExpenseButton;
    private JButton cancelExpenseButton;

    private JFrame viewSummaryFrame;
    private JButton backToMenuButton;

    public ExpenseTracker() {
        setTitle("Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("", JLabel.CENTER);

        JPanel loginPanel = createLoginPanel();
        add(loginPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Load credentials from the database
        loadCredentialsFromDatabase();
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel usernameLabel = new JLabel("Username: ");
        JLabel passwordLabel = new JLabel("Password: ");

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        loginButton = new JButton("Login");
        signupButton = new JButton("Sign Up");

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(signupButton);

        // Add action listeners for login and signup
        loginButton.addActionListener(e -> login());
        signupButton.addActionListener(e -> showSignupPage());

        return panel;
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (credentials.containsKey(username) && credentials.get(username).equals(password)) {
            currentUser = username;
            statusLabel.setText("Login successful!");
            showUserProfile(currentUser);
        } else {
            statusLabel.setText("Invalid username or password.");
        }
    }

    private void showSignupPage() {
        getContentPane().removeAll();
        setLayout(new GridLayout(8, 2, 10, 10));

        JLabel nameLabel = new JLabel("Name: ");
        JLabel ageLabel = new JLabel("Age: ");
        JLabel salaryLabel = new JLabel("Salary: ");
        JLabel contactLabel = new JLabel("Contact: ");
        JLabel dobLabel = new JLabel("Date of Birth: ");

        nameField = new JTextField();
        ageField = new JTextField();
        salaryField = new JTextField();
        contactField = new JTextField();
        dobField = new JTextField();

        JButton signupButton = new JButton("Sign Up");
        add(nameLabel);
        add(nameField);
        add(ageLabel);
        add(ageField);
        add(salaryLabel);
        add(salaryField);
        add(contactLabel);
        add(contactField);
        add(dobLabel);
        add(dobField);
        add(new JLabel());
        add(signupButton);
        add(new JLabel());
        add(new JLabel());
        add(new JLabel());
        add(statusLabel);

        signupButton.addActionListener(e -> signup());

        revalidate();
        repaint();
    }

    private void signup() {
        String newUsername = usernameField.getText();
        String newPassword = new String(passwordField.getPassword());
        String name = nameField.getText();
        String age = ageField.getText();
        String salary = salaryField.getText();
        String contact = contactField.getText();
        String dob = dobField.getText();

        if (credentials.containsKey(newUsername)) {
            statusLabel.setText("Username already exists. Choose a different username.");
        } else {
            // Save user details to the database
            saveUserDetailsToDatabase(newUsername, newPassword, name, age, salary, contact, dob);

            // Update credentials map
            credentials.put(newUsername, newPassword);

            statusLabel.setText("User created successfully!");
            currentUser = newUsername;
            showUserProfile(currentUser);
        }
    }

    private void showUserProfile(String username) {
        // Load expenses from the database
        loadExpensesFromDatabase(username);

        getContentPane().removeAll();
        setLayout(new GridLayout(5, 2, 10, 10));

        add(new JLabel("Name: " + getUserDetail(username, "name")));
        add(new JLabel("Age: " + getUserDetail(username, "age")));
        add(new JLabel("Salary: $" + getUserDetail(username, "salary")));
        add(new JLabel("Contact: " + getUserDetail(username, "contact")));
        add(new JLabel("Date of Birth: " + getUserDetail(username, "dob")));

        addExpenseButton = new JButton("Add Expense");
        viewExpenseButton = new JButton("View Summary");
        logoutButton = new JButton("Logout");

        add(addExpenseButton);
        add(viewExpenseButton);
        add(logoutButton);
        add(new JLabel());
        add(statusLabel);

        addExpenseButton.addActionListener(e -> displayExpenseAdder());
        viewExpenseButton.addActionListener(e -> displayExpenseSummary());
        logoutButton.addActionListener(e -> {
            statusLabel.setText("Logged out successfully!");
            showLoginPage();
        });

        revalidate();
        repaint();
    }

    private void displayExpenseAdder() {
        addExpenseFrame = new JFrame("Add Expense");
        addExpenseFrame.setSize(300, 200);
        addExpenseFrame.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel categoryLabel = new JLabel("Category: ");
        JLabel amountLabel = new JLabel("Amount: ");

        categoryField = new JTextField();
        amountField = new JTextField();

        saveExpenseButton = new JButton("Save");
        cancelExpenseButton = new JButton("Cancel");

        addExpenseFrame.add(categoryLabel);
        addExpenseFrame.add(categoryField);
        addExpenseFrame.add(amountLabel);
        addExpenseFrame.add(amountField);
        addExpenseFrame.add(saveExpenseButton);
        addExpenseFrame.add(cancelExpenseButton);
        addExpenseFrame.add(new JLabel());
        addExpenseFrame.add(statusLabel);

        saveExpenseButton.addActionListener(e -> saveExpense());
        cancelExpenseButton.addActionListener(e -> closeAddExpenseWindow());

        addExpenseFrame.setVisible(true);
    }

    private void saveExpense() {
        String category = categoryField.getText();
        String amountStr = amountField.getText();

        try {
            double amount = Double.parseDouble(amountStr);

            // Save expense to the database
            saveExpenseToDatabase(currentUser, category, amount);

            // Update local expenses map
            expenses.put(category, amount);

            statusLabel.setText("Expense saved successfully!");
            closeAddExpenseWindow();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount. Please enter a valid number.");
        }
    }

    private void closeAddExpenseWindow() {
        addExpenseFrame.dispose();
        showUserProfile(currentUser); // After closing the add expense window, show the user profile again
    }

    private void displayExpenseSummary() {
        viewSummaryFrame = new JFrame("Expense Summary");
        viewSummaryFrame.setSize(300, 200);
        viewSummaryFrame.setLayout(new GridLayout(expenses.size() + 3, 1, 10, 10));

        JLabel summaryLabel = new JLabel("Expense Summary:");
        viewSummaryFrame.add(summaryLabel);

        double totalExpense = 0;
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            JLabel expenseLabel = new JLabel(entry.getKey() + ": $" + entry.getValue());
            viewSummaryFrame.add(expenseLabel);
            totalExpense += entry.getValue();
        }

        JLabel totalLabel = new JLabel("Total: $" + totalExpense);
        viewSummaryFrame.add(totalLabel);

        backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.addActionListener(e -> closeViewSummaryWindow());
        viewSummaryFrame.add(backToMenuButton);

        viewSummaryFrame.add(statusLabel);

        viewSummaryFrame.setVisible(true);
    }

    private void closeViewSummaryWindow() {
        viewSummaryFrame.dispose();
        showUserProfile(currentUser); // After closing the summary window, show the user profile again
    }

    private void loadExpensesFromDatabase(String username) {
        expenses.clear(); // Clear existing expenses before loading

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT category, amount FROM expenses WHERE username = '" + username + "'")) {

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                double amount = resultSet.getDouble("amount");
                expenses.put(category, amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getUserDetail(String username, String column) {
        String value = "";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT " + column + " FROM user_profiles WHERE username = '" + username + "'")) {

            while (resultSet.next()) {
                value = resultSet.getString(column);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    private void saveUserDetailsToDatabase(String username, String password, String name, String age, String salary, String contact, String dob) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO user_profiles (username, name, age, salary, contact, dob) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, name);
                preparedStatement.setInt(3, Integer.parseInt(age));
                preparedStatement.setDouble(4, Double.parseDouble(salary));
                preparedStatement.setString(5, contact);
                preparedStatement.setDate(6, java.sql.Date.valueOf(dob));
                preparedStatement.executeUpdate();
            }

            // Save credentials to the database
            saveCredentialsToDatabase(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveCredentialsToDatabase(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO credentials (username, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveExpenseToDatabase(String username, String category, double amount) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO expenses (username, category, amount) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, category);
                preparedStatement.setDouble(3, amount);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void showLoginPage() {
        getContentPane().removeAll();
        JPanel loginPanel = createLoginPanel();
        add(loginPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ExpenseTracker tracker = new ExpenseTracker();
            tracker.setVisible(true);
        });
    }
}
