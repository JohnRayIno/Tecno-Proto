import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class SmartStorageUI extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(SmartStorageUI.class.getName());

    private static final int WINDOW_WIDTH = 920;
    private static final int WINDOW_HEIGHT = 580;
    private static final int PADDING = 16;
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Color BACKGROUND_COLOR = new Color(18, 28, 44);
    private static final Color CARD_COLOR = new Color(26, 38, 62);
    private static final Color ACCENT_COLOR = new Color(72, 156, 255);
    private static final Color TEXT_COLOR = new Color(238, 242, 247);
    private static final Color SECONDARY_TEXT = new Color(169, 184, 204);
    private static final Color SUCCESS_COLOR = new Color(72, 209, 133);
    private static final int MONITOR_INTERVAL_MS = 5000;
    private static final int HISTORY_MAX_SIZE = 20;
    private static final double DAILY_GROWTH_RATE = 1.5;
    private static final double CRITICAL_THRESHOLD = 90.0;
    private static final double WARNING_THRESHOLD = 75.0;

    private JLabel storageLabel;
    private JLabel predictionLabel;
    private JLabel suggestionLabel;
    private JLabel folderLabel;
    private JLabel syncStatusLabel;
    private JProgressBar storageBar;
    private JTextArea logArea;
    private Timer timer;
    private GraphPanel graphPanel;

    private ArrayList<Double> history = new ArrayList<>();
    private File selectedFolder;
    private File syncFolder;

    public SmartStorageUI() {
        initLookAndFeel();
        setTitle("Smart Storage Management System");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        startMonitoring();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopMonitoring();
            }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(PADDING, PADDING));
        root.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        root.setBackground(BACKGROUND_COLOR);

        JPanel statusPanel = createCardPanel(new GridLayout(6, 1, 10, 10));
        storageLabel = new JLabel("Storage Usage: --%", SwingConstants.LEFT);
        storageLabel.setFont(HEADER_FONT);
        storageLabel.setForeground(TEXT_COLOR);

        storageBar = new JProgressBar(0, 100);
        storageBar.setStringPainted(true);
        storageBar.setPreferredSize(new Dimension(0, 26));
        storageBar.setForeground(ACCENT_COLOR);
        storageBar.setBackground(new Color(35, 48, 71));

        predictionLabel = new JLabel("Prediction: Calculating...", SwingConstants.LEFT);
        predictionLabel.setFont(LABEL_FONT);
        predictionLabel.setForeground(SECONDARY_TEXT);

        suggestionLabel = new JLabel("Suggestion: -", SwingConstants.LEFT);
        suggestionLabel.setFont(LABEL_FONT);
        suggestionLabel.setForeground(SECONDARY_TEXT);

        folderLabel = new JLabel("Selected Folder: Not set", SwingConstants.LEFT);
        folderLabel.setFont(LABEL_FONT);
        folderLabel.setForeground(SECONDARY_TEXT);

        syncStatusLabel = new JLabel("Sync Status: Disabled", SwingConstants.LEFT);
        syncStatusLabel.setFont(LABEL_FONT);
        syncStatusLabel.setForeground(SECONDARY_TEXT);

        statusPanel.add(storageLabel);
        statusPanel.add(storageBar);
        statusPanel.add(predictionLabel);
        statusPanel.add(suggestionLabel);
        statusPanel.add(folderLabel);
        statusPanel.add(syncStatusLabel);

        graphPanel = new GraphPanel(history);
        graphPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                BorderFactory.createLineBorder(new Color(56, 84, 126), 1)));

        logArea = new JTextArea(12, 30);
        logArea.setEditable(false);
        logArea.setFont(SMALL_FONT);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(22, 34, 52));
        logArea.setForeground(TEXT_COLOR);
        logArea.setOpaque(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setOpaque(false);
        logScroll.getViewport().setBackground(new Color(22, 34, 52));
        logScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel logCard = createCardPanel(new BorderLayout());
        logCard.setPreferredSize(new Dimension(280, 0));
        JLabel logTitle = new JLabel("Insights & Actions");
        logTitle.setFont(LABEL_FONT);
        logTitle.setForeground(TEXT_COLOR);
        logTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        logCard.add(logTitle, BorderLayout.NORTH);
        logCard.add(logScroll, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(PADDING, PADDING));
        centerPanel.setOpaque(false);
        centerPanel.add(graphPanel, BorderLayout.CENTER);
        centerPanel.add(logCard, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 12, 12));
        buttonPanel.setOpaque(false);

        JButton chooseFolderBtn = createButton("Choose Folder");
        chooseFolderBtn.addActionListener(e -> chooseFolder());
        JButton chooseSyncBtn = createButton("Choose Sync Folder");
        chooseSyncBtn.addActionListener(e -> chooseSyncFolder());
        JButton scanBtn = createButton("Scan Selected Folder");
        scanBtn.addActionListener(e -> performScan());
        JButton organizeBtn = createButton("Organize Files");
        organizeBtn.addActionListener(e -> organizeFiles());
        JButton syncBtn = createButton("Sync Now");
        syncBtn.addActionListener(e -> syncNow());
        JButton helpBtn = createButton("Help / Tutorial");
        helpBtn.addActionListener(e -> showHelp());
        JButton testBtn = createButton("Open Test Window");
        testBtn.addActionListener(e -> new TestWindow());
        JButton refreshBtn = createButton("Refresh Storage");
        refreshBtn.addActionListener(e -> updateStorage());

        buttonPanel.add(chooseFolderBtn);
        buttonPanel.add(chooseSyncBtn);
        buttonPanel.add(scanBtn);
        buttonPanel.add(organizeBtn);
        buttonPanel.add(syncBtn);
        buttonPanel.add(helpBtn);
        buttonPanel.add(testBtn);
        buttonPanel.add(refreshBtn);

        JPanel topPanel = createCardPanel(new BorderLayout(PADDING, PADDING));
        topPanel.add(statusPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);

        add(root);
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
            UIManager.put("control", BACKGROUND_COLOR);
            UIManager.put("info", CARD_COLOR);
            UIManager.put("nimbusBase", new Color(15, 26, 44));
            UIManager.put("nimbusBlueGrey", new Color(50, 68, 97));
            UIManager.put("text", TEXT_COLOR);
            UIManager.put("Button.background", ACCENT_COLOR);
            UIManager.put("Button.foreground", TEXT_COLOR);
            UIManager.put("TabbedPane.contentAreaColor", BACKGROUND_COLOR);
            UIManager.put("FileChooser.background", BACKGROUND_COLOR);
            UIManager.put("FileChooser.foreground", TEXT_COLOR);
            UIManager.put("FileChooser.listViewBackground", CARD_COLOR);
            UIManager.put("FileChooser.listViewForeground", TEXT_COLOR);
            UIManager.put("FileChooser.selectionBackground", ACCENT_COLOR);
            UIManager.put("FileChooser.selectionForeground", TEXT_COLOR);
            UIManager.put("FileChooser.textFieldBackground", new Color(35, 48, 71));
            UIManager.put("FileChooser.textFieldForeground", TEXT_COLOR);
        } catch (UnsupportedLookAndFeelException ignored) {
        }
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACCENT_COLOR);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createCardPanel(LayoutManager layout) {
        JPanel card = new RoundedPanel(18, CARD_COLOR);
        card.setLayout(layout);
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return card;
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void chooseFolder() {
        JFileChooser chooser = createStyledFileChooser();
        chooser.setDialogTitle("Select a folder to manage");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        int returnValue = chooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedFolder = chooser.getSelectedFile();
            folderLabel.setText("Selected Folder: " + selectedFolder.getAbsolutePath());
            appendLog("Selected folder for organization and scan: " + selectedFolder.getAbsolutePath());
            updateStorage();
        }
    }

    private void chooseSyncFolder() {
        JFileChooser chooser = createStyledFileChooser();
        chooser.setDialogTitle("Select a local sync target folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        int returnValue = chooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            syncFolder = chooser.getSelectedFile();
            updateSyncStatus();
            appendLog("Sync target configured: " + syncFolder.getAbsolutePath());
        }
    }

    private JFileChooser createStyledFileChooser() {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chooser.setBackground(BACKGROUND_COLOR);
        chooser.setForeground(TEXT_COLOR);
        chooser.setOpaque(true);
        chooser.putClientProperty("FileChooser.useShellFolder", Boolean.TRUE);
        return chooser;
    }

    private void performScan() {
        if (selectedFolder == null) {
            appendLog("No folder selected. Please choose a folder before scanning.");
            return;
        }

        appendLog("Scanning folder: " + selectedFolder.getAbsolutePath());
        long folderSize = StorageManager.calculateFolderSize(selectedFolder);
        appendLog("Folder size: " + formatBytes(folderSize));

        List<File> largestFiles = StorageManager.listLargestFiles(selectedFolder, 5);
        if (largestFiles.isEmpty()) {
            appendLog("No files found in selected folder.");
        } else {
            appendLog("Largest files:");
            for (File file : largestFiles) {
                appendLog("  " + formatBytes(file.length()) + " — " + file.getAbsolutePath());
            }
        }

        updateStorage();
        if (folderSize > 0 && folderSize > selectedFolder.getTotalSpace() * 0.7) {
            appendLog("Notice: This folder consumes more than 70% of the drive capacity. Consider organizing or cleaning it.");
        }
    }

    private void organizeFiles() {
        if (selectedFolder == null) {
            appendLog("Please select a folder before attempting to organize files.");
            return;
        }

        File destinationFolder = new File(selectedFolder, "OrganizedByType");
        int confirmed = JOptionPane.showConfirmDialog(this,
                "This will organize files in the selected folder into a new subfolder called 'OrganizedByType'.\nDo you want to continue?",
                "Organize Files",
                JOptionPane.YES_NO_OPTION);

        if (confirmed != JOptionPane.YES_OPTION) {
            appendLog("File organization cancelled by user.");
            return;
        }

        appendLog("Organizing files by type in: " + selectedFolder.getAbsolutePath());
        int movedCount = StorageManager.organizeFilesByType(selectedFolder, destinationFolder);
        appendLog("Organized " + movedCount + " files into: " + destinationFolder.getAbsolutePath());
        updateStorage();
    }

    private void syncNow() {
        if (selectedFolder == null || syncFolder == null) {
            appendLog("Sync requires both a selected source folder and a sync target folder.");
            return;
        }

        appendLog("Syncing from " + selectedFolder.getAbsolutePath() + " to " + syncFolder.getAbsolutePath());
        int copied = StorageManager.syncFolders(selectedFolder, syncFolder);
        appendLog("Synchronized " + copied + " files.");
        updateSyncStatus();
    }

    private void showHelp() {
        String message = "<html><body width='420'><h2>Smart Storage Management</h2>"
                + "<ul>"
                + "<li><b>Choose Folder</b>: Select a folder to scan and organize.</li>"
                + "<li><b>Choose Sync Folder</b>: Configure a local sync destination.</li>"
                + "<li><b>Scan Selected Folder</b>: Lists largest files and folder usage.</li>"
                + "<li><b>Organize Files</b>: Categorizes files by type into an 'OrganizedByType' folder.</li>"
                + "<li><b>Sync Now</b>: Copies files from source to sync target to simulate cross-device sync.</li>"
                + "<li>The app also monitors storage usage, predicts when the drive will fill, and suggests cleanup actions.</li>"
                + "</ul></body></html>";
        JOptionPane.showMessageDialog(this, message, "Tutorial & Onboarding", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startMonitoring() {
        timer = new Timer(MONITOR_INTERVAL_MS, e -> updateStorage());
        timer.start();
        updateStorage();
    }

    private void stopMonitoring() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            LOGGER.info("Monitoring stopped");
        }
    }

    private void updateStorage() {
        try {
            File root;
            if (selectedFolder != null && selectedFolder.exists()) {
                root = selectedFolder;
            } else {
                root = new File("C:");
            }

            if (!root.exists()) {
                LOGGER.warning("Selected path not accessible");
                appendLog("Selected path not accessible: " + root.getAbsolutePath());
                return;
            }

            long total = root.getTotalSpace();
            long free = root.getFreeSpace();
            long used = total - free;

            if (total <= 0) {
                LOGGER.warning("Invalid storage values received");
                return;
            }

            double percentUsed = (double) used / total * 100;
            updateUIWithValue(percentUsed);
            updateSyncStatus();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating storage", e);
            appendLog("Error updating storage: " + e.getMessage());
        }
    }

    private void updateSyncStatus() {
        if (syncFolder != null) {
            syncStatusLabel.setText("Sync Status: Ready to sync to " + syncFolder.getName());
        } else {
            syncStatusLabel.setText("Sync Status: Disabled");
        }
    }

    public void updateUIWithValue(double percentUsed) {
        DecimalFormat df = new DecimalFormat("#.##");
        storageLabel.setText("Storage Usage: " + df.format(percentUsed) + "%");
        storageBar.setValue((int) percentUsed);

        history.add(percentUsed);
        if (history.size() > HISTORY_MAX_SIZE) {
            history.remove(0);
        }
        graphPanel.repaint();

        double remaining = Math.max(0, 100 - percentUsed);
        int daysLeft = (int) (remaining / DAILY_GROWTH_RATE);
        predictionLabel.setText("Estimated Full in: " + daysLeft + " days");

        if (percentUsed > CRITICAL_THRESHOLD) {
            suggestionLabel.setText("Suggestion: Delete large videos / unused apps");
            storageBar.setForeground(Color.RED);
        } else if (percentUsed > WARNING_THRESHOLD) {
            suggestionLabel.setText("Suggestion: Clear downloads / temp files");
            storageBar.setForeground(Color.ORANGE);
        } else {
            suggestionLabel.setText("Suggestion: Storage is healthy");
            storageBar.setForeground(new Color(34, 139, 34));
        }
    }

    private void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int unit = 1024;
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
