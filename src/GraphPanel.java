import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class GraphPanel extends JPanel {
    private static final int PADDING = 36;
    private static final Color GRID_COLOR = new Color(120, 155, 205, 100);
    private static final Color LINE_COLOR = new Color(72, 156, 255);
    private static final Color BACKGROUND_COLOR = new Color(28, 40, 64);
    private static final Color LABEL_COLOR = new Color(206, 216, 236);
    private static final int LINE_THICKNESS = 3;

    private final ArrayList<Double> data;

    public GraphPanel(ArrayList<Double> data) {
        this.data = data;
        setPreferredSize(new Dimension(520, 280));
        setBackground(BACKGROUND_COLOR);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        drawBackground(g2d, width, height);
        drawGrid(g2d, width, height);
        drawAxes(g2d, width, height);

        if (!data.isEmpty()) {
            drawDataLine(g2d, width, height);
            drawDataPoints(g2d, width, height);
        } else {
            drawPlaceholder(g2d, width, height);
        }
    }

    private void drawBackground(Graphics2D g2d, int width, int height) {
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRoundRect(0, 0, width, height, 24, 24);
    }

    private void drawAxes(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(162, 181, 215, 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(PADDING, height - PADDING, PADDING, PADDING);
        g2d.drawLine(PADDING, height - PADDING, width - PADDING, height - PADDING);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.drawString("100%", 10, PADDING + 15);
        g2d.drawString("0%", 10, height - PADDING + 18);
    }

    private void drawGrid(Graphics2D g2d, int width, int height) {
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1f));
        int availableHeight = height - 2 * PADDING;
        for (int i = 0; i <= 4; i++) {
            int y = height - PADDING - (i * availableHeight / 4);
            g2d.drawLine(PADDING, y, width - PADDING, y);
        }
    }

    private void drawDataLine(Graphics2D g2d, int width, int height) {
        g2d.setColor(LINE_COLOR);
        g2d.setStroke(new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int availableHeight = height - 2 * PADDING;
        int step = data.size() > 1 ? (width - 2 * PADDING) / (data.size() - 1) : 0;

        for (int i = 0; i < data.size() - 1; i++) {
            int x1 = PADDING + i * step;
            int y1 = height - PADDING - (int) (data.get(i) * availableHeight / 100);
            int x2 = PADDING + (i + 1) * step;
            int y2 = height - PADDING - (int) (data.get(i + 1) * availableHeight / 100);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawDataPoints(Graphics2D g2d, int width, int height) {
        g2d.setColor(LINE_COLOR);
        int availableHeight = height - 2 * PADDING;
        int step = data.size() > 1 ? (width - 2 * PADDING) / (data.size() - 1) : 0;
        for (int i = 0; i < data.size(); i++) {
            int x = PADDING + i * step;
            int y = height - PADDING - (int) (data.get(i) * availableHeight / 100);
            g2d.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    private void drawPlaceholder(Graphics2D g2d, int width, int height) {
        g2d.setColor(LABEL_COLOR);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        String text = "Waiting for storage data...";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = height / 2;
        g2d.drawString(text, x, y);
    }
}
