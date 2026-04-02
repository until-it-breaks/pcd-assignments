package pcd.poool.view;

public class RenderSync {

	private long nextFrameToRender;
	private long lastFrameRendered;
	
	public RenderSync() {
		nextFrameToRender = 0;
		lastFrameRendered = -1;
	}

	public synchronized long nextFrameToRender() {
		long f = nextFrameToRender;
		nextFrameToRender++;
		return f;
	}

	public synchronized void notifyFrameRendered() {
		lastFrameRendered++;
		notifyAll();
	}
	
	public synchronized void waitForFrameRendered(long frame) throws InterruptedException {
		while (lastFrameRendered < frame) {
			wait();
		}
	}
}
