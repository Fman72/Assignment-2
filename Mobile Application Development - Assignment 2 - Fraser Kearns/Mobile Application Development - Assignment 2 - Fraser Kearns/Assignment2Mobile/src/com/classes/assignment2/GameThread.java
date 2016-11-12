package com.classes.assignment2;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * This is the thread that displays content on the GamePanel SurfaceView of the
 * application.
 **/

public class GameThread extends Thread {

	/** The GamePanel for this application. **/
	private GamePanel gamePanel;
	/** Boolean indicating whether or not this thread should keep running. **/
	private boolean isRunning;
	/** The SurfaceHolder for the GamePanel SurfaceView **/
	private SurfaceHolder surfaceHolder;

	/**
	 * The constructor for this GameThread - initializes key variables for the
	 * class.
	 **/
	public GameThread(GamePanel gamePanel, SurfaceHolder surfaceHolder) {
		super();
		this.gamePanel = gamePanel;
		this.surfaceHolder = surfaceHolder;
	}

	/**
	 * This function calls various function in GamePanel that update the content
	 * on the GamePanel and redraw the content onto the SurfaceView.
	 **/
	public void run() {

		Canvas canvas;
		while (isRunning) {
			canvas = null;
			// Try to update the content on the GamePanel.
			try {
				canvas = surfaceHolder.lockCanvas();
				synchronized (surfaceHolder) {
					gamePanel.updateCanvas();
					gamePanel.drawCanvas(canvas);
				}
			} finally {
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
			// Putting this thread to sleep to free up the processor for other
			// threads.
			try {
				Thread.sleep((long) 40);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Function that updates the flag that indicates whether or not the
	 * GameThread should continue running.
	 * 
	 * @Param isRunning a boolean indicating whether or not the GameThread
	 *        should continue running.
	 **/
	public void setIsRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

}
