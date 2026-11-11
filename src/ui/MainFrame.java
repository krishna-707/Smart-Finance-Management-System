
package ui;

import dao.FinanceDAO;
import utils.Session;
import utils.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class MainFrame extends JFrame {

    private final FinanceDAO dao = new FinanceDAO();
    private final JLabel dashboard = new JLabel("Loading...");
    private final JTabbedPane tabs = new JTabbedPane();

    public MainFrame() {
        setTitle("FINORA | Smart Finance Management");
        setSize(1120, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        getContentPane().setBackground(UIConstants.BACKGROUND);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIConstants.BACKGROUND);

        JLabel welcome = new JLabel(
            "  FINORA  |  Welcome, " + Session.getFullName() + "   "
        );
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcome.setForeground(UIConstants.DARK_GREEN);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            Session.end();
            dispose();
            new LoginFrame().setVisible(true);
        });

        top.add(welcome, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        dashboard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        dashboard.setFont(UIConstants.BOLD);

        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(UIConstants.BACKGROUND);
        north.add(top, BorderLayout.NORTH);
        north.add(dashboard, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        addTab("Dashboard", this::dashboardPanel);
        addTab("Accounts", this::accountsPanel);
        addTab("Transactions", this::transactionsPanel);
        addTab("Budgets", this::budgetsPanel);
        addTab("Savings Jar", this::goalsPanel);
        addTab("Subscriptions", this::subscriptionsPanel);
        addTab("Buy Later", this::wishlistPanel);
        addTab("Salary", this::salaryPanel);
        addTab("Money-Time", this::moneyTimePanel);
        addTab("Reports", this::reportsPanel);
        addTab("Health Plant", this::healthPanel);

        refreshDashboard();
    }

    private interface PanelMaker {
        JPanel make();
    }

    private void addTab(String name, PanelMaker maker) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BACKGROUND);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> {
            int i = tabs.getSelectedIndex();
            tabs.setComponentAt(i, makeTab(maker));
            refreshDashboard();
        });

        wrapper.add(maker.make(), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refresh);
        wrapper.add(bottom, BorderLayout.SOUTH);

        tabs.addTab(name, wrapper);
    }

    private JPanel makeTab(PanelMaker maker) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BACKGROUND);
        p.add(maker.make(), BorderLayout.CENTER);
        return p;
    }

    private JPanel base() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        p.setBackground(UIConstants.BACKGROUND);
        return p;
    }

    private JPanel actionPanel(JTable table, JButton... buttons) {
        JPanel p = base();
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT));
        b.setBackground(UIConstants.BACKGROUND);

        for (JButton x : buttons) {
            b.add(x);
        }

        p.add(b, BorderLayout.SOUTH);
        return p;
    }

    private JTable table(String... columns) {
        return new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
    }

    private void fill(JTable t, List<String[]> rows) {
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        m.setRowCount(0);

        for (String[] r : rows) {
            m.addRow(r);
        }
    }

    private interface Task {
        void run() throws Exception;
    }

    private void safe(Task r) {
        try {
            r.run();
            refreshDashboard();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "FINORA",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String ask(String label) {
        return JOptionPane.showInputDialog(this, label);
    }

    private BigDecimal money(String s) {
        return new BigDecimal(s.trim());
    }

    private int selectedId(JTable t) {
        int row = t.getSelectedRow();

        if (row < 0) {
            throw new IllegalArgumentException("Select a row first.");
        }

        row = t.convertRowIndexToModel(row);
        return Integer.parseInt(t.getModel().getValueAt(row, 0).toString());
    }

    // DASHBOARD

    private JPanel dashboardPanel() {
        JPanel p = base();

        JTextArea info = new JTextArea(
            "Your finance overview is shown above. Use the tabs to manage " +
            "your accounts, transactions, budgets and goals."
        );

        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setFont(UIConstants.NORMAL);

        p.add(info, BorderLayout.NORTH);

        JButton refresh = new JButton("Refresh overview");
        refresh.addActionListener(e -> refreshDashboard());

        p.add(refresh, BorderLayout.SOUTH);
        return p;
    }

    // ACCOUNTS

    private JPanel accountsPanel() {
        JTable t = table("ID", "Account", "Type", "Balance");
        safe(() -> fill(t, dao.accounts()));

        JButton add = new JButton("Add account");

        add.addActionListener(e -> safe(() -> {
            String n = ask("Account name:");

            if (n == null || n.isBlank()) {
                return;
            }

            String type = ask("Account type (Cash/Bank/Savings):");

            if (type == null || type.isBlank()) {
                return;
            }

            dao.addAccount(n, type);
            fill(t, dao.accounts());
        }));

        return actionPanel(t, add);
    }

    // TRANSACTIONS

    private JPanel transactionsPanel() {
        JTable t = table(
            "ID", "Account", "Category", "Type",
            "Amount", "Description", "Date"
        );

        safe(() -> fill(t, dao.transactions()));

        JButton add = new JButton("Add transaction");

        add.addActionListener(e -> safe(() -> {
            List<String[]> ac = dao.accounts();

            if (ac.isEmpty()) {
                throw new Exception("Create an account first.");
            }

            String[] accountOptions = ac.stream()
                .map(a -> a[0] + " - " + a[1])
                .toArray(String[]::new);

            String account = (String) JOptionPane.showInputDialog(
                this, "Account", "Transaction",
                JOptionPane.PLAIN_MESSAGE, null,
                accountOptions, accountOptions[0]
            );

            if (account == null) return;

            String type = (String) JOptionPane.showInputDialog(
                this, "Type", "Transaction",
                JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"EXPENSE", "INCOME"}, "EXPENSE"
            );

            if (type == null) return;

            List<String[]> cats = dao.categories(type);

            String[] co = cats.stream()
                .map(a -> a[0] + " - " + a[1])
                .toArray(String[]::new);

            if (co.length == 0) {
                throw new Exception("No category exists for this type.");
            }

            String cat = (String) JOptionPane.showInputDialog(
                this, "Category", "Transaction",
                JOptionPane.PLAIN_MESSAGE, null, co, co[0]
            );

            if (cat == null) return;

            String amt = ask("Amount:");
            if (amt == null) return;

            String desc = ask("Description:");
            if (desc == null) desc = "";

            dao.addTransaction(
                Integer.parseInt(account.split(" - ")[0]),
                Integer.parseInt(cat.split(" - ")[0]),
                money(amt),
                type,
                desc,
                LocalDate.now()
            );

            fill(t, dao.transactions());
        }));

        return actionPanel(t, add);
    }

    // BUDGETS

    private JPanel budgetsPanel() {
        JTable t = table(
            "ID", "Category", "Month", "Year", "Limit", "Spent"
        );

        safe(() -> fill(t, dao.budgets()));

        JButton add = new JButton("Set monthly budget");

        add.addActionListener(e -> safe(() -> {
            List<String[]> cats = dao.categories("EXPENSE");

            String[] opts = cats.stream()
                .map(a -> a[0] + " - " + a[1])
                .toArray(String[]::new);

            if (opts.length == 0) {
                throw new Exception("No expense categories found.");
            }

            String cat = (String) JOptionPane.showInputDialog(
                this, "Category", "Budget",
                JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]
            );

            if (cat == null) return;

            String amount = ask("Monthly limit:");
            if (amount == null) return;

            YearMonth ym = YearMonth.now();

            dao.addBudget(
                Integer.parseInt(cat.split(" - ")[0]),
                ym.getMonthValue(),
                ym.getYear(),
                money(amount)
            );

            fill(t, dao.budgets());
        }));

        return actionPanel(t, add);
    }

    // SAVINGS JAR

    private JPanel goalsPanel() {
        JTable t = table(
            "ID", "Goal", "Target", "Saved", "Target date"
        );

        safe(() -> fill(t, dao.goals()));

        JButton add = new JButton("Add savings goal");
        JButton edit = new JButton("Edit goal");
        JButton contribute = new JButton("Add money");
        JButton delete = new JButton("Delete goal");

        add.addActionListener(e -> safe(() -> {
            String n = ask("Goal name:");
            if (n == null) return;

            if (n.isBlank()) {
                throw new IllegalArgumentException(
                    "Goal name cannot be empty."
                );
            }

            String target = ask("Target amount:");
            if (target == null) return;

            String saved = ask(
                "Amount already saved (enter 0 if none):"
            );

            if (saved == null) return;

            String d = ask(
                "Target date (YYYY-MM-DD), or leave blank:"
            );

            BigDecimal targetAmount = money(target);
            BigDecimal savedAmount = money(saved);

            if (targetAmount.signum() <= 0 ||
                savedAmount.signum() < 0) {
                throw new IllegalArgumentException(
                    "Target must be positive and saved amount cannot be negative."
                );
            }

            dao.addGoal(
                n.trim(),
                targetAmount,
                savedAmount,
                d == null || d.isBlank()
                    ? null : LocalDate.parse(d.trim())
            );

            fill(t, dao.goals());
        }));

        edit.addActionListener(e -> safe(() -> {
            int row = t.getSelectedRow();

            if (row < 0) {
                throw new IllegalArgumentException(
                    "Select a savings goal to edit."
                );
            }

            row = t.convertRowIndexToModel(row);

            int id = Integer.parseInt(
                t.getModel().getValueAt(row, 0).toString()
            );

            String oldName = t.getModel().getValueAt(row, 1).toString();
            String oldTarget = t.getModel().getValueAt(row, 2).toString();

            Object dateValue = t.getModel().getValueAt(row, 4);

            String oldDate = dateValue == null ||
                dateValue.toString().equals("null")
                ? "" : dateValue.toString();

            String name = JOptionPane.showInputDialog(
                this, "Goal name:", oldName
            );

            if (name == null) return;

            String target = JOptionPane.showInputDialog(
                this, "Target amount:", oldTarget
            );

            if (target == null) return;

            String date = JOptionPane.showInputDialog(
                this,
                "Target date (YYYY-MM-DD), or leave blank:",
                oldDate
            );

            if (date == null) return;

            if (name.isBlank()) {
                throw new IllegalArgumentException(
                    "Goal name cannot be empty."
                );
            }

            BigDecimal targetAmount = money(target);

            if (targetAmount.signum() <= 0) {
                throw new IllegalArgumentException(
                    "Target amount must be greater than zero."
                );
            }

            dao.updateGoal(
                id,
                name.trim(),
                targetAmount,
                date.isBlank() ? null : LocalDate.parse(date.trim())
            );

            fill(t, dao.goals());
        }));

        contribute.addActionListener(e -> safe(() -> {
            int id = selectedId(t);

            String amount = ask("Amount to add to this goal:");
            if (amount == null) return;

            BigDecimal value = money(amount);

            if (value.signum() <= 0) {
                throw new IllegalArgumentException(
                    "Contribution must be greater than zero."
                );
            }

            dao.addGoalMoney(id, value);
            fill(t, dao.goals());
        }));

        delete.addActionListener(e -> safe(() -> {
            int id = selectedId(t);

            int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected savings goal? This cannot be undone.",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) return;

            dao.deleteGoal(id);
            fill(t, dao.goals());
        }));

        return actionPanel(t, add, edit, contribute, delete);
    }

    // SUBSCRIPTIONS

    private JPanel subscriptionsPanel() {
        JTable t = table(
            "ID", "Name", "Amount", "Cycle", "Renewal", "Active"
        );

        safe(() -> fill(t, dao.subscriptions()));

        JButton add = new JButton("Add subscription");

        add.addActionListener(e -> safe(() -> {
            String n = ask("Subscription name:");
            if (n == null) return;

            String a = ask("Amount:");
            if (a == null) return;

            String c = ask("Billing cycle (Monthly/Yearly):");
            if (c == null) return;

            String d = ask("Next renewal date (YYYY-MM-DD):");
            if (d == null) return;

            dao.addSubscription(
                n, money(a), c, LocalDate.parse(d)
            );

            fill(t, dao.subscriptions());
        }));

        return actionPanel(t, add);
    }

    // BUY LATER

    private JPanel wishlistPanel() {
        JTable t = table(
            "ID", "Item", "Price", "Added",
            "Decision available", "Status"
        );

        safe(() -> fill(t, dao.wishlist()));

        JButton add = new JButton("Add to Buy Later");
        JButton purchased = new JButton("Mark purchased");
        JButton skip = new JButton("Skip item");

        add.addActionListener(e -> safe(() -> {
            String n = ask("Item name:");
            if (n == null) return;

            String a = ask("Price:");
            if (a == null) return;

            dao.addWishlist(n, money(a));
            fill(t, dao.wishlist());
        }));

        purchased.addActionListener(e -> safe(() -> {
            dao.updateWishlist(selectedId(t), "PURCHASED");
            fill(t, dao.wishlist());
        }));

        skip.addActionListener(e -> safe(() -> {
            dao.updateWishlist(selectedId(t), "SKIPPED");
            fill(t, dao.wishlist());
        }));

        return actionPanel(t, add, purchased, skip);
    }

    // SALARY

    private JPanel salaryPanel() {
        JTable t = table(
            "ID", "Salary source", "Amount",
            "Expected date", "Confirmed"
        );

        safe(() -> fill(t, dao.salaries()));

        JButton add = new JButton("Add salary schedule");
        JButton confirm = new JButton("Confirm received");

        add.addActionListener(e -> safe(() -> {
            String n = ask("Salary source/name:");
            if (n == null) return;

            String a = ask("Expected amount:");
            if (a == null) return;

            String d = ask("Expected date (YYYY-MM-DD):");
            if (d == null) return;

            dao.addSalary(n, money(a), LocalDate.parse(d));
            fill(t, dao.salaries());
        }));

        confirm.addActionListener(e -> safe(() -> {
            dao.confirmSalary(selectedId(t));
            fill(t, dao.salaries());
        }));

        return actionPanel(t, add, confirm);
    }

    // MONEY-TIME CALCULATOR

    private JPanel moneyTimePanel() {
        JPanel p = base();

        JTextField price = new JTextField(12);
        JTextField income = new JTextField(12);
        JTextField hours = new JTextField("8", 12);

        JLabel result = new JLabel(
            "Enter values to calculate work time."
        );

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBackground(UIConstants.BACKGROUND);

        form.add(new JLabel("Item price (₹)"));
        form.add(price);

        form.add(new JLabel("Monthly income (₹)"));
        form.add(income);

        form.add(new JLabel("Working hours per day"));
        form.add(hours);

        JButton calc = new JButton("Calculate");

        calc.addActionListener(e -> {
            try {
                double itemPrice = Double.parseDouble(price.getText());
                double monthlyIncome = Double.parseDouble(income.getText());
                double dailyHours = Double.parseDouble(hours.getText());

                if (itemPrice <= 0 ||
                    monthlyIncome <= 0 ||
                    dailyHours <= 0) {
                    throw new IllegalArgumentException();
                }

                double days = itemPrice / (monthlyIncome / 30.0);
                double h = days * dailyHours;

                result.setText(String.format(
                    "Estimated work time: %.2f hours (%.2f working days)",
                    h, h / dailyHours
                ));

            } catch (Exception ex) {
                result.setText(
                    "Please enter valid positive numbers."
                );
            }
        });

        p.add(form, BorderLayout.NORTH);
        p.add(calc, BorderLayout.CENTER);
        p.add(result, BorderLayout.SOUTH);

        return p;
    }

    // REPORTS

    private JPanel reportsPanel() {
        JPanel p = base();

        JTextField month = new JTextField(
            "" + LocalDate.now().getMonthValue(), 5
        );

        JTextField year = new JTextField(
            "" + LocalDate.now().getYear(), 7
        );

        JTextArea out = new JTextArea();
        out.setEditable(false);

        JButton run = new JButton("Generate monthly report");

        run.addActionListener(e -> safe(() -> {
            int m = Integer.parseInt(month.getText());
            int y = Integer.parseInt(year.getText());

            if (m < 1 || m > 12 || y < 1) {
                throw new IllegalArgumentException(
                    "Enter a valid month and year."
                );
            }

            List<String[]> rows = dao.report(m, y);

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;

            for (String[] r : rows) {
                if (r[0].equals("INCOME")) {
                    income = new BigDecimal(r[1]);
                } else {
                    expense = new BigDecimal(r[1]);
                }
            }

            out.setText(
                "FINORA MONTHLY REPORT\n" +
                "Month: " + month.getText() + " / " + year.getText() +
                "\n\nIncome: ₹" + income +
                "\nExpenses: ₹" + expense +
                "\nNet savings: ₹" + income.subtract(expense)
            );
        }));

        JPanel f = new JPanel();
        f.setBackground(UIConstants.BACKGROUND);

        f.add(new JLabel("Month (1-12)"));
        f.add(month);
        f.add(new JLabel("Year"));
        f.add(year);
        f.add(run);

        p.add(f, BorderLayout.NORTH);
        p.add(new JScrollPane(out), BorderLayout.CENTER);

        return p;
    }

    // HEALTH PLANT

    private JPanel healthPanel() {
        JPanel p = base();

        JLabel title = new JLabel("FINORA - Financial Health Plant");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.DARK_GREEN);

        JLabel scoreLabel = new JLabel("Loading financial health...");
        scoreLabel.setFont(UIConstants.BOLD);

        PlantDrawing plant = new PlantDrawing();

        try {
            List<String[]> data = dao.health();

            if (!data.isEmpty()) {
                String stage = data.get(0)[1];

                scoreLabel.setText(
                    "Stage: " + stage +
                    "     |     Score: " + data.get(0)[0] +
                    "     |     Points: " + data.get(0)[2]
                );

                plant.setStage(stage);
            } else {
                scoreLabel.setText(
                    "No financial health data available."
                );
            }

        } catch (Exception ex) {
            scoreLabel.setText(
                "Could not load health data: " + ex.getMessage()
            );
        }

        JPanel top = new JPanel(new BorderLayout(5, 10));
        top.setBackground(UIConstants.BACKGROUND);
        top.add(title, BorderLayout.NORTH);
        top.add(scoreLabel, BorderLayout.SOUTH);

        p.add(top, BorderLayout.NORTH);
        p.add(plant, BorderLayout.CENTER);

        return p;
    }

    // GRAPHICAL PLANT

    private class PlantDrawing extends JPanel {

        private String stage = "Seed";

        public PlantDrawing() {
            setBackground(UIConstants.BACKGROUND);
            setPreferredSize(new Dimension(400, 350));
        }

        public void setStage(String stage) {
            this.stage = stage;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            int center = getWidth() / 2;
            int ground = getHeight() - 75;

            // Ground
            g2.setColor(new Color(120, 160, 100));
            g2.fillOval(center - 110, ground, 220, 20);

            // Stem
            g2.setColor(new Color(40, 130, 65));
            g2.setStroke(new BasicStroke(8));
            g2.drawLine(center, ground, center, ground - 140);

            // Left leaves
            g2.setColor(new Color(55, 160, 75));
            g2.fillOval(center - 85, ground - 125, 85, 38);
            g2.fillOval(center - 100, ground - 175, 90, 40);

            // Right leaves
            g2.setColor(new Color(75, 180, 85));
            g2.fillOval(center, ground - 105, 85, 38);
            g2.fillOval(center + 10, ground - 160, 90, 40);

            // Top leaf
            g2.setColor(new Color(40, 145, 65));
            g2.fillOval(center - 30, ground - 205, 60, 65);

            // Pot
            g2.setColor(new Color(170, 95, 55));

            int[] x = {
                center - 65,
                center + 65,
                center + 48,
                center - 48
            };

            int[] y = {
                ground + 10,
                ground + 10,
                ground + 75,
                ground + 75
            };

            g2.fillPolygon(x, y, 4);

            g2.setColor(new Color(120, 65, 40));
            g2.fillRoundRect(
                center - 72, ground, 144, 18, 8, 8
            );

            // Plant stage
            g2.setColor(UIConstants.DARK_GREEN);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));

            g2.drawString(
                "Plant Stage: " + stage,
                30,
                35
            );

            g2.dispose();
        }
    }

    // DASHBOARD REFRESH

    private void refreshDashboard() {
        try {
            dashboard.setText(dao.dashboard());
        } catch (Exception ex) {
            dashboard.setText(
                "Could not load overview: " + ex.getMessage()
            );
        }
    }
}
