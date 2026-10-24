import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class MainGUI extends JFrame {

    private final TrainOperations trainService;
    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;

    private UserAccount currentUser;
    private String currentLoginRole = "PASSENGER";

    // CardLayout container for application screens
    private CardLayout mainCardLayout;
    private JPanel mainCardContainer;

    // Welcome Screen
    private JLabel welcomeDbStatusLabel;

    // Login Screen
    private JLabel loginTitleLabel;
    private JLabel loginSubtitleLabel;
    private JLabel loginRoleBadgeLabel;
    private JLabel loginHintLabel;
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // Passenger Portal
    private JLabel passengerGreetingLabel;
    private JLabel passengerDbStatusLabel;
    private JTabbedPane passengerTabbedPane;
    private JTable myBookingsTable;
    private DefaultTableModel myBookingsModel;
    private JLabel myBookingsSubtitleLabel;

    // Admin Portal
    private JLabel adminGreetingLabel;
    private JLabel adminDbStatusLabel;
    private JTabbedPane adminTabbedPane;
    private JTable adminTrainTable;
    private DefaultTableModel adminTrainTableModel;
    private JTable adminRouteTable;
    private DefaultTableModel adminRouteTableModel;
    private List<Train> adminTrainList;
    private JTable allPassengersTable;
    private DefaultTableModel allPassengersModel;

    private JLabel dbStatusLabel;
    private JTabbedPane tabbedPane;

    private JComboBox<Station> searchSourceCombo;
    private JComboBox<Station> searchDestCombo;
    private JTextField searchDateField;
    private JTable trainTable;
    private DefaultTableModel trainTableModel;
    private JTable routeTable;
    private DefaultTableModel routeTableModel;
    private List<Train> currentTrainList;

    private JComboBox<Train> bookTrainCombo;
    private JComboBox<Station> bookSourceCombo;
    private JComboBox<Station> bookDestCombo;
    private JComboBox<TrainDAO.TrainClassItem> bookClassCombo;
    private JTextField bookDateField;
    private JLabel bookDistanceLabel;
    private JLabel bookFareLabel;
    private JLabel bookSeatsLabel;
    private JTextField passNameField;
    private JSpinner passAgeSpinner;
    private JComboBox<String> passGenderCombo;
    private JTextField passPhoneField;

    // PNR Status and Cancellation tab components
    private JTextField pnrSearchField;
    private JLabel pnrValueLabel;
    private JLabel pnrBookingIdLabel;
    private JLabel pnrStatusLabel;
    private JLabel pnrBookingTimeLabel;
    private JLabel pnrFareLabel;

    private JLabel pnrTrainLabel;
    private JLabel pnrRouteLabel;
    private JLabel pnrDateLabel;
    private JLabel pnrTimeLabel;
    private JLabel pnrClassLabel;

    private JLabel pnrPassengerLabel;
    private JPanel pnrPassengersContainer;

    private JLabel pnrPaymentStatusLabel;
    private JLabel pnrPaymentMethodLabel;
    private JLabel pnrPaymentAmountLabel;
    private JLabel pnrPaymentTxnLabel;
    private JLabel pnrPaymentDateLabel;
    private JLabel pnrPaymentNoticeLabel;

    private JLabel pnrCancelStatusLabel;
    private JLabel pnrCancelDateLabel;
    private JLabel pnrCancelReasonLabel;
    private JLabel pnrRefundAmountLabel;

    private JButton cancelTicketButton;
    private Booking currentPnrBooking;

    private JTable allBookingsTable;
    private DefaultTableModel allBookingsModel;

    public MainGUI() {
        this.trainService = new TrainDAO();
        this.bookingDAO = new BookingDAO();
        this.userDAO = new UserDAO();

        setTitle("RailEase - Railway Management System");
        setSize(1040, 740);
        setMinimumSize(new Dimension(860, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        checkDatabaseAndLoadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        mainCardLayout = new CardLayout();
        mainCardContainer = new JPanel(mainCardLayout);

        mainCardContainer.add(createWelcomePanel(), "WELCOME");
        mainCardContainer.add(createLoginPanel(), "LOGIN");
        mainCardContainer.add(createPassengerView(), "PASSENGER");
        mainCardContainer.add(createAdminView(), "ADMIN");

        add(mainCardContainer, BorderLayout.CENTER);
        mainCardLayout.show(mainCardContainer, "WELCOME");
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor, int width, int height, int fontSize) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                ButtonModel model = getModel();
                Color currentBg = bgColor;
                if (!isEnabled()) {
                    currentBg = new Color(215, 220, 228);
                } else if (model.isPressed()) {
                    currentBg = bgColor.darker();
                } else if (model.isRollover()) {
                    currentBg = new Color(
                            Math.min(255, bgColor.getRed() + 25),
                            Math.min(255, bgColor.getGreen() + 25),
                            Math.min(255, bgColor.getBlue() + 25)
                    );
                }

                // Draw solid background
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Draw border for light buttons
                if (bgColor.getRed() > 215 && bgColor.getGreen() > 215 && bgColor.getBlue() > 215) {
                    g2.setColor(new Color(185, 196, 212));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }

                // Draw label perfectly centered
                Font f = getFont();
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics(f);
                String label = getText();
                if (label != null && !label.isEmpty()) {
                    int textX = (getWidth() - fm.stringWidth(label)) / 2;
                    int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                    g2.setColor(isEnabled() ? fgColor : new Color(130, 138, 148));
                    g2.drawString(label, textX, textY);
                }
                g2.dispose();
            }
        };

        Font f = new Font("SansSerif", Font.BOLD, fontSize);
        btn.setFont(f);
        btn.setForeground(fgColor);
        btn.setBackground(bgColor);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FontMetrics fm = btn.getFontMetrics(f);
        int calcW = (width > 0) ? width : fm.stringWidth(text != null ? text : "") + 28;
        int calcH = (height > 0) ? height : 32;
        Dimension dim = new Dimension(calcW, calcH);
        btn.setPreferredSize(dim);
        if (width > 0 && height > 0) {
            btn.setMaximumSize(dim);
            btn.setMinimumSize(dim);
        }
        return btn;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 220, 230), 1),
                new EmptyBorder(32, 45, 32, 45)
        ));
        card.setMaximumSize(new Dimension(520, 520));
        card.setPreferredSize(new Dimension(500, 500));

        JLabel brandLabel = new JLabel("RailEase");
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        brandLabel.setForeground(new Color(24, 43, 73));
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Railway Reservation Management System");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subLabel.setForeground(new Color(100, 112, 128));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(420, 2));

        JLabel promptLabel = new JLabel("Welcome! Please choose your login role to continue:");
        promptLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        promptLabel.setForeground(new Color(35, 48, 68));
        promptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons with guaranteed visibility on macOS Swing
        JButton passengerLoginBtn = createStyledButton("Passenger Login", new Color(24, 100, 190), Color.WHITE, 340, 46, 15);
        passengerLoginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        passengerLoginBtn.addActionListener(e -> showLoginForm("PASSENGER"));

        JButton adminLoginBtn = createStyledButton("Admin Login", new Color(36, 50, 72), Color.WHITE, 340, 46, 15);
        adminLoginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminLoginBtn.addActionListener(e -> showLoginForm("ADMIN"));

        // Demo credentials guide box
        JPanel demoBox = new JPanel();
        demoBox.setLayout(new BoxLayout(demoBox, BoxLayout.Y_AXIS));
        demoBox.setBackground(new Color(244, 247, 253));
        demoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 220, 240), 1),
                new EmptyBorder(10, 16, 10, 16)
        ));
        demoBox.setMaximumSize(new Dimension(380, 95));
        demoBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel demoTitle = new JLabel("Demo Accounts for Testing:");
        demoTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        demoTitle.setForeground(new Color(20, 70, 140));
        demoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel demoPassenger = new JLabel("• Passenger: aarav_demo  (Password: aarav123)");
        demoPassenger.setFont(new Font("SansSerif", Font.PLAIN, 12));
        demoPassenger.setForeground(new Color(40, 50, 65));
        demoPassenger.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel demoAdmin = new JLabel("• Admin: admin_demo  (Password: admin123)");
        demoAdmin.setFont(new Font("SansSerif", Font.PLAIN, 12));
        demoAdmin.setForeground(new Color(40, 50, 65));
        demoAdmin.setAlignmentX(Component.LEFT_ALIGNMENT);

        demoBox.add(demoTitle);
        demoBox.add(Box.createVerticalStrut(4));
        demoBox.add(demoPassenger);
        demoBox.add(Box.createVerticalStrut(2));
        demoBox.add(demoAdmin);

        card.add(brandLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));
        card.add(promptLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(passengerLoginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(adminLoginBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(demoBox);

        centerWrapper.add(card);
        panel.add(centerWrapper, BorderLayout.CENTER);

        // Footer with DB status and controls
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        footer.setBackground(new Color(235, 240, 246));

        welcomeDbStatusLabel = new JLabel("Database: Checking DB...");
        welcomeDbStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        footer.add(welcomeDbStatusLabel, BorderLayout.WEST);

        JPanel footerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerRight.setOpaque(false);

        JButton dbBtn = createStyledButton("MySQL Settings", new Color(242, 245, 250), new Color(40, 50, 70), 135, 30, 12);
        dbBtn.addActionListener(e -> showDatabaseConfigDialog());

        JButton exitBtn = createStyledButton("Exit", new Color(242, 245, 250), new Color(180, 40, 40), 80, 30, 12);
        exitBtn.addActionListener(e -> System.exit(0));

        footerRight.add(dbBtn);
        footerRight.add(exitBtn);
        footer.add(footerRight, BorderLayout.EAST);

        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));

        // Top bar with Back button
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topBar.setBackground(new Color(24, 43, 73));

        JButton backBtn = createStyledButton("← Back to Welcome Screen", new Color(38, 60, 92), Color.WHITE, 210, 32, 12);
        backBtn.addActionListener(e -> {
            loginPasswordField.setText("");
            mainCardLayout.show(mainCardContainer, "WELCOME");
        });
        topBar.add(backBtn);
        panel.add(topBar, BorderLayout.NORTH);

        // Center Login Card
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 220, 230), 1),
                new EmptyBorder(22, 30, 22, 30)
        ));
        card.setPreferredSize(new Dimension(470, 345));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Role Badge
        loginRoleBadgeLabel = new JLabel("PASSENGER PORTAL", SwingConstants.CENTER);
        loginRoleBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        loginRoleBadgeLabel.setForeground(new Color(24, 100, 190));
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(loginRoleBadgeLabel, gbc);

        // 2. Title
        gbc.gridy++;
        loginTitleLabel = new JLabel("Passenger Login", SwingConstants.CENTER);
        loginTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        loginTitleLabel.setForeground(new Color(24, 43, 73));
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(loginTitleLabel, gbc);

        // 3. Subtitle
        gbc.gridy++;
        loginSubtitleLabel = new JLabel("Sign in to search trains, book tickets, and manage reservations", SwingConstants.CENTER);
        loginSubtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginSubtitleLabel.setForeground(new Color(100, 110, 125));
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(loginSubtitleLabel, gbc);

        // 4. Username Label
        gbc.gridy++;
        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLbl.setForeground(new Color(30, 42, 60));
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(userLbl, gbc);

        // 5. Username Field (Full Width)
        gbc.gridy++;
        loginUsernameField = new JTextField(15);
        loginUsernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loginUsernameField.setPreferredSize(new Dimension(0, 36));
        loginUsernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(185, 195, 210), 1),
                new EmptyBorder(4, 12, 4, 12)
        ));
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(loginUsernameField, gbc);

        // 6. Password Label
        gbc.gridy++;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        passLbl.setForeground(new Color(30, 42, 60));
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(passLbl, gbc);

        // 7. Password Field (Full Width)
        gbc.gridy++;
        loginPasswordField = new JPasswordField(15);
        loginPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loginPasswordField.setPreferredSize(new Dimension(0, 36));
        loginPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(185, 195, 210), 1),
                new EmptyBorder(4, 12, 4, 12)
        ));
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(loginPasswordField, gbc);

        // Enter key to submit login
        loginUsernameField.addActionListener(e -> executeLogin());
        loginPasswordField.addActionListener(e -> executeLogin());

        // 8. Buttons Panel (Side by Side, Equal Width)
        gbc.gridy++;
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(0, 38));

        JButton cancelBtn = createStyledButton("Cancel / Back", new Color(242, 245, 250), new Color(40, 50, 70), 0, 38, 13);
        cancelBtn.addActionListener(e -> {
            loginPasswordField.setText("");
            mainCardLayout.show(mainCardContainer, "WELCOME");
        });

        JButton loginBtn = createStyledButton("Login", new Color(24, 100, 190), Color.WHITE, 0, 38, 14);
        loginBtn.addActionListener(e -> executeLogin());

        btnPanel.add(cancelBtn);
        btnPanel.add(loginBtn);
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(btnPanel, gbc);

        // 9. Demo Credentials Hint Box (Full Width)
        gbc.gridy++;
        JPanel hintBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        hintBox.setBackground(new Color(244, 247, 253));
        hintBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 225, 240), 1),
                new EmptyBorder(2, 8, 2, 8)
        ));
        loginHintLabel = new JLabel("Demo credentials: aarav_demo  /  aarav123", SwingConstants.CENTER);
        loginHintLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginHintLabel.setForeground(new Color(60, 75, 100));
        hintBox.add(loginHintLabel);
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(hintBox, gbc);

        centerWrapper.add(card);
        panel.add(centerWrapper, BorderLayout.CENTER);
        return panel;
    }

    private void showLoginForm(String role) {
        this.currentLoginRole = (role != null) ? role.toUpperCase() : "PASSENGER";
        if ("ADMIN".equalsIgnoreCase(currentLoginRole)) {
            loginRoleBadgeLabel.setText("ADMINISTRATOR PORTAL");
            loginRoleBadgeLabel.setForeground(new Color(190, 45, 45));
            loginTitleLabel.setText("Admin Login");
            loginSubtitleLabel.setText("Sign in to manage trains, view all bookings, and access records");
            loginHintLabel.setText("Demo credentials: admin_demo  /  admin123");
            loginUsernameField.setText("admin_demo");
            loginPasswordField.setText("admin123");
        } else {
            loginRoleBadgeLabel.setText("PASSENGER PORTAL");
            loginRoleBadgeLabel.setForeground(new Color(24, 100, 190));
            loginTitleLabel.setText("Passenger Login");
            loginSubtitleLabel.setText("Sign in to search trains, book tickets, and manage reservations");
            loginHintLabel.setText("Demo credentials: aarav_demo  /  aarav123");
            loginUsernameField.setText("aarav_demo");
            loginPasswordField.setText("aarav123");
        }
        mainCardLayout.show(mainCardContainer, "LOGIN");
        loginUsernameField.requestFocusInWindow();
    }

    private void executeLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username.", "Login Validation", JOptionPane.WARNING_MESSAGE);
            loginUsernameField.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Login Validation", JOptionPane.WARNING_MESSAGE);
            loginPasswordField.requestFocusInWindow();
            return;
        }

        UserDAO.AuthResult result = userDAO.authenticate(username, password, currentLoginRole);

        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            loginPasswordField.setText("");
            loginPasswordField.requestFocusInWindow();
            return;
        }

        // Login succeeded
        this.currentUser = result.getAccount();
        loginPasswordField.setText(""); // Never keep password in memory or UI

        if ("PASSENGER".equalsIgnoreCase(currentUser.getRole())) {
            String name = (currentUser.getPassengerName() != null && !currentUser.getPassengerName().isEmpty())
                    ? currentUser.getPassengerName() : currentUser.getUsername();
            passengerGreetingLabel.setText("Welcome, " + name + " (Passenger)");

            // Pre-fill passenger booking fields for convenience
            if (currentUser.getPassengerName() != null) {
                passNameField.setText(currentUser.getPassengerName());
            }
            if (currentUser.getPassengerPhone() != null) {
                passPhoneField.setText(currentUser.getPassengerPhone());
            }

            loadMyBookings();
            passengerTabbedPane.setSelectedIndex(0);
            mainCardLayout.show(mainCardContainer, "PASSENGER");

        } else if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            adminGreetingLabel.setText("Welcome, " + currentUser.getUsername() + " [ADMIN]");

            loadAdminTrains();
            loadAllBookings();
            loadAllPassengers();

            adminTabbedPane.setSelectedIndex(0);
            mainCardLayout.show(mainCardContainer, "ADMIN");
        }
    }

    private JPanel createPassengerView() {
        JPanel panel = new JPanel(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel("RailEase — Passenger Portal");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        passengerGreetingLabel = new JLabel("Welcome, Passenger");
        passengerGreetingLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passengerGreetingLabel.setForeground(new Color(200, 220, 245));

        left.add(titleLabel);
        left.add(passengerGreetingLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        right.setOpaque(false);

        passengerDbStatusLabel = new JLabel("Connected");
        passengerDbStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passengerDbStatusLabel.setForeground(new Color(140, 240, 140));

        JButton logoutBtn = createStyledButton("Logout", new Color(190, 45, 45), Color.WHITE, 95, 30, 12);
        logoutBtn.addActionListener(e -> performLogout());

        right.add(passengerDbStatusLabel);
        right.add(logoutBtn);

        headerPanel.add(left, BorderLayout.WEST);
        headerPanel.add(right, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Passenger Tabs
        passengerTabbedPane = new JTabbedPane();
        passengerTabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        passengerTabbedPane.addTab("Search Trains", createTrainsTab());
        passengerTabbedPane.addTab("Book Ticket", createBookingTab());
        passengerTabbedPane.addTab("PNR Status & Cancellation", createPnrTab());
        passengerTabbedPane.addTab("My Bookings", createMyBookingsTab());

        panel.add(passengerTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMyBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new BorderLayout());
        myBookingsSubtitleLabel = new JLabel("Your Personal Booking History");
        myBookingsSubtitleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        myBookingsSubtitleLabel.setForeground(new Color(30, 40, 60));

        JButton refreshBtn = createStyledButton("Refresh My Bookings", new Color(242, 245, 250), new Color(40, 50, 70), 0, 30, 12);
        refreshBtn.addActionListener(e -> loadMyBookings());

        topBar.add(myBookingsSubtitleLabel, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {
                "Booking ID", "PNR", "Passenger Name", "Phone", "Train Name",
                "From", "To", "Journey Date", "Fare Paid", "Status"
        };
        myBookingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        myBookingsTable = new JTable(myBookingsModel);
        myBookingsTable.setRowHeight(25);
        myBookingsTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = String.valueOf(value);
                if ("CONFIRMED".equalsIgnoreCase(status)) {
                    setForeground(new Color(25, 130, 45));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if ("CANCELLED".equalsIgnoreCase(status)) {
                    setForeground(new Color(185, 40, 40));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setForeground(Color.BLACK);
                }
                return c;
            }
        });

        panel.add(new JScrollPane(myBookingsTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadMyBookings() {
        if (myBookingsModel == null) return;
        myBookingsModel.setRowCount(0);

        if (currentUser == null || currentUser.getPassengerId() == null) {
            if (myBookingsSubtitleLabel != null) {
                myBookingsSubtitleLabel.setText("No passenger profile linked to this account.");
            }
            return;
        }

        int passengerId = currentUser.getPassengerId();
        String name = (currentUser.getPassengerName() != null) ? currentUser.getPassengerName() : currentUser.getUsername();
        if (myBookingsSubtitleLabel != null) {
            myBookingsSubtitleLabel.setText("Showing personal bookings for Passenger #" + passengerId + " (" + name + ")");
        }

        List<Booking> list = bookingDAO.getBookingsByPassengerId(passengerId);
        for (Booking b : list) {
            myBookingsModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getPnr(),
                    b.getPassengerName(),
                    b.getPassengerPhone(),
                    b.getTrainName(),
                    b.getSourceStationName(),
                    b.getDestinationStationName(),
                    b.getJourneyDate(),
                    String.format("₹%.2f", b.getFare()),
                    b.getStatus()
            });
        }
    }

    private JPanel createAdminView() {
        JPanel panel = new JPanel(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(20, 32, 50));
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel("RailEase — Administrator Portal");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        adminGreetingLabel = new JLabel("Welcome, Admin");
        adminGreetingLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        adminGreetingLabel.setForeground(new Color(230, 200, 150));

        left.add(titleLabel);
        left.add(adminGreetingLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        right.setOpaque(false);

        adminDbStatusLabel = new JLabel("Connected");
        adminDbStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        adminDbStatusLabel.setForeground(new Color(140, 240, 140));

        JButton logoutBtn = createStyledButton("Logout", new Color(190, 45, 45), Color.WHITE, 95, 30, 12);
        logoutBtn.addActionListener(e -> performLogout());

        right.add(adminDbStatusLabel);
        right.add(logoutBtn);

        headerPanel.add(left, BorderLayout.WEST);
        headerPanel.add(right, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Admin Tabs
        adminTabbedPane = new JTabbedPane();
        adminTabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        adminTabbedPane.addTab("View All Trains", createAdminTrainsTab());
        adminTabbedPane.addTab("View All Bookings", createAllBookingsTab());
        adminTabbedPane.addTab("View All Passengers", createAllPassengersTab());

        panel.add(adminTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdminTrainsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("All Trains & Operating Schedules in RailEaseDB");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton refreshBtn = createStyledButton("Refresh Trains", new Color(242, 245, 250), new Color(40, 50, 70), 0, 30, 12);
        refreshBtn.addActionListener(e -> loadAdminTrains());

        topBar.add(lbl, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.6);

        String[] trainCols = {"Train #", "Train Name", "From", "To", "Date", "Departure", "Arrival", "Distance", "Classes", "Capacity"};
        adminTrainTableModel = new DefaultTableModel(trainCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        adminTrainTable = new JTable(adminTrainTableModel);
        adminTrainTable.setRowHeight(25);
        adminTrainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        adminTrainTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onAdminTrainSelected();
            }
        });

        JScrollPane topScroll = new JScrollPane(adminTrainTable);
        topScroll.setBorder(BorderFactory.createTitledBorder("Trains (Click row to inspect intermediate route stops)"));
        split.setTopComponent(topScroll);

        String[] routeCols = {"Stop #", "Station Name", "City", "Distance from Source (km)"};
        adminRouteTableModel = new DefaultTableModel(routeCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        adminRouteTable = new JTable(adminRouteTableModel);
        adminRouteTable.setRowHeight(24);

        JScrollPane botScroll = new JScrollPane(adminRouteTable);
        botScroll.setBorder(BorderFactory.createTitledBorder("Intermediate Stops for Selected Train"));
        split.setBottomComponent(botScroll);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void loadAdminTrains() {
        if (adminTrainTableModel == null) return;
        adminTrainTableModel.setRowCount(0);
        if (adminRouteTableModel != null) adminRouteTableModel.setRowCount(0);

        adminTrainList = trainService.getAllTrains();
        for (Train t : adminTrainList) {
            String from = (t.getSourceStationName() != null && !t.getSourceStationName().isEmpty()) ? t.getSourceStationName() : "-";
            String to = (t.getDestinationStationName() != null && !t.getDestinationStationName().isEmpty()) ? t.getDestinationStationName() : "-";
            String date = (t.getJourneyDate() != null && !t.getJourneyDate().isEmpty() && !"null".equalsIgnoreCase(t.getJourneyDate())) ? t.getJourneyDate() : "Scheduled";
            String dep = (t.getDepartureTime() != null && !t.getDepartureTime().isEmpty() && !"null".equalsIgnoreCase(t.getDepartureTime())) ? t.getDepartureTime() : "-";
            String arr = (t.getArrivalTime() != null && !t.getArrivalTime().isEmpty() && !"null".equalsIgnoreCase(t.getArrivalTime())) ? t.getArrivalTime() : "-";
            String dist = (t.getJourneyDistance() > 0) ? String.format("%.1f km", t.getJourneyDistance()) : "-";
            String classes = (t.getAvailableClasses() != null && !t.getAvailableClasses().isEmpty()) ? t.getAvailableClasses() : "-";
            String capacity = t.getTotalSeats() + " seats";

            adminTrainTableModel.addRow(new Object[]{
                    t.getTrainNumber(), t.getTrainName(), from, to, date, dep, arr, dist, classes, capacity
            });
        }
        if (adminTrainTable.getRowCount() > 0) {
            adminTrainTable.setRowSelectionInterval(0, 0);
        }
    }

    private void onAdminTrainSelected() {
        int selectedRow = adminTrainTable.getSelectedRow();
        if (selectedRow < 0 || adminTrainList == null || selectedRow >= adminTrainList.size()) {
            adminRouteTableModel.setRowCount(0);
            return;
        }

        Train train = adminTrainList.get(selectedRow);
        adminRouteTableModel.setRowCount(0);

        List<TrainRoute> routes = train.getRoutes();
        if (routes != null) {
            for (TrainRoute r : routes) {
                adminRouteTableModel.addRow(new Object[]{
                        r.getStopNumber(),
                        r.getStationName(),
                        r.getCity(),
                        r.getDistanceFromSource()
                });
            }
        }
    }

    private JPanel createAllPassengersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("Registered Passengers in RailEaseDB");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton refreshBtn = createStyledButton("Refresh Passengers", new Color(242, 245, 250), new Color(40, 50, 70), 0, 30, 12);
        refreshBtn.addActionListener(e -> loadAllPassengers());

        topBar.add(lbl, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Passenger ID", "Passenger Name", "Age", "Gender", "Phone"};
        allPassengersModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        allPassengersTable = new JTable(allPassengersModel);
        allPassengersTable.setRowHeight(25);
        panel.add(new JScrollPane(allPassengersTable), BorderLayout.CENTER);

        return panel;
    }

    private void loadAllPassengers() {
        if (allPassengersModel == null) return;
        allPassengersModel.setRowCount(0);

        List<Passenger> list = userDAO.getAllPassengers();
        for (Passenger p : list) {
            allPassengersModel.addRow(new Object[]{
                    p.getPassengerId(),
                    p.getName(),
                    p.getAge(),
                    p.getGender(),
                    p.getPhone()
            });
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of your session?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            currentUser = null;
            if (loginUsernameField != null) loginUsernameField.setText("");
            if (loginPasswordField != null) loginPasswordField.setText("");
            mainCardLayout.show(mainCardContainer, "WELCOME");
        }
    }

    private JPanel createTrainsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search Trains Between Stations"));

        filterPanel.add(new JLabel("From:"));
        searchSourceCombo = new JComboBox<>();
        searchSourceCombo.setPreferredSize(new Dimension(160, 28));
        filterPanel.add(searchSourceCombo);

        filterPanel.add(new JLabel("To:"));
        searchDestCombo = new JComboBox<>();
        searchDestCombo.setPreferredSize(new Dimension(160, 28));
        filterPanel.add(searchDestCombo);

        filterPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        searchDateField = new JTextField("2026-11-10", 10);
        searchDateField.setPreferredSize(new Dimension(105, 28));
        filterPanel.add(searchDateField);

        JButton searchBtn = createStyledButton("Find Trains", new Color(24, 100, 190), Color.WHITE, 0, 30, 12);
        searchBtn.addActionListener(e -> performTrainSearch());
        filterPanel.add(searchBtn);

        JButton resetBtn = createStyledButton("Show All Trains", new Color(242, 245, 250), new Color(40, 50, 70), 0, 30, 12);
        resetBtn.addActionListener(e -> loadAllTrains());
        filterPanel.add(resetBtn);

        panel.add(filterPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);

        String[] trainCols = {"Train #", "Train Name", "From", "To", "Date", "Departure", "Arrival", "Distance", "Classes", "Capacity"};
        trainTableModel = new DefaultTableModel(trainCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        trainTable = new JTable(trainTableModel);
        trainTable.setRowHeight(26);
        trainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trainTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTrainSelectedInTable();
            }
        });

        JScrollPane trainScroll = new JScrollPane(trainTable);
        trainScroll.setBorder(BorderFactory.createTitledBorder("Available Trains (Click row to view stops)"));
        splitPane.setTopComponent(trainScroll);

        JPanel bottomContainer = new JPanel(new BorderLayout(5, 5));
        String[] routeCols = {"Stop #", "Station Name", "City", "Distance from Source (km)"};
        routeTableModel = new DefaultTableModel(routeCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        routeTable = new JTable(routeTableModel);
        routeTable.setRowHeight(24);
        JScrollPane routeScroll = new JScrollPane(routeTable);
        routeScroll.setBorder(BorderFactory.createTitledBorder("Intermediate Stops & Route Schedule"));
        bottomContainer.add(routeScroll, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bookSelectedBtn = createStyledButton("Book Selected Train", new Color(30, 140, 60), Color.WHITE, 0, 32, 12);
        bookSelectedBtn.addActionListener(e -> goToBookingWithSelectedTrain());
        actionPanel.add(bookSelectedBtn);
        bottomContainer.add(actionPanel, BorderLayout.SOUTH);

        splitPane.setBottomComponent(bottomContainer);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBookingTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formGrid = new JPanel(new GridLayout(1, 2, 20, 0));

        JPanel journeyPanel = new JPanel();
        journeyPanel.setLayout(new BoxLayout(journeyPanel, BoxLayout.Y_AXIS));
        journeyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(null, "1. Journey Information", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 13)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        bookTrainCombo = new JComboBox<>();
        bookTrainCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                onBookTrainChanged();
            }
        });

        bookSourceCombo = new JComboBox<>();
        bookSourceCombo.addItemListener(e -> updateFareCalculation());

        bookDestCombo = new JComboBox<>();
        bookDestCombo.addItemListener(e -> updateFareCalculation());

        bookClassCombo = new JComboBox<>();
        bookClassCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateFareCalculation();
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        bookDateField = new JTextField(sdf.format(new Date()));
        bookDateField.addActionListener(e -> updateFareCalculation());
        bookDateField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                updateFareCalculation();
            }
        });

        bookDistanceLabel = new JLabel("Distance: 0.0 km");
        bookDistanceLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        bookFareLabel = new JLabel("Calculated Fare: ₹0.00");
        bookFareLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        bookFareLabel.setForeground(new Color(20, 120, 40));

        bookSeatsLabel = new JLabel("Available Seats: -");
        bookSeatsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        bookSeatsLabel.setForeground(new Color(180, 70, 20));

        addFormField(journeyPanel, "Select Train:", bookTrainCombo);
        addFormField(journeyPanel, "Source Station:", bookSourceCombo);
        addFormField(journeyPanel, "Destination Station:", bookDestCombo);
        addFormField(journeyPanel, "Travel Class:", bookClassCombo);
        addFormField(journeyPanel, "Journey Date (YYYY-MM-DD):", bookDateField);

        JPanel calcBox = new JPanel(new GridLayout(3, 1, 4, 4));
        calcBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 230)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        calcBox.setBackground(new Color(245, 248, 252));
        calcBox.add(bookDistanceLabel);
        calcBox.add(bookFareLabel);
        calcBox.add(bookSeatsLabel);

        journeyPanel.add(Box.createVerticalStrut(10));
        journeyPanel.add(calcBox);
        journeyPanel.add(Box.createVerticalGlue());

        JPanel passengerPanel = new JPanel();
        passengerPanel.setLayout(new BoxLayout(passengerPanel, BoxLayout.Y_AXIS));
        passengerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(null, "2. Passenger Registration", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 13)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        passNameField = new JTextField();
        passAgeSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 120, 1));
        passGenderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        passPhoneField = new JTextField();

        addFormField(passengerPanel, "Passenger Full Name:", passNameField);
        addFormField(passengerPanel, "Age:", passAgeSpinner);
        addFormField(passengerPanel, "Gender:", passGenderCombo);
        addFormField(passengerPanel, "Phone Number (10 digits):", passPhoneField);

        JLabel infoNote = new JLabel("<html><font color='#555555'>* Passenger will be automatically registered in the database.<br>* Seat reservation is locked transactionally.</font></html>");
        passengerPanel.add(Box.createVerticalStrut(10));
        passengerPanel.add(infoNote);
        passengerPanel.add(Box.createVerticalGlue());

        formGrid.add(journeyPanel);
        formGrid.add(passengerPanel);
        mainPanel.add(formGrid, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton bookSubmitBtn = createStyledButton("Proceed to Payment", new Color(24, 100, 190), Color.WHITE, 200, 38, 14);
        bookSubmitBtn.addActionListener(e -> proceedToPayment());
        bottomPanel.add(bookSubmitBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void addFormField(JPanel panel, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(component);
        panel.add(Box.createVerticalStrut(10));
    }

    private JPanel createPnrTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        searchBar.setBorder(BorderFactory.createTitledBorder("Search Booking by PNR"));

        searchBar.add(new JLabel("Enter PNR Number:"));
        pnrSearchField = new JTextField(14);
        pnrSearchField.setFont(new Font("Monospaced", Font.BOLD, 14));
        pnrSearchField.addActionListener(e -> searchPnrStatus());
        searchBar.add(pnrSearchField);

        JButton findPnrBtn = createStyledButton("Check Status", new Color(24, 100, 190), Color.WHITE, 0, 30, 12);
        findPnrBtn.addActionListener(e -> searchPnrStatus());
        searchBar.add(findPnrBtn);

        panel.add(searchBar, BorderLayout.NORTH);

        // Center Content Container (Scrollable)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 1. Booking Details Card
        JPanel bookingCard = createSectionCard("1. Booking Details");
        pnrValueLabel = createValueLabel("-", true);
        pnrValueLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
        pnrBookingIdLabel = createValueLabel("-", false);
        pnrStatusLabel = createValueLabel("-", true);
        pnrBookingTimeLabel = createValueLabel("-", false);
        pnrFareLabel = createValueLabel("-", true);

        addSectionRow(bookingCard, 0, "PNR Number:", pnrValueLabel, "Booking ID:", pnrBookingIdLabel);
        addSectionRow(bookingCard, 1, "Booking Status:", pnrStatusLabel, "Booking Date/Time:", pnrBookingTimeLabel);
        addSectionRow(bookingCard, 2, "Total Fare:", pnrFareLabel, null, null);
        contentPanel.add(bookingCard);
        contentPanel.add(Box.createVerticalStrut(10));

        // 2. Journey Details Card
        JPanel journeyCard = createSectionCard("2. Journey Details");
        pnrTrainLabel = createValueLabel("-", true);
        pnrClassLabel = createValueLabel("-", false);
        pnrRouteLabel = createValueLabel("-", false);
        pnrDateLabel = createValueLabel("-", false);
        pnrTimeLabel = createValueLabel("-", false);

        addSectionRow(journeyCard, 0, "Train:", pnrTrainLabel, "Travel Class:", pnrClassLabel);
        addSectionRow(journeyCard, 1, "Journey Route:", pnrRouteLabel, "Journey Date:", pnrDateLabel);
        addSectionRow(journeyCard, 2, "Timings:", pnrTimeLabel, null, null);
        contentPanel.add(journeyCard);
        contentPanel.add(Box.createVerticalStrut(10));

        // 3. Passenger Details Card
        JPanel passengerCard = createSectionCard("3. Passenger Details");
        pnrPassengerLabel = createValueLabel("-", false);
        pnrPassengersContainer = new JPanel();
        pnrPassengersContainer.setLayout(new BoxLayout(pnrPassengersContainer, BoxLayout.Y_AXIS));
        pnrPassengersContainer.setOpaque(false);

        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.insets = new Insets(4, 10, 4, 10);
        gbcPass.anchor = GridBagConstraints.WEST;
        gbcPass.fill = GridBagConstraints.HORIZONTAL;

        gbcPass.gridx = 0; gbcPass.gridy = 0;
        passengerCard.add(createTitleLabel("Primary Booker:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 0; gbcPass.gridwidth = 3;
        passengerCard.add(pnrPassengerLabel, gbcPass);

        gbcPass.gridx = 0; gbcPass.gridy = 1; gbcPass.gridwidth = 1;
        passengerCard.add(createTitleLabel("Booked Passengers:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 1; gbcPass.gridwidth = 3;
        passengerCard.add(pnrPassengersContainer, gbcPass);

        contentPanel.add(passengerCard);
        contentPanel.add(Box.createVerticalStrut(10));

        // 4. Payment Details Card
        JPanel paymentCard = createSectionCard("4. Payment Details");
        pnrPaymentNoticeLabel = new JLabel("No payment record found for this booking.");
        pnrPaymentNoticeLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        pnrPaymentNoticeLabel.setForeground(new Color(170, 90, 20));
        pnrPaymentNoticeLabel.setVisible(false);

        pnrPaymentStatusLabel = createValueLabel("-", true);
        pnrPaymentAmountLabel = createValueLabel("-", false);
        pnrPaymentMethodLabel = createValueLabel("-", false);
        pnrPaymentTxnLabel = createValueLabel("-", false);
        pnrPaymentDateLabel = createValueLabel("-", false);

        GridBagConstraints gbcPay = new GridBagConstraints();
        gbcPay.insets = new Insets(4, 10, 4, 10);
        gbcPay.anchor = GridBagConstraints.WEST;
        gbcPay.gridx = 0; gbcPay.gridy = 0; gbcPay.gridwidth = 4;
        paymentCard.add(pnrPaymentNoticeLabel, gbcPay);

        addSectionRow(paymentCard, 1, "Payment Status:", pnrPaymentStatusLabel, "Payment Amount:", pnrPaymentAmountLabel);
        addSectionRow(paymentCard, 2, "Payment Method:", pnrPaymentMethodLabel, "Transaction Ref:", pnrPaymentTxnLabel);
        addSectionRow(paymentCard, 3, "Payment Date:", pnrPaymentDateLabel, null, null);

        contentPanel.add(paymentCard);
        contentPanel.add(Box.createVerticalStrut(10));

        // 5. Cancellation & Refund Details Card
        JPanel cancelCard = createSectionCard("5. Cancellation & Refund Details");
        pnrCancelStatusLabel = createValueLabel("-", true);
        pnrRefundAmountLabel = createValueLabel("-", true);
        pnrCancelDateLabel = createValueLabel("-", false);
        pnrCancelReasonLabel = createValueLabel("-", false);

        addSectionRow(cancelCard, 0, "Cancellation Status:", pnrCancelStatusLabel, "Refund Amount:", pnrRefundAmountLabel);
        addSectionRow(cancelCard, 1, "Cancellation Date:", pnrCancelDateLabel, "Cancellation Reason:", pnrCancelReasonLabel);

        contentPanel.add(cancelCard);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        cancelTicketButton = createStyledButton("Cancel Ticket", new Color(195, 40, 40), Color.WHITE, 140, 34, 13);
        cancelTicketButton.setEnabled(false);
        cancelTicketButton.addActionListener(e -> cancelCurrentTicket());
        actionPanel.add(cancelTicketButton);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSectionCard(String title) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(210, 215, 222)),
                        title,
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        new Font("SansSerif", Font.BOLD, 13),
                        new Color(24, 85, 160)
                ),
                new EmptyBorder(8, 12, 8, 12)
        ));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JLabel createTitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(90, 95, 105));
        return lbl;
    }

    private JLabel createValueLabel(String text, boolean bold) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, 13));
        lbl.setForeground(new Color(30, 30, 30));
        return lbl;
    }

    private void addSectionRow(JPanel panel, int row, String title1, JLabel val1, String title2, JLabel val2) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        panel.add(createTitleLabel(title1), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        panel.add(val1, gbc);

        if (title2 != null && val2 != null) {
            gbc.gridx = 2;
            gbc.weightx = 0.0;
            panel.add(createTitleLabel(title2), gbc);

            gbc.gridx = 3;
            gbc.weightx = 0.5;
            panel.add(val2, gbc);
        }
    }

    private JPanel createAllBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton refreshBtn = createStyledButton("Refresh Bookings", new Color(242, 245, 250), new Color(40, 50, 70), 0, 30, 12);
        refreshBtn.addActionListener(e -> loadAllBookings());
        topBar.add(refreshBtn);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "PNR", "Passenger", "Phone", "Train", "Source", "Destination", "Date", "Fare", "Status"};
        allBookingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        allBookingsTable = new JTable(allBookingsModel);
        allBookingsTable.setRowHeight(25);

        allBookingsTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = String.valueOf(value);
                if ("CONFIRMED".equalsIgnoreCase(status)) {
                    setForeground(new Color(25, 130, 45));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if ("CANCELLED".equalsIgnoreCase(status)) {
                    setForeground(new Color(185, 40, 40));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setForeground(Color.BLACK);
                }
                return c;
            }
        });

        panel.add(new JScrollPane(allBookingsTable), BorderLayout.CENTER);
        return panel;
    }

    private void updateDatabaseStatus(boolean connected) {
        String text = connected ? "Connected: " + DBConnection.getDatabaseName() : "Disconnected";
        Color color = connected ? new Color(140, 240, 140) : new Color(255, 120, 120);

        if (welcomeDbStatusLabel != null) {
            welcomeDbStatusLabel.setText("Database: " + text);
            welcomeDbStatusLabel.setForeground(connected ? new Color(20, 120, 40) : new Color(180, 40, 40));
        }
        if (passengerDbStatusLabel != null) {
            passengerDbStatusLabel.setText(text);
            passengerDbStatusLabel.setForeground(color);
        }
        if (adminDbStatusLabel != null) {
            adminDbStatusLabel.setText(text);
            adminDbStatusLabel.setForeground(color);
        }
        if (dbStatusLabel != null) {
            dbStatusLabel.setText(text);
            dbStatusLabel.setForeground(color);
        }
    }

    private void checkDatabaseAndLoadData() {
        boolean connected = DBConnection.testConnection();
        updateDatabaseStatus(connected);
        if (connected) {
            userDAO.initializeDemoPasswordsIfPlaceholders();
            loadAllStations();
            loadAllTrains();
            loadAllBookings();
        } else {
            SwingUtilities.invokeLater(() -> {
                int option = JOptionPane.showConfirmDialog(
                        this,
                        "Could not connect to MySQL database '" + DBConnection.getDatabaseName() + "'.\n"
                                + "Current User: " + DBConnection.getUsername() + "\n\n"
                                + "Would you like to configure your MySQL connection credentials now?",
                        "Database Connection Notice",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (option == JOptionPane.YES_OPTION) {
                    showDatabaseConfigDialog();
                }
            });
        }
    }

    private void showDatabaseConfigDialog() {
        JTextField userField = new JTextField(DBConnection.getUsername());
        JPasswordField passField = new JPasswordField(DBConnection.getPassword());
        JTextField dbField = new JTextField(DBConnection.getDatabaseName());

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("MySQL Username:"));
        panel.add(userField);
        panel.add(new JLabel("MySQL Password:"));
        panel.add(passField);
        panel.add(new JLabel("Database Name:"));
        panel.add(dbField);

        int result = JOptionPane.showConfirmDialog(this, panel, "MySQL Connection Settings", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            DBConnection.setUsername(userField.getText().trim());
            DBConnection.setPassword(new String(passField.getPassword()));
            DBConnection.setDatabaseName(dbField.getText().trim());

            if (DBConnection.testConnection()) {
                updateDatabaseStatus(true);
                JOptionPane.showMessageDialog(this, "Connected to database successfully.", "Connection Successful", JOptionPane.INFORMATION_MESSAGE);
                userDAO.initializeDemoPasswordsIfPlaceholders();
                loadAllStations();
                loadAllTrains();
                loadAllBookings();
            } else {
                updateDatabaseStatus(false);
                JOptionPane.showMessageDialog(this, "Connection failed. Please check MySQL username and password.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadAllStations() {
        List<Station> stations = trainService.getAllStations();
        searchSourceCombo.removeAllItems();
        searchDestCombo.removeAllItems();

        for (Station s : stations) {
            searchSourceCombo.addItem(s);
            searchDestCombo.addItem(s);
        }
    }

    private void loadAllTrains() {
        currentTrainList = trainService.getAllTrains();
        displayTrainsInTable(currentTrainList);
        populateBookTrainDropdown(currentTrainList);
    }

    private void displayTrainsInTable(List<Train> trains) {
        trainTableModel.setRowCount(0);
        routeTableModel.setRowCount(0);
        for (Train t : trains) {
            String from = (t.getSourceStationName() != null && !t.getSourceStationName().isEmpty())
                    ? t.getSourceStationName() : "-";
            String to = (t.getDestinationStationName() != null && !t.getDestinationStationName().isEmpty())
                    ? t.getDestinationStationName() : "-";
            String date = (t.getJourneyDate() != null && !t.getJourneyDate().isEmpty() && !"null".equalsIgnoreCase(t.getJourneyDate()))
                    ? t.getJourneyDate() : "Scheduled";
            String dep = (t.getDepartureTime() != null && !t.getDepartureTime().isEmpty() && !"null".equalsIgnoreCase(t.getDepartureTime()))
                    ? t.getDepartureTime() : "-";
            String arr = (t.getArrivalTime() != null && !t.getArrivalTime().isEmpty() && !"null".equalsIgnoreCase(t.getArrivalTime()))
                    ? t.getArrivalTime() : "-";
            String dist = (t.getJourneyDistance() > 0)
                    ? String.format("%.1f km", t.getJourneyDistance()) : "-";
            String classes = (t.getAvailableClasses() != null && !t.getAvailableClasses().isEmpty())
                    ? t.getAvailableClasses() : "-";
            String capacity = t.getTotalSeats() + " seats";

            trainTableModel.addRow(new Object[]{
                    t.getTrainNumber(),
                    t.getTrainName(),
                    from,
                    to,
                    date,
                    dep,
                    arr,
                    dist,
                    classes,
                    capacity
            });
        }
        if (trainTable.getRowCount() > 0) {
            trainTable.setRowSelectionInterval(0, 0);
        }
    }

    private void populateBookTrainDropdown(List<Train> trains) {
        bookTrainCombo.removeAllItems();
        for (Train t : trains) {
            bookTrainCombo.addItem(t);
        }
        onBookTrainChanged();
    }

    private void performTrainSearch() {
        Station src = (Station) searchSourceCombo.getSelectedItem();
        Station dst = (Station) searchDestCombo.getSelectedItem();

        if (src == null || dst == null) {
            JOptionPane.showMessageDialog(this, "Please select both source and destination stations.", "Search Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (src.getStationId() == dst.getStationId()) {
            JOptionPane.showMessageDialog(this, "Source and Destination cannot be the same station.", "Search Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateStr = (searchDateField != null) ? searchDateField.getText().trim() : "";
        if (dateStr.isEmpty() || !dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid journey date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Train> results = trainService.searchTrains(src.getStationId(), dst.getStationId(), dateStr);
        currentTrainList = results;
        displayTrainsInTable(results);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No trains found operating from " + src.getStationName() + " to " + dst.getStationName() + " on " + dateStr + ".\n"
                    + "(Trains must cover these stations in order and have a schedule matching this date).",
                    "No Trains Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onTrainSelectedInTable() {
        int selectedRow = trainTable.getSelectedRow();
        if (selectedRow < 0 || currentTrainList == null || selectedRow >= currentTrainList.size()) {
            routeTableModel.setRowCount(0);
            return;
        }

        Train train = currentTrainList.get(selectedRow);
        routeTableModel.setRowCount(0);

        List<TrainRoute> routes = train.getRoutes();
        if (routes != null) {
            for (TrainRoute r : routes) {
                routeTableModel.addRow(new Object[]{
                        r.getStopNumber(),
                        r.getStationName(),
                        r.getCity(),
                        r.getDistanceFromSource()
                });
            }
        }
    }

    private void goToBookingWithSelectedTrain() {
        int selectedRow = trainTable.getSelectedRow();
        if (selectedRow < 0 || currentTrainList == null || selectedRow >= currentTrainList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a train from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Train selectedTrain = currentTrainList.get(selectedRow);
        if (passengerTabbedPane != null) {
            passengerTabbedPane.setSelectedIndex(1);
        }

        for (int i = 0; i < bookTrainCombo.getItemCount(); i++) {
            Train t = bookTrainCombo.getItemAt(i);
            if (t.getTrainId() == selectedTrain.getTrainId()) {
                bookTrainCombo.setSelectedIndex(i);
                break;
            }
        }

        Station src = (Station) searchSourceCombo.getSelectedItem();
        Station dst = (Station) searchDestCombo.getSelectedItem();
        String dateStr = (searchDateField != null) ? searchDateField.getText().trim() : "";
        if (selectedTrain.getJourneyDate() != null && !selectedTrain.getJourneyDate().isEmpty() && !"null".equalsIgnoreCase(selectedTrain.getJourneyDate())) {
            dateStr = selectedTrain.getJourneyDate();
        }
        if (dateStr != null && !dateStr.isEmpty() && dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            bookDateField.setText(dateStr);
        }

        if (src != null) {
            for (int i = 0; i < bookSourceCombo.getItemCount(); i++) {
                Station s = bookSourceCombo.getItemAt(i);
                if (s.getStationId() == src.getStationId() || s.getStationName().equalsIgnoreCase(src.getStationName())) {
                    bookSourceCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (dst != null) {
            for (int i = 0; i < bookDestCombo.getItemCount(); i++) {
                Station s = bookDestCombo.getItemAt(i);
                if (s.getStationId() == dst.getStationId() || s.getStationName().equalsIgnoreCase(dst.getStationName())) {
                    bookDestCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        updateFareCalculation();
    }

    private void onBookTrainChanged() {
        Train train = (Train) bookTrainCombo.getSelectedItem();
        bookSourceCombo.removeAllItems();
        bookDestCombo.removeAllItems();

        if (train == null) return;

        List<TrainRoute> routes = train.getRoutes();
        if (routes == null || routes.isEmpty()) {
            routes = trainService.getRoutesForTrain(train.getTrainId());
            train.setRoutes(routes);
        }

        for (TrainRoute r : routes) {
            Station s = new Station(r.getStationId(), r.getStationName(), r.getCity());
            bookSourceCombo.addItem(s);
            bookDestCombo.addItem(s);
        }

        if (bookSourceCombo.getItemCount() > 0) {
            bookSourceCombo.setSelectedIndex(0);
        }
        if (bookDestCombo.getItemCount() > 1) {
            bookDestCombo.setSelectedIndex(bookDestCombo.getItemCount() - 1);
        }

        if (bookClassCombo != null) {
            bookClassCombo.removeAllItems();
            TrainDAO tDao = (trainService instanceof TrainDAO) ? (TrainDAO) trainService : new TrainDAO();
            List<TrainDAO.TrainClassItem> classes = tDao.getClassesForTrain(train.getTrainId());
            for (TrainDAO.TrainClassItem c : classes) {
                bookClassCombo.addItem(c);
            }
            if (bookClassCombo.getItemCount() > 0) {
                bookClassCombo.setSelectedIndex(0);
            }
        }

        updateFareCalculation();
    }

    public static double calculateFare(double distance, TrainDAO.TrainClassItem trainClass) {
        if (distance <= 0) return 50.0;
        double classMultiplier = 1.0;
        if (trainClass != null && trainClass.getClassName() != null) {
            String cName = trainClass.getClassName().toUpperCase().trim();
            if (cName.contains("1A") || cName.contains("EC") || cName.contains("FIRST")) {
                classMultiplier = 2.2;
            } else if (cName.contains("2A") || cName.contains("2 TIER")) {
                classMultiplier = 1.75;
            } else if (cName.contains("3A") || cName.contains("3 TIER")) {
                classMultiplier = 1.35;
            } else if (cName.contains("CC") || cName.contains("CHAIR")) {
                classMultiplier = 1.20;
            } else if (cName.contains("SL") || cName.contains("SLEEPER")) {
                classMultiplier = 1.0;
            } else if (cName.contains("2S") || cName.contains("SECOND")) {
                classMultiplier = 0.85;
            }
        }
        double rawFare = 50.0 + (distance * 1.25 * classMultiplier);
        return Math.round(rawFare * 100.0) / 100.0;
    }

    private void updateFareCalculation() {
        Train train = (Train) bookTrainCombo.getSelectedItem();
        Station src = (Station) bookSourceCombo.getSelectedItem();
        Station dst = (Station) bookDestCombo.getSelectedItem();

        if (train == null || src == null || dst == null) {
            bookDistanceLabel.setText("Distance: 0.0 km");
            bookFareLabel.setText("Calculated Fare: ₹0.00");
            bookSeatsLabel.setText("Available Seats: -");
            return;
        }

        TrainRoute srcRoute = train.getRouteForStation(src.getStationId());
        TrainRoute dstRoute = train.getRouteForStation(dst.getStationId());

        if (srcRoute != null && dstRoute != null && srcRoute.getStopNumber() < dstRoute.getStopNumber()) {
            double distance = Math.abs(dstRoute.getDistanceFromSource() - srcRoute.getDistanceFromSource());
            TrainDAO.TrainClassItem selectedClass = (bookClassCombo != null) ? (TrainDAO.TrainClassItem) bookClassCombo.getSelectedItem() : null;
            double fare = calculateFare(distance, selectedClass);

            bookDistanceLabel.setText(String.format("Distance: %.1f km (Stop %d -> Stop %d)", distance, srcRoute.getStopNumber(), dstRoute.getStopNumber()));
            bookDistanceLabel.setForeground(new Color(30, 80, 160));
            bookFareLabel.setText(String.format("Calculated Fare: ₹%.2f", fare));

            String journeyDate = bookDateField.getText().trim();
            int avail = trainService.getAvailableSeats(train.getTrainId(), journeyDate);
            bookSeatsLabel.setText("Available Seats: " + avail + " / " + train.getTotalSeats());
            bookSeatsLabel.setForeground(avail > 0 ? new Color(20, 120, 40) : Color.RED);
        } else {
            bookDistanceLabel.setText("Invalid Stop Order (Source must be before Destination)");
            bookDistanceLabel.setForeground(Color.RED);
            bookFareLabel.setText("Calculated Fare: N/A");
            bookSeatsLabel.setText("Available Seats: -");
        }
    }

    private void proceedToPayment() {
        Train train = (Train) bookTrainCombo.getSelectedItem();
        Station src = (Station) bookSourceCombo.getSelectedItem();
        Station dst = (Station) bookDestCombo.getSelectedItem();
        String dateStr = bookDateField.getText().trim();

        if (train == null || src == null || dst == null) {
            JOptionPane.showMessageDialog(this, "Please select a train and stations.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (src.getStationId() == dst.getStationId()) {
            JOptionPane.showMessageDialog(this, "Source and Destination cannot be the same station.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!train.isValidRoute(src.getStationId(), dst.getStationId())) {
            JOptionPane.showMessageDialog(this, "Invalid Route: Destination must come after Source on this train route.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dateStr.isEmpty() || !dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid journey date in YYYY-MM-DD format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int avail = trainService.getAvailableSeats(train.getTrainId(), dateStr);
        if (avail <= 0) {
            JOptionPane.showMessageDialog(this,
                    "No seats available on " + train.getTrainName() + " for date " + dateStr + ".\nPlease select another date or train.",
                    "Seats Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String passName = passNameField.getText().trim();
        if (passName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the passenger's full name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String phone = passPhoneField.getText().trim();
        if (phone.isEmpty() || !phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit mobile phone number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int age = (Integer) passAgeSpinner.getValue();
        String gender = (String) passGenderCombo.getSelectedItem();

        double distance = train.getDistanceBetween(src.getStationId(), dst.getStationId());
        TrainDAO.TrainClassItem selectedClass = (bookClassCombo != null) ? (TrainDAO.TrainClassItem) bookClassCombo.getSelectedItem() : null;
        double fare = calculateFare(distance, selectedClass);

        Passenger passenger = new Passenger(passName, age, gender, phone);
        if (currentUser != null && currentUser.getPassengerId() != null) {
            passenger.setPassengerId(currentUser.getPassengerId());
        }

        showSimulatedPaymentDialog(train, src, dst, dateStr, selectedClass, passenger, distance, fare);
    }

    private void showSimulatedPaymentDialog(Train train, Station src, Station dst, String dateStr,
                                            TrainDAO.TrainClassItem selectedClass, Passenger passenger,
                                            double distance, double fare) {

        JDialog paymentDialog = new JDialog(this, "Simulated Payment Checkout — RailEase Demo", true);
        paymentDialog.setLayout(new BorderLayout(12, 12));
        paymentDialog.setSize(520, 600);
        paymentDialog.setLocationRelativeTo(this);
        paymentDialog.setResizable(false);

        // Header Panel with Simulation Notice
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(12, 16, 6, 16));
        headerPanel.setBackground(new Color(242, 246, 252));

        JLabel titleLbl = new JLabel("Simulated Payment Gateway");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLbl.setForeground(new Color(24, 85, 160));
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(titleLbl);

        JLabel subTitleLbl = new JLabel("College Project Simulation • No Real Money or Banking Credentials Required");
        subTitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subTitleLbl.setForeground(new Color(90, 100, 115));
        subTitleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(subTitleLbl);
        headerPanel.add(Box.createVerticalStrut(8));

        JLabel noticeLbl = new JLabel("<html><div style='background-color:#fff3cd; border:1px solid #ffeeba; padding:6px 10px; border-radius:4px; color:#856404; font-size:11px;'>"
                + "<b>⚠️ Demo Notice:</b> This simulated payment flow is built for testing and demonstration.<br>"
                + "No real payment gateway is contacted. <b>Do not enter actual bank passwords, CVVs, or PINs.</b>"
                + "</div></html>");
        noticeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(noticeLbl);

        paymentDialog.add(headerPanel, BorderLayout.NORTH);

        // Center Content: Summary Card + Method Card
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(6, 16, 6, 16));

        // 1. Summary Card
        JPanel summaryCard = new JPanel(new GridBagLayout());
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(210, 215, 222)),
                        "Booking Summary",
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        new Font("SansSerif", Font.BOLD, 12),
                        new Color(24, 85, 160)
                ),
                new EmptyBorder(6, 10, 6, 10)
        ));
        summaryCard.setBackground(Color.WHITE);
        summaryCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addSummaryField(summaryCard, gbc, 0, "Train:", train.getTrainName() + " (" + train.getTrainNumber() + ")");
        addSummaryField(summaryCard, gbc, 1, "Route:", src.getStationName() + " -> " + dst.getStationName() + String.format(" (%.1f km)", distance));
        addSummaryField(summaryCard, gbc, 2, "Journey Date:", dateStr);
        String classDesc = (selectedClass != null) ? selectedClass.getClassName() + (selectedClass.getDescription() != null ? " - " + selectedClass.getDescription() : "") : "Standard";
        addSummaryField(summaryCard, gbc, 3, "Travel Class:", classDesc);
        addSummaryField(summaryCard, gbc, 4, "Main Passenger:", passenger.getPassengerName() + " (" + passenger.getGender() + ", Age " + passenger.getAge() + ", Ph: " + passenger.getPhone() + ")");

        gbc.gridy = 5;
        gbc.gridx = 0;
        JLabel fareTitle = new JLabel("Total Fare Payable:");
        fareTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        fareTitle.setForeground(new Color(60, 60, 60));
        summaryCard.add(fareTitle, gbc);

        gbc.gridx = 1;
        JLabel fareVal = new JLabel(String.format("₹%.2f", fare));
        fareVal.setFont(new Font("SansSerif", Font.BOLD, 18));
        fareVal.setForeground(new Color(25, 140, 50));
        summaryCard.add(fareVal, gbc);

        centerPanel.add(summaryCard);
        centerPanel.add(Box.createVerticalStrut(10));

        // 2. Payment Method Card
        JPanel methodCard = new JPanel();
        methodCard.setLayout(new BoxLayout(methodCard, BoxLayout.Y_AXIS));
        methodCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(210, 215, 222)),
                        "Select Payment Method",
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        new Font("SansSerif", Font.BOLD, 12),
                        new Color(24, 85, 160)
                ),
                new EmptyBorder(8, 12, 8, 12)
        ));
        methodCard.setBackground(Color.WHITE);
        methodCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JRadioButton upiRadio = new JRadioButton("UPI (Google Pay, PhonePe, Paytm, BHIM, Any UPI ID)", true);
        upiRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        upiRadio.setOpaque(false);

        JRadioButton cardRadio = new JRadioButton("Credit / Debit / ATM Card (Visa, MasterCard, RuPay)");
        cardRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cardRadio.setOpaque(false);

        JRadioButton nbRadio = new JRadioButton("Net Banking (SBI, HDFC, ICICI, Axis, PNB & 50+ Banks)");
        nbRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        nbRadio.setOpaque(false);

        ButtonGroup methodGroup = new ButtonGroup();
        methodGroup.add(upiRadio);
        methodGroup.add(cardRadio);
        methodGroup.add(nbRadio);

        methodCard.add(upiRadio);
        methodCard.add(Box.createVerticalStrut(4));
        methodCard.add(cardRadio);
        methodCard.add(Box.createVerticalStrut(4));
        methodCard.add(nbRadio);
        methodCard.add(Box.createVerticalStrut(10));

        JLabel helperNote = new JLabel("<html><font color='#666666' size='2'>* Clicking 'Pay & Book Ticket' simulates an immediate payment confirmation and reserves your seat.</font></html>");
        methodCard.add(helperNote);

        centerPanel.add(methodCard);
        paymentDialog.add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        actionPanel.setBorder(new EmptyBorder(0, 12, 10, 16));

        JButton cancelBtn = createStyledButton("Cancel / Back", new Color(242, 245, 250), new Color(40, 50, 70), 130, 36, 13);
        cancelBtn.addActionListener(e -> paymentDialog.dispose());
        actionPanel.add(cancelBtn);

        JButton payBtn = createStyledButton(String.format("Pay & Book Ticket (₹%.2f)", fare), new Color(25, 135, 50), Color.WHITE, 0, 36, 13);
        payBtn.addActionListener(e -> {
            payBtn.setEnabled(false);
            String selectedMethod = "UPI";
            if (cardRadio.isSelected()) {
                selectedMethod = "Card";
            } else if (nbRadio.isSelected()) {
                selectedMethod = "Net Banking";
            }

            Integer classId = (selectedClass != null) ? selectedClass.getClassId() : null;
            try {
                Booking booking = bookingDAO.bookTicket(passenger, train.getTrainId(), src.getStationId(), dst.getStationId(), dateStr, classId, fare, selectedMethod);
                paymentDialog.dispose();

                String successMsg = "Ticket Booked & Paid Successfully!\n\n"
                        + "PNR Number          : " + booking.getPnr() + "\n"
                        + "Booking ID          : #" + booking.getBookingId() + "\n"
                        + "Payment Status      : SUCCESS (Simulated)\n"
                        + "Payment Method      : " + selectedMethod + "\n"
                        + "Demo Txn Reference  : " + booking.getTransactionReference() + "\n"
                        + "Total Fare Paid     : ₹" + String.format("%.2f", fare) + "\n\n"
                        + "Passenger Name      : " + passenger.getPassengerName() + " (" + passenger.getGender() + ", Age: " + passenger.getAge() + ")\n"
                        + "Train               : " + train.getTrainName() + " (" + train.getTrainNumber() + ")"
                        + (selectedClass != null ? " [" + selectedClass.getClassName() + "]" : "") + "\n"
                        + "Journey Route       : " + src.getStationName() + " -> " + dst.getStationName() + "\n"
                        + "Journey Date        : " + dateStr + "\n"
                        + "Booking Status      : " + booking.getStatus() + "\n\n"
                        + "A confirmed ticket and payment record have been saved in RailEaseDB.";

                JOptionPane.showMessageDialog(MainGUI.this, successMsg, "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

                if (currentUser != null && currentUser.getPassengerName() != null) {
                    passNameField.setText(currentUser.getPassengerName());
                    passPhoneField.setText(currentUser.getPassengerPhone() != null ? currentUser.getPassengerPhone() : "");
                } else {
                    passNameField.setText("");
                    passPhoneField.setText("");
                }
                updateFareCalculation();
                loadAllBookings();
                loadMyBookings();
                pnrSearchField.setText(booking.getPnr());

                int viewChoice = JOptionPane.showConfirmDialog(
                        MainGUI.this,
                        "Would you like to view this ticket in the PNR Status tab now?",
                        "View PNR Details",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (viewChoice == JOptionPane.YES_OPTION) {
                    if (passengerTabbedPane != null) {
                        passengerTabbedPane.setSelectedIndex(2);
                    }
                    searchPnrStatus();
                }

            } catch (IllegalStateException ise) {
                payBtn.setEnabled(true);
                JOptionPane.showMessageDialog(paymentDialog, ise.getMessage(), "Booking Notice", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException | IllegalArgumentException ex) {
                payBtn.setEnabled(true);
                JOptionPane.showMessageDialog(paymentDialog, "Booking / Payment Failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        actionPanel.add(payBtn);

        paymentDialog.add(actionPanel, BorderLayout.SOUTH);
        paymentDialog.setVisible(true);
    }

    private void addSummaryField(JPanel panel, GridBagConstraints gbc, int row, String title, String value) {
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(90, 95, 105));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        valLbl.setForeground(new Color(30, 30, 30));
        panel.add(valLbl, gbc);
    }

    private void searchPnrStatus() {
        String pnr = pnrSearchField.getText().trim();
        if (pnr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a PNR number to search.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking booking = bookingDAO.getBookingByPNR(pnr);
        currentPnrBooking = booking;

        if (booking == null) {
            pnrValueLabel.setText("NOT FOUND");
            pnrValueLabel.setForeground(Color.RED);
            pnrBookingIdLabel.setText("-");
            pnrStatusLabel.setText("-");
            pnrStatusLabel.setForeground(Color.BLACK);
            pnrBookingTimeLabel.setText("-");
            pnrFareLabel.setText("-");

            pnrTrainLabel.setText("-");
            pnrRouteLabel.setText("-");
            pnrDateLabel.setText("-");
            pnrTimeLabel.setText("-");
            pnrClassLabel.setText("-");

            pnrPassengerLabel.setText("-");
            pnrPassengersContainer.removeAll();
            pnrPassengersContainer.revalidate();
            pnrPassengersContainer.repaint();

            pnrPaymentNoticeLabel.setVisible(false);
            pnrPaymentStatusLabel.setText("-");
            pnrPaymentStatusLabel.setForeground(Color.BLACK);
            pnrPaymentAmountLabel.setText("-");
            pnrPaymentMethodLabel.setText("-");
            pnrPaymentTxnLabel.setText("-");
            pnrPaymentDateLabel.setText("-");

            pnrCancelStatusLabel.setText("-");
            pnrCancelStatusLabel.setForeground(Color.BLACK);
            pnrCancelDateLabel.setText("-");
            pnrCancelReasonLabel.setText("-");
            pnrRefundAmountLabel.setText("-");

            cancelTicketButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "No booking found matching PNR: " + pnr, "PNR Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 1. Booking Details
        pnrValueLabel.setText(booking.getPnr());
        pnrValueLabel.setForeground(new Color(24, 85, 160));
        pnrBookingIdLabel.setText("#" + booking.getBookingId());
        pnrStatusLabel.setText(booking.getStatus());
        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            pnrStatusLabel.setForeground(new Color(25, 140, 50));
            cancelTicketButton.setEnabled(true);
        } else {
            pnrStatusLabel.setForeground(new Color(190, 40, 40));
            cancelTicketButton.setEnabled(false);
        }
        String bTime = booking.getBookingTime();
        pnrBookingTimeLabel.setText(bTime != null && !bTime.isEmpty() && !"null".equalsIgnoreCase(bTime) ? bTime : "-");
        pnrFareLabel.setText(String.format("₹%.2f", booking.getFare()));

        // 2. Journey Details
        pnrTrainLabel.setText(booking.getTrainName() + " (#" + booking.getTrainNumber() + ")");
        pnrRouteLabel.setText(booking.getSourceStationName() + " -> " + booking.getDestinationStationName());
        pnrDateLabel.setText(booking.getJourneyDate() != null ? booking.getJourneyDate() : "-");
        String dep = (booking.getDepartureTime() != null && !"null".equalsIgnoreCase(booking.getDepartureTime())) ? booking.getDepartureTime() : "-";
        String arr = (booking.getArrivalTime() != null && !"null".equalsIgnoreCase(booking.getArrivalTime())) ? booking.getArrivalTime() : "-";
        pnrTimeLabel.setText("Dept: " + dep + " | Arrv: " + arr);

        String travelClass = (booking.getClassName() != null && !booking.getClassName().isEmpty()) ? booking.getClassName() : "-";
        if (booking.getClassDescription() != null && !booking.getClassDescription().isEmpty() && !"null".equalsIgnoreCase(booking.getClassDescription())) {
            travelClass += " (" + booking.getClassDescription() + ")";
        }
        pnrClassLabel.setText(travelClass);

        // 3. Passenger Details
        String primaryPass = booking.getPassengerName() + " (Phone: " + booking.getPassengerPhone();
        if (booking.getPassengerAge() > 0) {
            primaryPass += ", Age: " + booking.getPassengerAge();
        }
        if (booking.getPassengerGender() != null && !booking.getPassengerGender().isEmpty() && !"null".equalsIgnoreCase(booking.getPassengerGender())) {
            primaryPass += ", " + booking.getPassengerGender();
        }
        primaryPass += ")";
        pnrPassengerLabel.setText(primaryPass);

        pnrPassengersContainer.removeAll();
        List<Booking.BookingPassengerItem> bpList = booking.getBookingPassengers();
        if (bpList == null || bpList.isEmpty()) {
            JLabel noBpLabel = new JLabel("No individual passenger records found in BookingPassenger");
            noBpLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            noBpLabel.setForeground(Color.GRAY);
            pnrPassengersContainer.add(noBpLabel);
        } else {
            for (Booking.BookingPassengerItem bp : bpList) {
                JLabel itemLabel = new JLabel(String.format("• Passenger %d: %s  |  %s  |  Age: %d",
                        bp.getPassengerNumber(), bp.getPassengerName(), bp.getGender(), bp.getAge()));
                itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                itemLabel.setForeground(new Color(40, 40, 40));
                pnrPassengersContainer.add(itemLabel);
                pnrPassengersContainer.add(Box.createVerticalStrut(2));
            }
        }
        pnrPassengersContainer.revalidate();
        pnrPassengersContainer.repaint();

        // 4. Payment Details
        if (booking.getPaymentId() != null && booking.getPaymentId() > 0) {
            pnrPaymentNoticeLabel.setVisible(false);
            pnrPaymentStatusLabel.setText(booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "-");
            if ("SUCCESS".equalsIgnoreCase(booking.getPaymentStatus())) {
                pnrPaymentStatusLabel.setForeground(new Color(25, 140, 50));
            } else if ("REFUNDED".equalsIgnoreCase(booking.getPaymentStatus())) {
                pnrPaymentStatusLabel.setForeground(new Color(180, 100, 20));
            } else {
                pnrPaymentStatusLabel.setForeground(Color.DARK_GRAY);
            }
            pnrPaymentAmountLabel.setText(booking.getPaymentAmount() != null ? String.format("₹%.2f", booking.getPaymentAmount()) : "-");
            pnrPaymentMethodLabel.setText(booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "-");
            pnrPaymentTxnLabel.setText(booking.getTransactionReference() != null ? booking.getTransactionReference() : "-");
            String pDate = booking.getPaymentDate();
            pnrPaymentDateLabel.setText(pDate != null && !"null".equalsIgnoreCase(pDate) ? pDate : "-");
        } else {
            pnrPaymentNoticeLabel.setText("No payment record found for this booking.");
            pnrPaymentNoticeLabel.setVisible(true);
            pnrPaymentStatusLabel.setText("-");
            pnrPaymentStatusLabel.setForeground(Color.DARK_GRAY);
            pnrPaymentAmountLabel.setText("-");
            pnrPaymentMethodLabel.setText("-");
            pnrPaymentTxnLabel.setText("-");
            pnrPaymentDateLabel.setText("-");
        }

        // 5. Cancellation & Refund Details
        boolean isCancelled = "CANCELLED".equalsIgnoreCase(booking.getStatus());
        if (isCancelled) {
            pnrCancelStatusLabel.setText("Cancelled");
            pnrCancelStatusLabel.setForeground(new Color(190, 40, 40));

            String cDate = booking.getCancellationDate();
            pnrCancelDateLabel.setText(cDate != null && !"null".equalsIgnoreCase(cDate) ? cDate : "Not recorded");

            String cReason = booking.getCancellationReason();
            if (cReason != null && !cReason.isEmpty()) {
                pnrCancelReasonLabel.setText(cReason);
            } else {
                pnrCancelReasonLabel.setText("Booking marked CANCELLED (No detailed record in Cancellation table)");
            }

            if (booking.getRefundAmount() != null) {
                pnrRefundAmountLabel.setText(String.format("₹%.2f", booking.getRefundAmount()));
                pnrRefundAmountLabel.setForeground(new Color(180, 100, 20));
            } else {
                pnrRefundAmountLabel.setText("Pending / Not recorded");
                pnrRefundAmountLabel.setForeground(Color.DARK_GRAY);
            }
        } else {
            pnrCancelStatusLabel.setText("Not Cancelled (Active Ticket)");
            pnrCancelStatusLabel.setForeground(new Color(25, 140, 50));
            pnrCancelDateLabel.setText("N/A - Ticket is active");
            pnrCancelReasonLabel.setText("N/A - Ticket is active");
            pnrRefundAmountLabel.setText("N/A");
            pnrRefundAmountLabel.setForeground(Color.BLACK);
        }
    }

    private void cancelCurrentTicket() {
        if (currentPnrBooking == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel ticket for PNR " + currentPnrBooking.getPnr() + "?\n"
                        + "Passenger: " + currentPnrBooking.getPassengerName() + "\n"
                        + "Refund Amount: ₹" + String.format("%.2f", currentPnrBooking.getFare()) + "\n\n"
                        + "This action will update the booking status to CANCELLED and initiate refund in the database.",
                "Confirm Ticket Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = bookingDAO.cancelTicket(currentPnrBooking.getPnr());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Ticket cancelled successfully. Refund initiated.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    searchPnrStatus();
                    loadAllBookings();
                    loadMyBookings();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not cancel ticket. It may already be cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, "Cancellation error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadAllBookings() {
        allBookingsModel.setRowCount(0);
        List<Booking> list = bookingDAO.getAllBookings();
        for (Booking b : list) {
            allBookingsModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getPnr(),
                    b.getPassengerName(),
                    b.getPassengerPhone(),
                    b.getTrainName(),
                    b.getSourceStationName(),
                    b.getDestinationStationName(),
                    b.getJourneyDate(),
                    String.format("₹%.2f", b.getFare()),
                    b.getStatus()
            });
        }
    }
}
