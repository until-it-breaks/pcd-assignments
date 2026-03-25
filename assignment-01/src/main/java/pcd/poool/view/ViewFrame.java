package pcd.poool.view;

import pcd.poool.controller.commands.CommandProcessor;
import pcd.poool.model.V2d;
import pcd.poool.controller.MoveCommand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewFrame extends JFrame implements KeyListener {
    
    private final VisualiserPanel panel;
    private final ViewModel model;
    private final RenderSync sync;
	private final CommandProcessor controller;

	private final boolean[] keys = new boolean[256];
    
    public ViewFrame(ViewModel model, int w, int h, CommandProcessor commandProcessor){
    	this.model = model;
    	this.sync = new RenderSync();
    	setTitle("Poool");
        setSize(w,h + 25);
        setResizable(false);
        this.panel = new VisualiserPanel(w,h);
        getContentPane().add(panel);
        addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent ev){
				System.exit(-1);
			}
			public void windowClosed(WindowEvent ev){
				System.exit(-1);
			}
		});
		this.controller = commandProcessor;
		this.addKeyListener(this);
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
        
        public VisualiserPanel(int w, int h){
            setSize(w,h + 25);
            ox = w / 2;
            oy = h / 2;
            delta = Math.min(ox, oy);
        }

        public void paint(Graphics g){
    		Graphics2D g2 = (Graphics2D) g;
    		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    		g2.clearRect(0, 0, this.getWidth(), this.getHeight());
    		g2.setColor(Color.LIGHT_GRAY);
		    g2.setStroke(new BasicStroke(1));
    		g2.drawLine(ox, 0, ox, oy * 2);
    		g2.drawLine(0, oy, ox * 2, oy);
    		g2.setColor(Color.BLACK);
			for (var hole: model.getHoles()) {
				int holeRadius = (int)(hole.radius() * delta);
				int x0 = (int)(ox + hole.pos().x() * delta);
				int y0 = (int)(oy - hole.pos().y() * delta);
				g2.fillOval(x0 - holeRadius, y0 - holeRadius, holeRadius * 2, holeRadius * 2);
			}
			for (var ball: model.getBalls()) {
				var pos = ball.pos();
				int x0 = (int)(ox + pos.x() * delta);
				int y0 = (int)(oy - pos.y() * delta);
				int radiusX = (int)(ball.radius() * delta);
				int radiusY = (int)(ball.radius() * delta);
				g2.drawOval(x0 - radiusX, y0 - radiusY, radiusX * 2, radiusY * 2);
			}
			g2.setStroke(new BasicStroke(3));
			var player = model.getPlayerBall();
			if (player != null) {
				var pos = player.pos();
				int x0 = (int)(ox + pos.x() * delta);
				int y0 = (int)(oy - pos.y() * delta);
				int radiusX = (int)(player.radius() * delta);
				int radiusY = (int)(player.radius() * delta);
				g2.drawOval(x0 - radiusX, y0 - radiusY, radiusX * 2, radiusY * 2);
				g2.drawString("P", x0 - (radiusX / 3), y0 + (radiusY / 3));
			}
			var bot = model.getBotBall();
			if (bot != null) {
				var pos = bot.pos();
				int x0 = (int)(ox + pos.x() * delta);
				int y0 = (int)(oy - pos.y() * delta);
				int radiusX = (int)(bot.radius() * delta);
				int radiusY = (int)(bot.radius() * delta);
				g2.drawOval(x0 - radiusX, y0 - radiusY, radiusX * 2, radiusY * 2);
				g2.drawString("B", x0 - (radiusX / 3), y0 + (radiusY / 3));
			}
			g2.setStroke(new BasicStroke(1));
			g2.drawString("Num small balls: " + model.getBalls().size(), 50, 40);
			g2.drawString("Frame per sec: " + model.getFramePerSec(), 50, 60);
			g2.drawString("Player score: " + model.getPlayerScore(), 50, 80);
			g2.drawString("Bot score: " + model.getBotScore(), 50, 100);
			sync.notifyFrameRendered();
        }
    }
}
