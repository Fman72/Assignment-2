package com.classes.assignment2;

import com.example.assignment2.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

/** This is the GamePanel SurfaceView that the games animations are drawn on. **/

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

	/** The thread to run the animation for the application. **/
	private GameThread gameThread;
	/** The context of the application. **/
	private Context context;
	/** The SpaceCraft object to be displayed for the application. **/
	private SpaceCraft spaceCraft;
	/** The Background object to be displayed for the application. **/
	private Background background;
	/** The paint object used to paint to the canvas. **/
	private Paint gamePanelPaint;
	/**
	 * An AlertDialog that tells the user the game is over and presents the with
	 * some options.
	 **/
	private AlertDialog gameOverDialog;
	/** Button that allows the user to reset the game on the gameOverDialog. **/
	private Button playAgainButton;

	/**
	 * Constructor for the GamePanel class. Creates the GamePanel class and
	 * initializes key variables.
	 * 
	 * @Param context The context of the application.
	 * @Param attrs The attributes associated with this XML layout.
	 **/
	public GamePanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		this.context = context;
		this.gamePanelPaint = new Paint();
		this.gamePanelPaint.setColor(Color.WHITE);
		this.gamePanelPaint.setTextSize(25);
		this.spaceCraft = new SpaceCraft(getResources(), this);
		this.initializeGameOverDialog();
	}

	/**
	 * This function is called each time the thread runs, it moves the
	 * SpaceCraft in a direction depending on what buttons are/are not pressed,
	 * this function also stops the GameThread when the game is over.
	 **/
	public void updateCanvas() {
		boolean cont = spaceCraft.updateSpaceCraft(
				background.getLandscapeRegion(), background.getXCoordinates(),
				background.getYCoordinates());
		background.updateBackground(spaceCraft);
		if (cont == false) {
			//Stopping the gameThread.
			gameThread.setIsRunning(false);
			// Using the SurfaceView's post method to publish an event the the
			// UI thread's queue. (The event being displaying the
			// gameOverDialog).
			this.post(new Runnable() {
				@Override
				public void run() {
					GamePanel.this
							.displayGameOverDialog(GamePanel.this.spaceCraft
									.hasCrashed());
				}
			});
		}

	}

	/**
	 * This function redraws the canvas with the new location/bitmap for the
	 * space craft.
	 * 
	 * @Param canvas The canvas used for this SurfaceView.
	 **/
	public void drawCanvas(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		background.drawBackground(canvas, gamePanelPaint,
				context.getString(R.string.fuel_string));
		spaceCraft.drawSpaceCraft(canvas, gamePanelPaint);
	}

	/**
	 * Function that runs when this surface view is created, it starts the
	 * gameThread and creates the background object.
	 * 
	 * @param holder
	 *            The SurfaceHolder for this surface view.
	 **/
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.background = new Background(getResources(), this);
		this.gameThread = new GameThread(this, getHolder());
		gameThread.setIsRunning(true);
		gameThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.gameThread.setIsRunning(false);
	}

	/**
	 * Function that initializes and registers the OnTouchListeners for the
	 * buttons used to control the SpaceCraft.
	 * 
	 * @Param mainActivity The MainActivity of this application, used to
	 *        retrieve views in the application.
	 **/
	public void registerOnTouchListeners(MainActivity mainActivity) {
		// Initializing the button views as Java Button variables.
		Button leftButton = (Button) mainActivity
				.findViewById(R.id.left_button);
		Button rightButton = (Button) mainActivity
				.findViewById(R.id.right_button);
		Button upButton = (Button) mainActivity.findViewById(R.id.up_button);
		// Registering the buttons OnTouchListeners as the SpaceCraft class.
		leftButton.setOnTouchListener(spaceCraft);
		rightButton.setOnTouchListener(spaceCraft);
		upButton.setOnTouchListener(spaceCraft);
	}

	/**
	 * Function that initializes the gameOverDialog that prompts the user if
	 * they have won/lost. The user is able to play again from this dialog.
	 **/
	public void initializeGameOverDialog() {
		gameOverDialog = new AlertDialog.Builder(context).create();
		// Inflating the dialogView with the wrapper to ensure it's layout is
		// preserved.
		View dialogView = (LayoutInflater.from(context).inflate(
				R.layout.dialog_layout, MainActivity.dialogWrapperView, false));
		gameOverDialog.setView(dialogView);
		// Stopping the user from dismissing the dialog.
		gameOverDialog.setCanceledOnTouchOutside(false);
		// Retrieving the Button form the dialog and registering an OnClick
		// listener to it that resets the game.
		playAgainButton = (Button) dialogView
				.findViewById(R.id.play_again_button);
		playAgainButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// Disabling the button once it is clicked to prevent the
					// dialog form reappearing if the user clicks it many times
					// due to their eagerness to play again.
					v.setEnabled(false);
					//Creating a new gameThread, resetting and restarting the game.
					GamePanel.this.gameThread = new GameThread(GamePanel.this, GamePanel.this.getHolder());
					GamePanel.this.resetGame();
					GamePanel.this.gameThread.start();
					gameOverDialog.dismiss();
				}
				return true;
			}
		});
	}

	/**
	 * Function that displays the gameOverDialog and updates the dialogs message
	 * based on whether the user has won/lost.
	 * 
	 * @Param crashed A boolean indicating whether or not the SpaceCraft has
	 *        crashed.
	 **/
	public void displayGameOverDialog(boolean crashed) {
		//Enabling the playAgainButton in case it has been disabled by past clicks.
		playAgainButton.setEnabled(true);
		// Changing the text in the dialog based on whether the user has
		// one/lost the game.
		if (crashed) {
			gameOverDialog.setMessage("Oh No, you crashed!");
		} else {
			gameOverDialog.setMessage("Congratulations, you landed safely!");
		}
		gameOverDialog.show();
	}

	/**
	 * Function that resets the game so the user can play again. Resets the
	 * SpaceCraft and the landscape back to their starting values.
	 **/
	public void resetGame() {
		this.spaceCraft.resetSpaceCraft();
		this.gameThread.setIsRunning(true);
	}
}