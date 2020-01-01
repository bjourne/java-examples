import static java.lang.Math.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.Timer;
import java.util.TimerTask;

class PaintSurface extends JPanel implements KeyListener {
    static final Dimension SURFACE_DIM = new Dimension(400, 600);
    static final Font SCORE_FONT = new Font("Calibri", Font.PLAIN, 30);

    // The ball's position and directed speed.
    Rectangle ball;
    int ballXSpeed = 10;
    int ballYSpeed = 10;

    // The paddle's position, speed and movement direction.
    Rectangle paddle;
    int paddleSpeed = 10;
    boolean movingLeft = false;
    boolean movingRight = false;

    // The player's score (number of errors).
    int score;

    public PaintSurface() {
        setBackground(Color.BLACK);
        setPreferredSize(SURFACE_DIM);

        ball = new Rectangle(0, 0, 40, 40);
        int width = 70, height = 15;
        paddle = new Rectangle(5, SURFACE_DIM.height - height - 5,
                               width, height);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(), 100, 17);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g2d.fill(ball);
        g.setColor(Color.RED);
        g2d.fill(paddle);
        g.setColor(Color.GREEN);
        g.setFont(SCORE_FONT);
        g.drawString(score + "", 200, 50);
    }
    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
            movingLeft = true;
            break;
        case KeyEvent.VK_RIGHT:
            movingRight = true;
            break;
        }
    }
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
            movingLeft = false;
            break;
        case KeyEvent.VK_RIGHT:
            movingRight = false;
            break;
        }
    }
    private class ScheduleTask extends TimerTask {
        public void run() {
            int surfaceWidth = SURFACE_DIM.width;
            int surfaceHeight = SURFACE_DIM.height;
            if (ball.x < 0 || ball.x > surfaceWidth - ball.width) {
                ballXSpeed = -ballXSpeed;
            }
            if (ball.y < 0 || ball.y > surfaceHeight - ball.height) {
                ballYSpeed = -ballYSpeed;
            }
            if (ball.intersects(paddle)) {
                ballYSpeed = -ballYSpeed;
            }
            if (ball.y >= surfaceHeight - paddle.height - 15) {
                score++;
            }
            ball.x += ballXSpeed;
            ball.y += ballYSpeed;

            if (movingLeft) {
                paddle.x -= paddleSpeed;
            } else if (movingRight) {
                paddle.x += paddleSpeed;
            }
            paddle.x = max(0, min(paddle.x, surfaceWidth - paddle.width));
            SwingUtilities.invokeLater(() -> repaint());
        }
    }
}

public class Pong {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
                PaintSurface ps = new PaintSurface();
                JFrame frame = new JFrame();
                frame.add(ps);
                frame.addKeyListener(ps);
                frame.setTitle("Pong");
                frame.setResizable(false);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            });
    }
}
