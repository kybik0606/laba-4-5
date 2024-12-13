import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class GraphDisplay extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private double minX = -10;
    private double maxX = 10;
    private double minY = -10;
    private double maxY = 10;

    private double initialMinX, initialMaxX, initialMinY, initialMaxY;
    private Point startPoint = null;
    private Rectangle zoomRect = null;

    private List<Point> points = new ArrayList<>();
    private Point hoverPoint = null;

    // Settings for display options
    private boolean showAxes = true;
    private boolean showMarkers = true;

    public GraphDisplay() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        initialMinX = minX;
        initialMaxX = maxX;
        initialMinY = minY;
        initialMaxY = maxY;

        //Обработчик мыши
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                checkHover(e.getPoint());
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (startPoint != null) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int width = Math.abs(startPoint.x - e.getX());
                    int height = Math.abs(startPoint.y - e.getY());
                    zoomRect = new Rectangle(x, y, width, height);
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    startPoint = e.getPoint();
                    zoomRect = null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && zoomRect != null) {
                    applyZoom(zoomRect);
                    startPoint = null;
                    zoomRect = null;
                    repaint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    resetZoom();
                    repaint();
                }
            }
        });

        generatePoints();
    }

    private void generatePoints() {
        double step = 0.1;
        for (double x = minX; x <= maxX; x += step) {
            double y = Math.sin(x);  // Example function y = sin(x)
            int screenX = (int) ((x - minX) / (maxX - minX) * WIDTH);
            int screenY = HEIGHT - (int) ((y - minY) / (maxY - minY) * HEIGHT);
            points.add(new Point(screenX, screenY));
        }
    }

    private void checkHover(Point mousePoint) {
        hoverPoint = null;
        for (Point p : points) {
            if (mousePoint.distance(p) < 6) {  // Tolerance for hover detection
                hoverPoint = p;
                break;
            }
        }
    }

    private void applyZoom(Rectangle zoomRect) {
        double x1 = minX + zoomRect.x * (maxX - minX) / WIDTH;
        double x2 = minX + (zoomRect.x + zoomRect.width) * (maxX - minX) / WIDTH;
        double y1 = maxY - (zoomRect.y + zoomRect.height) * (maxY - minY) / HEIGHT;
        double y2 = maxY - zoomRect.y * (maxY - minY) / HEIGHT;

        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minY = Math.min(y1, y2);
        maxY = Math.max(y1, y2);

        points.clear();
        generatePoints();
    }

    private void resetZoom() {
        minX = initialMinX;
        maxX = initialMaxX;
        minY = initialMinY;
        maxY = initialMaxY;

        points.clear();
        generatePoints();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Smoothing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw axes if enabled
        if (showAxes) {
            drawAxes(g2d);
        }

        // Draw function line
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Draw function points (markers) if enabled
        if (showMarkers) {
            for (Point p : points) {
                // Check if the integer part of the Y value is even
                double yValue = minY + (HEIGHT - p.y) * (maxY - minY) / HEIGHT;
                if (Math.floor(yValue) % 2 == 0) {
                    g2d.setColor(Color.RED);  // Highlight even integer part values in red
                } else {
                    g2d.setColor(Color.YELLOW);  // Default color for other points
                }
                drawStarMarker(g2d, p);  // Draw star marker
            }
        }

        // Draw zoom rectangle if active
        if (zoomRect != null) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            g2d.draw(zoomRect);
        }

        // Show coordinates if hovering over a point
        if (hoverPoint != null) {
            g2d.setColor(Color.BLACK);
            double x = minX + hoverPoint.x * (maxX - minX) / WIDTH;
            double y = maxY - hoverPoint.y * (maxY - minY) / HEIGHT;
            g2d.drawString(String.format("(%.2f, %.2f)", x, y), hoverPoint.x + 10, hoverPoint.y - 10);
        }
    }

    private void drawAxes(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, HEIGHT / 2, WIDTH, HEIGHT / 2);
        g2d.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
    }

    private void drawStarMarker(Graphics2D g2d, Point point) {
        int size = 11;
        int halfSize = size / 2;
        int x = point.x;
        int y = point.y;

        // Draw horizontal and vertical lines
        g2d.drawLine(x - halfSize, y, x + halfSize, y);  // Horizontal line
        g2d.drawLine(x, y - halfSize, x, y + halfSize);  // Vertical line

        // Draw diagonal lines
        g2d.drawLine(x - halfSize, y - halfSize, x + halfSize, y + halfSize);  // First diagonal
        g2d.drawLine(x - halfSize, y + halfSize, x + halfSize, y - halfSize);  // Second diagonal
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Graph Display with Zoom and Hover");
        GraphDisplay graphDisplay = new GraphDisplay();

        // Create menu
        JMenuBar menuBar = new JMenuBar();
        JMenu graphMenu = new JMenu("График");
        JMenuItem toggleAxes = new JMenuItem("Показать оси");
        JMenuItem toggleMarkers = new JMenuItem("Показать маркеры");

        // Toggle axes visibility
        toggleAxes.addActionListener(e -> {
            graphDisplay.showAxes = !graphDisplay.showAxes;
            graphDisplay.repaint();
        });

        // Toggle markers visibility
        toggleMarkers.addActionListener(e -> {
            graphDisplay.showMarkers = !graphDisplay.showMarkers;
            graphDisplay.repaint();
        });

        graphMenu.add(toggleAxes);
        graphMenu.add(toggleMarkers);
        menuBar.add(graphMenu);

        // Set up the frame
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(graphDisplay);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
