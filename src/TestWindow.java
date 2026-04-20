import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

class TestWindow extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(TestWindow.class.getName());

    private static final int WINDOW_WIDTH = 420;
    private static final int WINDOW_HEIGHT = 260;
    private static final int PADDING = 18;

    private JSlider slider;
    private JLabel valueLabel;

    public TestWindow() {
        setTitle("Test Storage Levels");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI();
        setVisible(true);
        LOGGER.info("Test window opened");
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(PADDING, PADDING));
        panel.setBackground(new Color(18, 28, 44));
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        valueLabel = new JLabel("Simulated: 50%", SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(new Color(238, 242, 247));

        slider = new JSlider(0, 100, 50);
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(new Color(18, 28, 44));
        slider.setForeground(new Color(238, 242, 247));

        slider.addChangeListener(e -> {
            int value = slider.getValue();
            valueLabel.setText("Simulated: " + value + "%");
        });

        JButton applyBtn = new JButton("Apply to Main UI");
        applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        applyBtn.setForeground(new Color(238, 242, 247));
        applyBtn.setBackground(new Color(72, 156, 255));
        applyBtn.setFocusPainted(false);
        applyBtn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        applyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyBtn.addActionListener(e -> applyToMainUI());

        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 12, 12));
        contentPanel.setOpaque(false);
        contentPanel.add(valueLabel);
        contentPanel.add(slider);
        contentPanel.add(applyBtn);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(26, 38, 62));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(72, 156, 255, 120), 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        card.add(contentPanel, BorderLayout.CENTER);

        panel.add(card, BorderLayout.CENTER);
        add(panel);
    }

    private void applyToMainUI() {
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof SmartStorageUI) {
                ((SmartStorageUI) frame).updateUIWithValue(slider.getValue());
                LOGGER.info("Applied value: " + slider.getValue() + "% to main UI");
            }
        }
    }
}
