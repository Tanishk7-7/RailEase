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

    private JLabel dbStatusLabel;
    private JTabbedPane tabbedPane;

    private JComboBox<Station> searchSourceCombo;
    private JComboBox<Station> searchDestCombo;
    private JTable trainTable;
    private DefaultTableModel trainTableModel;
    private JTable routeTable;
    private DefaultTableModel routeTableModel;
    private List<Train> currentTrainList;

    private JComboBox<Train> bookTrainCombo;
    private JComboBox<Station> bookSourceCombo;
    private JComboBox<Station> bookDestCombo;
    private JTextField bookDateField;
    private JLabel bookDistanceLabel;
    private JLabel bookFareLabel;
    private JLabel bookSeatsLabel;
    private JTextField passNameField;
    private JSpinner passAgeSpinner;
    private JComboBox<String> passGenderCombo;
    private JTextField passPhoneField;

    private JTextField pnrSearchField;
    private JPanel pnrDetailsCard;
    private JLabel pnrValueLabel;
    private JLabel pnrStatusLabel;
    private JLabel pnrPassengerLabel;
    private JLabel pnrTrainLabel;
    private JLabel pnrRouteLabel;
    private JLabel pnrDateLabel;
    private JLabel pnrFareLabel;
    private JButton cancelTicketButton;
    private Booking currentPnrBooking;

    private JTable allBookingsTable;
    private DefaultTableModel allBookingsModel;

    public MainGUI() {
        this.trainService = new TrainDAO();
        this.bookingDAO = new BookingDAO();

        setTitle("RailEase - Railway Management System");
        setSize(1000, 720);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        checkDatabaseAndLoadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("RailEase - Railway Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 0, 4));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);

        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        statusContainer.setOpaque(false);

        dbStatusLabel = new JLabel("Checking DB...");
        dbStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        dbStatusLabel.setForeground(Color.WHITE);

        JButton dbConfigBtn = new JButton("DB Settings");
        dbConfigBtn.setFocusPainted(false);
        dbConfigBtn.addActionListener(e -> showDatabaseConfigDialog());

        statusContainer.add(dbStatusLabel);
        statusContainer.add(dbConfigBtn);

        headerPanel.add(titleContainer, BorderLayout.WEST);
        headerPanel.add(statusContainer, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabbedPane.addTab("Search Trains", createTrainsTab());
        tabbedPane.addTab("Book Ticket", createBookingTab());
        tabbedPane.addTab("PNR Status", createPnrTab());
        tabbedPane.addTab("All Bookings", createAllBookingsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTrainsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search Trains Between Stations"));

        filterPanel.add(new JLabel("From:"));
        searchSourceCombo = new JComboBox<>();
        searchSourceCombo.setPreferredSize(new Dimension(180, 28));
        filterPanel.add(searchSourceCombo);

        filterPanel.add(new JLabel("To:"));
        searchDestCombo = new JComboBox<>();
        searchDestCombo.setPreferredSize(new Dimension(180, 28));
        filterPanel.add(searchDestCombo);

        JButton searchBtn = new JButton("Find Trains");
        searchBtn.setBackground(new Color(30, 120, 220));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> performTrainSearch());
        filterPanel.add(searchBtn);

        JButton resetBtn = new JButton("Show All Trains");
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> loadAllTrains());
        filterPanel.add(resetBtn);

        panel.add(filterPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);

        String[] trainCols = {"Train ID", "Train Number", "Train Name", "Total Seats", "Total Stops"};
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
        JButton bookSelectedBtn = new JButton("Book Selected Train");
        bookSelectedBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        bookSelectedBtn.setBackground(new Color(40, 150, 70));
        bookSelectedBtn.setForeground(Color.WHITE);
        bookSelectedBtn.setFocusPainted(false);
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        bookDateField = new JTextField(sdf.format(new Date()));

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
        JButton bookSubmitBtn = new JButton("Book Ticket");
        bookSubmitBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        bookSubmitBtn.setBackground(new Color(30, 140, 60));
        bookSubmitBtn.setForeground(Color.WHITE);
        bookSubmitBtn.setPreferredSize(new Dimension(220, 42));
        bookSubmitBtn.setFocusPainted(false);
        bookSubmitBtn.addActionListener(e -> executeBooking());
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
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        searchBar.setBorder(BorderFactory.createTitledBorder("Search Booking by PNR"));

        searchBar.add(new JLabel("Enter PNR Number:"));
        pnrSearchField = new JTextField(14);
        pnrSearchField.setFont(new Font("Monospaced", Font.BOLD, 14));
        searchBar.add(pnrSearchField);

        JButton findPnrBtn = new JButton("Check Status");
        findPnrBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        findPnrBtn.setBackground(new Color(24, 85, 160));
        findPnrBtn.setForeground(Color.WHITE);
        findPnrBtn.setFocusPainted(false);
        findPnrBtn.addActionListener(e -> searchPnrStatus());
        searchBar.add(findPnrBtn);

        panel.add(searchBar, BorderLayout.NORTH);

        pnrDetailsCard = new JPanel(new GridBagLayout());
        pnrDetailsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(null, "Ticket Details", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 13)),
                new EmptyBorder(15, 20, 15, 20)
        ));
        pnrDetailsCard.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        pnrValueLabel = new JLabel("-");
        pnrValueLabel.setFont(new Font("Monospaced", Font.BOLD, 16));

        pnrStatusLabel = new JLabel("-");
        pnrStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        pnrPassengerLabel = new JLabel("-");
        pnrTrainLabel = new JLabel("-");
        pnrRouteLabel = new JLabel("-");
        pnrDateLabel = new JLabel("-");
        pnrFareLabel = new JLabel("-");
        pnrFareLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        addCardRow(pnrDetailsCard, gbc, 0, "PNR Number:", pnrValueLabel);
        addCardRow(pnrDetailsCard, gbc, 1, "Booking Status:", pnrStatusLabel);
        addCardRow(pnrDetailsCard, gbc, 2, "Passenger:", pnrPassengerLabel);
        addCardRow(pnrDetailsCard, gbc, 3, "Train Details:", pnrTrainLabel);
        addCardRow(pnrDetailsCard, gbc, 4, "Journey Route:", pnrRouteLabel);
        addCardRow(pnrDetailsCard, gbc, 5, "Date of Journey:", pnrDateLabel);
        addCardRow(pnrDetailsCard, gbc, 6, "Total Fare:", pnrFareLabel);

        panel.add(pnrDetailsCard, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        cancelTicketButton = new JButton("Cancel Ticket");
        cancelTicketButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        cancelTicketButton.setBackground(new Color(200, 45, 45));
        cancelTicketButton.setForeground(Color.WHITE);
        cancelTicketButton.setFocusPainted(false);
        cancelTicketButton.setEnabled(false);
        cancelTicketButton.addActionListener(e -> cancelCurrentTicket());
        actionPanel.add(cancelTicketButton);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addCardRow(JPanel panel, GridBagConstraints gbc, int row, String title, JLabel value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        panel.add(value, gbc);
    }

    private JPanel createAllBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFocusPainted(false);
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

    private void checkDatabaseAndLoadData() {
        boolean connected = DBConnection.testConnection();
        if (connected) {
            dbStatusLabel.setText("Connected: " + DBConnection.getDatabaseName());
            loadAllStations();
            loadAllTrains();
            loadAllBookings();
        } else {
            dbStatusLabel.setText("Disconnected");
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
                dbStatusLabel.setText("Connected: " + DBConnection.getDatabaseName());
                JOptionPane.showMessageDialog(this, "Connected to database successfully.", "Connection Successful", JOptionPane.INFORMATION_MESSAGE);
                loadAllStations();
                loadAllTrains();
                loadAllBookings();
            } else {
                dbStatusLabel.setText("Connection Failed");
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
            int stopCount = (t.getRoutes() != null) ? t.getRoutes().size() : 0;
            trainTableModel.addRow(new Object[]{
                    t.getTrainId(),
                    t.getTrainNumber(),
                    t.getTrainName(),
                    t.getTotalSeats(),
                    stopCount
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

        List<Train> results = trainService.searchTrains(src.getStationId(), dst.getStationId());
        currentTrainList = results;
        displayTrainsInTable(results);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trains found operating between " + src.getStationName() + " and " + dst.getStationName() + " in that direction.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
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
        tabbedPane.setSelectedIndex(1);

        for (int i = 0; i < bookTrainCombo.getItemCount(); i++) {
            Train t = bookTrainCombo.getItemAt(i);
            if (t.getTrainId() == selectedTrain.getTrainId()) {
                bookTrainCombo.setSelectedIndex(i);
                break;
            }
        }
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

        updateFareCalculation();
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

            double fare = Math.max(50.0, 50.0 + (distance * 1.25));

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

    private void executeBooking() {
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
        double fare = Math.max(50.0, 50.0 + (distance * 1.25));

        try {
            Passenger passenger = new Passenger(passName, age, gender, phone);
            Booking booking = bookingDAO.bookTicket(passenger, train.getTrainId(), src.getStationId(), dst.getStationId(), dateStr, fare);

            String message = "Ticket Booked Successfully!\n\n"
                    + "PNR Number      : " + booking.getPnr() + "\n"
                    + "Passenger Name  : " + passenger.getPassengerName() + " (" + gender + ", Age: " + age + ")\n"
                    + "Phone Number    : " + passenger.getPhone() + "\n"
                    + "Train           : " + train.getTrainName() + " (" + train.getTrainNumber() + ")\n"
                    + "Journey Route   : " + src.getStationName() + " -> " + dst.getStationName() + "\n"
                    + "Journey Date    : " + dateStr + "\n"
                    + "Distance        : " + String.format("%.1f km", distance) + "\n"
                    + "Total Fare Paid : ₹" + String.format("%.2f", fare) + "\n"
                    + "Status          : " + booking.getStatus() + "\n\n"
                    + "You can check status or cancel anytime using your PNR in the PNR tab.";

            JOptionPane.showMessageDialog(this, message, "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

            passNameField.setText("");
            passPhoneField.setText("");
            updateFareCalculation();
            loadAllBookings();

            pnrSearchField.setText(booking.getPnr());

        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(this, ise.getMessage(), "Booking Notice", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Booking Failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
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
            pnrStatusLabel.setText("-");
            pnrPassengerLabel.setText("-");
            pnrTrainLabel.setText("-");
            pnrRouteLabel.setText("-");
            pnrDateLabel.setText("-");
            pnrFareLabel.setText("-");
            cancelTicketButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "No booking found matching PNR: " + pnr, "PNR Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        pnrValueLabel.setText(booking.getPnr());
        pnrStatusLabel.setText(booking.getStatus());
        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            pnrStatusLabel.setForeground(new Color(25, 140, 50));
            cancelTicketButton.setEnabled(true);
        } else {
            pnrStatusLabel.setForeground(new Color(190, 40, 40));
            cancelTicketButton.setEnabled(false);
        }

        pnrPassengerLabel.setText(booking.getPassengerName() + " (" + booking.getPassengerPhone() + ")");
        pnrTrainLabel.setText(booking.getTrainName() + " (#" + booking.getTrainNumber() + ")");
        pnrRouteLabel.setText(booking.getSourceStationName() + " -> " + booking.getDestinationStationName());
        pnrDateLabel.setText(booking.getJourneyDate());
        pnrFareLabel.setText(String.format("₹%.2f", booking.getFare()));
    }

    private void cancelCurrentTicket() {
        if (currentPnrBooking == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel ticket for PNR " + currentPnrBooking.getPnr() + "?\n"
                        + "Passenger: " + currentPnrBooking.getPassengerName() + "\n"
                        + "Refund Amount: ₹" + String.format("%.2f", currentPnrBooking.getFare()) + "\n\n"
                        + "This action will update the booking status to CANCELLED in the database.",
                "Confirm Ticket Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = bookingDAO.cancelTicket(currentPnrBooking.getPnr());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Ticket cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    searchPnrStatus();
                    loadAllBookings();
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
