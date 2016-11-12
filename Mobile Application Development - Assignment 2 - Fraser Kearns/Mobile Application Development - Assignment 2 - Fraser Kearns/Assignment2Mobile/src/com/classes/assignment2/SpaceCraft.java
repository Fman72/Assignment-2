package com.classes.assignment2;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.example.assignment2.R;

/**
 * This class contains all the data about the SpaceCraft in the application.
 * This includes data used to display the SpaceCraft (its position,
 * currentBitmap and how to wrap the bitmap) as well as data used by the game's
 * logic (remainingFuel, direction and SPEED).
 **/

public class SpaceCraft implements OnTouchListener {

	/** Constant int used to represent the direction UP.**/
	public static final int UP = 0;
	/** Constant int used to represent the direction DOWN.**/
	public static final int DOWN = 1;
	/** Constant int used to represent the direction LEFT.**/
	public static final int LEFT = 2;
	/** Constant int used to represent the direction RIGHT.**/
	public static final int RIGHT = 3;
	/** Constant int used to represent the speed of the SpaceCraft.**/
	private static final int SPEED = 10;

	/** The bitmaps used to show the SpaceCraft when none of it's boosters are firing. **/
	private Bitmap noBoosterOnBitmap;
	/** The bitmaps used to show the SpaceCraft when its main booster is firing. **/
	private Bitmap mainBoosterOnBitmap;
	/** The bitmaps used to show the SpaceCraft when its left booster is firing. **/
	private Bitmap leftBoosterOnBitmap;
	/** The bitmaps used to show the SpaceCraft when its right booster is firing. **/
	private Bitmap rightBoosterOnBitmap;
	/** The bitmap currently used to draw the SpaceCraft. **/
	private Bitmap currentBitmap;

	/** The GamePanel for this application. **/
	private GamePanel gamePanel;

	/** An int that represents the amount of fuel the spaceCraft has left. **/
	private int remainingFuel;
	/**
	 * The current direction the SpaceCraft is traveling in. Initialized as
	 * DOWN because gravity is the first force that acts on the SpaceCraft.
	 **/
	private int currentDirection = DOWN;
	/**
	 * An int that represents the force due to gravity acting on the SpaceCraft.
	 **/
	private int gravitationalPull;
	
	/** The matrix used to rotate and draw the spaceCraft. **/
	private Matrix spaceCraftMatrix;
	/**
	 * An array containing the Bitmaps that make up the SpaceCraft's explosion
	 * animation.
	 **/
	private Bitmap[] explosionFrames;
	/** A boolean indicating whether or not the SpaceCraft has crashed. **/
	boolean crashed;
	/** The Bitmap used to show the SpaceCraft's wreckage. **/
	private Bitmap craterAndWreckageBitmap;

	/**
	 * An integer tracking what crash animation frame the SpaceCraft is
	 * displaying.
	 **/
	int currentCrashFrame;
	/**
	 * A point used to represent the X,Y coordinates of the SpaceCraft's current
	 * position. This is the top left point of the SpaceCraft bitmap.
	 **/
	private Point currentPosition;

	/**
	 * The constructor for the SpaceCraft, initializes important variables.
	 * 
	 * @Param resources The resources for the application - used by this
	 *        function to get the images to draw for the SpaceCraft.
	 **/
	public SpaceCraft(Resources resources, GamePanel gamePanel) {
		// Initializing the SpaceCraft's initial position.
		this.gamePanel = gamePanel;
		this.currentPosition = new Point(100, 100);
		this.currentDirection = DOWN;
		this.currentBitmap = noBoosterOnBitmap;
		this.initializeResources(resources);
		this.remainingFuel = 100;
		this.spaceCraftMatrix = new Matrix();
		this.spaceCraftMatrix
				.setTranslate(currentPosition.x, currentPosition.y);
		this.crashed = false;
		this.currentCrashFrame = 0;
	}

	/**
	 * This function checks for collisions between the SpaceCraft and the
	 * LandScape. This function uses a combination of bounding algorithms and
	 * image masking to detect collisions.
	 * 
	 * This function uses the width and height of the noBoosterOn bitmap to
	 * detect collisions as this prevents the booster's flames from causing
	 * collisions.
	 * 
	 * @Param landscapeRegion The landscapeRegion for the Background of the
	 *        application. Used to detect if a collision has occurred.
	 **/
	public boolean checkForCollision(Region landscapeRegion) {
		Rect spaceCraftRect = new Rect(currentPosition.x, currentPosition.y,
				(currentPosition.x + noBoosterOnBitmap.getWidth()),
				(currentPosition.y + noBoosterOnBitmap.getHeight()));
		if (!landscapeRegion.quickReject(spaceCraftRect)) {
			for (int y = 0; y++ < noBoosterOnBitmap.getHeight(); y++) {
				for (int x = 0; x++ < noBoosterOnBitmap.getWidth(); x++) {
					if (landscapeRegion.contains(currentPosition.x + x,
							currentPosition.y + y)) {
						if (!(noBoosterOnBitmap.getPixel(x, y) == Color.TRANSPARENT)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * This function checks if the location the SpaceCraft landed on is flat
	 * enough for the landing to be successful.
	 * 
	 * @Param xCoordinates An integer array containing the x co-ordinates of all
	 *        the points on the landscape path.
	 * @Param yCoordinates An integer array containing the y co-ordinates of all
	 *        the points on the landscape path.
	 **/
	public boolean isLandingSafe(int[] xCoordinates, int[] yCoordinates) {
		// Creating an array containing the x co-ordinates of the leftmost and
		// rightmost points of the spaceCraft. Adding the 5px offsets to make it slightly easier to land the spaceCraft.
		int spaceCraftPoints[] = { currentPosition.x + 5,
				(currentPosition.x + noBoosterOnBitmap.getWidth() - 5) };
		// Accounting for the SpaceCrafts bitmap being wrapped which could make
		// the right side of the SpaceCraft's x co-ordinates be greater than the
		// width of the GamePanel.
		if (spaceCraftPoints[1] > gamePanel.getWidth()) {
			spaceCraftPoints[1] -= gamePanel.getWidth();
		}
		// Finding the contour(s) of the landscape that the SpaceCraft has
		// landed on.
		for (int spaceCraftPoint : spaceCraftPoints) {
			boolean searching = true;
			int i = 1;
			while (searching) {
				int pathContourStart = xCoordinates[i];
				int pathContourEnd = xCoordinates[i + 1];
				if ((spaceCraftPoint > pathContourStart)
						&& (spaceCraftPoint < pathContourEnd)) {
					searching = false;
					// Finding the steepness of the countour the SpaceCraft has
					// landed on.
					float xDifference = xCoordinates[i] - xCoordinates[i + 1];
					float yDifference = yCoordinates[i] - yCoordinates[i + 1];
					float landingZoneSlope = Math
							.abs(yDifference / xDifference);
					// If the countour is too steep the SpaceCraft has crashed.
					if (landingZoneSlope > .5) {
						return false;
					}
				}
				i++;
			}
		}
		return true;

	}

	/**
	 * This function checks if the color that is passed to it is transparent.
	 * 
	 * @Param color The color this function will check the transparency of.
	 **/
	public boolean isTransparent(int color) {
		if (color != Color.TRANSPARENT) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Function that updates the SpaceCraft so that it displays a crater and
	 * some wreckage.
	 **/
	public void displayCraterAndWreckage() {
		if (currentBitmap != craterAndWreckageBitmap) {
			this.currentBitmap = craterAndWreckageBitmap;
			// Lowering the SpaceCraft's current position so that the Crater and
			// wreckage appear inside the landscape.
			this.currentPosition.y += 70;
			spaceCraftMatrix.setTranslate(currentPosition.x, currentPosition.y);
		} else {
			return;
		}
	}

	/**
	 * This function is called each time the GameThread runs, it updates the
	 * position of the SpaceCraft based off of the buttons the users is pressing
	 * it also detects if a collision has occurred.
	 * 
	 * @Param landscapeRegion The landscapeRegion for the Background of the
	 *        application. Used to detect if a collision has occurred.
	 * @Param xCoordinates An integer array containing the x co-ordinates of all
	 *        the points on the landscape path - used to detect if the
	 *        SpaceCraft has landed safely.
	 * @Param yCoordinates An integer array containing the y co-ordinates of all
	 *        the points on the landscape path - used to detect if the
	 *        SpaceCraft has landed safely.
	 **/
	public boolean updateSpaceCraft(Region landscapeRegion, int[] xCoordinates,
			int[] yCoordinates) {
		// If SpaceCraft has collided with landscape did it crash.
		if (this.checkForCollision(landscapeRegion) == true) {
			// If the SpaceCraft has crashed either display the
			// currentCrashFrame
			// or if the SpaceCraft is finished exploding
			// display the craterAndWreckageBitmap.
			if (!isLandingSafe(xCoordinates, yCoordinates)) {
				crashed = true;
				if (currentCrashFrame == explosionFrames.length - 1) {
					this.displayCraterAndWreckage();
					return false;
				} else {
					currentBitmap = explosionFrames[currentCrashFrame];
					currentCrashFrame++;
				}
			} else {
				return false;
			}
		}
		// If it has not crashed and has fuel move the SpaceCraft.
		else if (remainingFuel > 0) {
			switch (currentDirection) {
			case RIGHT:
				currentPosition.x -= SPEED;
				gravitationalPull = 10;
				currentBitmap = rightBoosterOnBitmap;
				remainingFuel = remainingFuel - 2;
				break;
			case LEFT:
				currentPosition.x += SPEED;
				gravitationalPull = 10;
				currentBitmap = leftBoosterOnBitmap;
				remainingFuel = remainingFuel - 2;
				break;
			case UP:
				currentPosition.y -= SPEED;
				gravitationalPull = 10;
				currentBitmap = mainBoosterOnBitmap;
				remainingFuel = remainingFuel - 2;
				break;
			case DOWN:
				currentPosition.y += gravitationalPull;
				gravitationalPull += 3;
				currentBitmap = noBoosterOnBitmap;
				break;
			}
			// If it has not crashed and has no fuel make the SpaceCraft
			// descend.
		} else {
			currentPosition.y += gravitationalPull;
			gravitationalPull += 3;
			currentBitmap = noBoosterOnBitmap;
		}
		return true;
	}

	/**
	 * Function that draws the SpaceCraft to the canvas.
	 * 
	 * @Param canvas The canvas for the GamePanel of the application.
	 **/
	public void drawSpaceCraft(Canvas canvas, Paint paint) {
		if (this.detectWrapAround()) {
			this.wrapBitmap(canvas, paint);
		}
		canvas.drawBitmap(currentBitmap, currentPosition.x, currentPosition.y, paint);
	}

	/**
	 * Function that detects if the SpaceCraft's position requires it's Bitmap
	 * to be wrapped around and resets the currentPosition if it stretches past the
	 * edge of the screen.
	 **/
	public boolean detectWrapAround() {
		// If the SpaceCraft's bitmap stretches beyond the edge of the GamePanel
		// wrap it around.
		if ((currentPosition.x + currentBitmap.getWidth()) > gamePanel
				.getWidth()) {
			// If currentPosition.x is greater than the width of the GamePanel set it to 0.
			if (currentPosition.x > gamePanel.getWidth()) {
				currentPosition.x = 0;
			}
			return true;
			// If currentPosition.x is less than 0 set it to the width of the GamePanel.
		} else if (currentPosition.x < 0) {
			currentPosition.x = gamePanel.getWidth();
			return true;
		}
		return false;
	}

	/**
	 * Function that wraps the SpaceCraft bitmap around the edges of the screen
	 * if it comes into contact with the edge of the screen.
	 * 
	 * @Param canvas The canvas for the game that the bitmap's are drawn to.
	 * @Param paint The GamePanel's paint object used to draw the Bitmap to the
	 *        canvas.
	 */
	public void wrapBitmap(Canvas canvas, Paint paint) {
		// If the SpaceCraft is drawn to far to the left wrap around to the
		// right.
		if ((currentPosition.x + currentBitmap.getWidth()) > gamePanel
				.getWidth()) {
			// Calculating the point to split the Bitmap in half at.
			int splitPoint = (gamePanel.getWidth() - currentPosition.x);
			// Creating a Rect using the splitPoint that will be used to select
			// the portion of the Bitmap that is wrapped around.
			Rect sourceRect = new Rect(splitPoint, 0, currentBitmap.getWidth(),
					currentBitmap.getHeight());
			// Creating rect that will be used to position the split Bitmap on
			// the screen.
			Rect destinationRect = new Rect(0, currentPosition.y,
					sourceRect.width(), currentPosition.y + sourceRect.height());
			canvas.drawBitmap(currentBitmap, sourceRect, destinationRect, paint);
		}
	}

	/**
	 * Function that changes the direction of the SpaceCraft based on what
	 * button the user is pressing.
	 * 
	 * @Param v The view that fired the touch event to this function.
	 * @Param event The MotionEvent passed to this function.
	 **/
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.right_button:
				currentDirection = RIGHT;
				break;
			case R.id.left_button:
				currentDirection = LEFT;
				break;
			case R.id.up_button:
				currentDirection = UP;
				break;
			}
			return true;
		} else {
			currentDirection = DOWN;
			return true;
		}

	}

	/**
	 * Function that takes the resources for this application, identifies the
	 * SpaceCraft image resources and turns them into bitmaps to be used with
	 * the application. This function creates Bitmap's for the SpaceCraft's
	 * movement, explosion and wreckage.
	 * 
	 * @Param resources The resources for this application.
	 **/
	public void initializeResources(Resources resources) {
		explosionFrames = new Bitmap[] {
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_one)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_two)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_three)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_four)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_five)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_six)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_seven)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_eight)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_nine)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_ten)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_eleven)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_twelve)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_thirteen)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_fourteen)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_fifteen)),
				(BitmapFactory.decodeResource(resources,
						R.drawable.explosion_sixteen)) };
		noBoosterOnBitmap = BitmapFactory.decodeResource(resources,
				R.drawable.no_booster_on);
		mainBoosterOnBitmap = BitmapFactory.decodeResource(resources,
				R.drawable.main_booster_on);
		leftBoosterOnBitmap = BitmapFactory.decodeResource(resources,
				R.drawable.left_booster_on);
		rightBoosterOnBitmap = BitmapFactory.decodeResource(resources,
				R.drawable.right_booster_on);
		craterAndWreckageBitmap = BitmapFactory.decodeResource(resources,
				R.drawable.crater_and_wreckage);
	}

	/**
	 * Function that resets the SpaceCraft's variables back to what they are
	 * when the game starts.
	 **/
	public void resetSpaceCraft() {
		this.currentPosition = new Point(100, 100);
		this.currentDirection = DOWN;
		this.currentBitmap = noBoosterOnBitmap;
		this.remainingFuel = 100;
		this.currentCrashFrame = 0;
		this.gravitationalPull = 10;
		this.crashed = false;
	}

	/**
	 * Function the returns the amount of fuel the spaceCraft has left.
	 * 
	 * @return The amount of fuel the spaceCraft has left.
	 **/
	public int getRemainingFuel() {
		return remainingFuel;
	}

	/**
	 * Function that returns a boolean indicating if the SpaceCraft has crashed
	 * or not.
	 * 
	 * @return A boolean indicating whether or not the SpaceCraft has crashed.
	 */
	public boolean hasCrashed() {
		return crashed;
	}
}
