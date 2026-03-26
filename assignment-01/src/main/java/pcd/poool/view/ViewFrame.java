package pcd.poool.view;

import pcd.poool.controller.commands.CommandProcessor;
import pcd.poool.model.V2d;
import pcd.poool.controller.commands.MoveCommand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ViewFrame extends JFrame implements KeyListener {
    
    private final VisualiserPanel panel;
    private final ViewModel model;
    private final RenderSync sync;
	private final CommandProcessor controller;

	private final boolean[] keys = new boolean[256];

	public ViewFrame(ViewModel model, int width, int height, CommandProcessor commandProcessor){
		this.model = model;
		this.sync = new RenderSync();
		this.panel = new VisualiserPanel(width, height);
		this.controller = commandProcessor;
		getContentPane().add(panel);
		setTitle("Poool");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setFocusable(true);
		requestFocusInWindow();
		addKeyListener(this);
		pack();
		setLocationRelativeTo(null);
	}
     
    public void render(){
		long nextFrame = sync.nextFrameToRender();
        panel.repaint();
		try {
			sync.waitForFrameRendered(nextFrame);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    }

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if (code < 256) keys[code] = true;
		processInput();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		if (code < 256) keys[code] = false;
		processInput();
	}

	private void processInput() {
		double vx = 0;
		double vy = 0;
		double speed = 1.0;

		if (keys[KeyEvent.VK_W]) vy += speed;
		if (keys[KeyEvent.VK_S]) vy -= speed;
		if (keys[KeyEvent.VK_A]) vx -= speed;
		if (keys[KeyEvent.VK_D]) vx += speed;

		if (vx != 0 || vy != 0) {
			controller.notifyNewCommand(new MoveCommand(new V2d(vx, vy)));
		}
	}

	public class VisualiserPanel extends JPanel {
        private final int ox;
        private final int oy;
        private final int delta;

        public VisualiserPanel(int width, int height){
			setPreferredSize(new Dimension(width, height));
            ox = width / 2;
            oy = height / 2;
            delta = Math.min(ox, oy);
        }

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setColor(Color.LIGHT_GRAY);
			g2.setStroke(new BasicStroke(1));
			g2.drawLine(ox, 0, ox, oy * 2);
			g2.drawLine(0, oy, ox * 2, oy);
			g2.setColor(Color.BLACK);
			for (var hole : model.getHoles()) {
				drawFilledCircle(g2, hole.pos().x(), hole.pos().y(), hole.radius());
			}
			g2.setStroke(new BasicStroke(1));
			for (var ball : model.getBalls()) {
				drawCircle(g2, ball.pos().x(), ball.pos().y(), ball.radius());
			}
			g2.setStroke(new BasicStroke(3));
			drawBallWithLetter(g2, model.getPlayerBall(), "P");
			drawBallWithLetter(g2, model.getBotBall(), "B");
			drawGameInfo(g2);
			sync.notifyFrameRendered();
		}

		private void drawFilledCircle(Graphics2D g2, double x, double y, double radius) {
			int r = (int) (radius * delta);
			int x0 = (int) (ox + x * delta);
			int y0 = (int) (oy - y * delta);
			g2.fillOval(x0 - r, y0 - r, r * 2, r * 2);
		}

		private void drawCircle(Graphics2D g2, double x, double y, double radius) {
			int r = (int) (radius * delta);
			int x0 = (int) (ox + x * delta);
			int y0 = (int) (oy - y * delta);
			g2.drawOval(x0 - r, y0 - r, r * 2, r * 2);
		}

		private void drawBallWithLetter(Graphics2D g2, BallViewInfo ball, String letter) {
			if (ball == null) return;
			int r = (int) (ball.radius() * delta);
			int x0 = (int) (ox + ball.pos().x() * delta);
			int y0 = (int) (oy - ball.pos().y() * delta);
			g2.drawOval(x0 - r, y0 - r, r * 2, r * 2);
			g2.drawString(letter, (int)(x0 - ball.radius() / 4 * delta), (int)(y0 + ball.radius() / 4 * delta));
		}

		private void drawGameInfo(Graphics2D g2) {
			g2.setStroke(new BasicStroke(1));
			g2.drawString("Num small balls: " + model.getBalls().size(), 50, 40);
			g2.drawString("Frame per sec: " + model.getFramePerSec(), 50, 60);
			g2.drawString("Player score: " + model.getPlayerScore(), 50, 80);
			g2.drawString("Bot score: " + model.getBotScore(), 50, 100);
		}
    }
}
