package com.classes.assignment2;

import com.example.assignment2.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader.TileMode;
import android.view.View;

/**
 * This class represents the Background of the application including the
 * landscape and the fuel bar. It contains the logic for drawing and updating
 * these elements on the GamePanel.
 **/

public class Background {

	/**
	 * A constant that holds the X axis location to draw the string "Fuel:" to on
	 * the canvas.
	 **/
	public static final int FUELTEXTX = 1;
	/**
	 * A constant that holds the Y axis location to draw the string "Fuel:" to on
	 * the canvas.
	 **/
	public static final int FUELTEXTY = 20;
	/** A constant that holds the location of the left side of the fuel bar.**/
	public static final int FUELBARLEFT = 70;
	/** A constant that holds the location of the top of the fuel bar.**/
	public static final int FUELBARTOP = 10;
	/** A constant that holds the location of the bottom of the fuel bar.**/
	public static final int FUELBARBOTTOM = 20;

	/** The bitmap used to draw the landscape on to the background. **/
	private Bitmap landscapeBitmap;
	/** The BitmapShader that will draw the landscape on to the background. **/
	private BitmapShader landscapeShader;
	/** The Paint that will paint the landscape to the canvas. **/
	private Paint landscapePaint;
	/** The Path used to draw the landscape on to the background. **/
	private Path landscapePath;
	/** An array holding all the x-coordinates to draw the landscape path with. **/
	private int[] xCoordinates;
	/** An array holding all the y-coordinates to draw the landscape path with. **/
	private int[] yCoordinates;
	/**
	 * The region used to detect whether or not the SpaceCraft has collided with
	 * the landscape.
	 **/
	public Region landscapeRegion;
	/** The GamePanel for this application. **/
	private GamePanel gamePanel;
	/** Rect that acts as the FuelBar for the SpaceCraft. **/
	private Rect fuelBar;

	/** The constructor for the Background class. Initializes key variables for the class.**/
	public Background(Resources resources, GamePanel gamePanel) {
		this.gamePanel = gamePanel;
		this.initializeResources(resources);
	}

	/**
	 * This function redraws the fuel bar to the canvas at the correct size
	 * based off the amount of fuel the spaceCraft has left.
	 * 
	 * @Param spaceCraft The SpaceCraft for the application.
	 **/
	public void updateBackground(SpaceCraft spaceCraft) {
		int fuelBarRight = (spaceCraft.getRemainingFuel()) + 70;
		this.fuelBar = new Rect(FUELBARLEFT, FUELBARTOP, fuelBarRight,
				FUELBARBOTTOM);
	}

	/**
	 * This function draws the background (the fuel bar and the landscape) to the
	 * canvas.
	 * 
	 * @Param canvas The canvas for the GamePanel of the application.
	 **/
	public void drawBackground(Canvas canvas, Paint paint, String fuelString) {
		canvas.drawText(fuelString, 1, 20, paint);
		canvas.drawRect(fuelBar, paint);
		canvas.drawPath(landscapePath, landscapePaint);
	}

	/**
	 * Function that initializes the resources for this application. 
	 * 
	 * This function identifies the
	 * SpaceCraft image resources and turns them into bitmaps to be used with
	 * the application. It also initializes several variables used to draw the Landscape.
	 * 
	 * @Param resources The resources for this application.
	 **/
	public void initializeResources(Resources resources) {
		xCoordinates = new int[] { 0, 0, (gamePanel.getWidth() / 4),
				(gamePanel.getWidth() / 3), (gamePanel.getWidth() / 2),
				(gamePanel.getWidth() - gamePanel.getWidth() / 3),
				(gamePanel.getWidth() - gamePanel.getWidth() / 5),
				(gamePanel.getWidth()), (gamePanel.getWidth()) };
		yCoordinates = new int[] { (gamePanel.getHeight()),
				(gamePanel.getHeight() - gamePanel.getHeight() / 8),
				(gamePanel.getHeight() - (gamePanel.getHeight() / 3)),
				(gamePanel.getHeight() - (gamePanel.getHeight() / 7)),
				(gamePanel.getHeight() - (gamePanel.getHeight() / 7)),
				(gamePanel.getHeight() - (gamePanel.getHeight() / 4)),
				(gamePanel.getHeight() - (gamePanel.getHeight() / 6)),
				(gamePanel.getHeight() - (gamePanel.getHeight() / 8)),
				gamePanel.getHeight() };
		this.createLandscapePath();
		landscapeRegion = new Region();
		// The region in this function acts as a bounding box for the path. I am
		// setting it to the size of the SurfaceView.
		landscapeRegion.setPath(this.landscapePath,
				new Region(0, 0, gamePanel.getWidth(), gamePanel.getHeight()));
		landscapeBitmap = BitmapFactory.decodeResource(resources,
				R.drawable.mars);
		landscapeShader = new BitmapShader(landscapeBitmap, TileMode.REPEAT,
				TileMode.REPEAT);
		landscapePaint = new Paint();
		landscapePaint.setColor(Color.RED);
		landscapePaint.setShader(landscapeShader);
	}

	/**
	 * Function that creates the path that defines the shape of the landscape
	 * the SpaceCraft must land on.
	 **/
	public void createLandscapePath() {
		this.landscapePath = new Path();
		landscapePath.moveTo(xCoordinates[0], yCoordinates[0]);
		for (int i = 0; i < xCoordinates.length; i++) {
			landscapePath.lineTo(xCoordinates[i], yCoordinates[i]);
		}
	}

	/**
	 * Returns the Landscape region for the Background.
	 * 
	 * @Return the landscapeRegion for this Background.
	 **/
	public Region getLandscapeRegion() {
		return this.landscapeRegion;
	}

	/**
	 * Returns the array containing the X-Coordinates for the landscape Path.
	 * 
	 * @Return The xCoordinates array containing the X-Coordinates for the
	 *         landscapePath.
	 **/
	public int[] getXCoordinates() {
		return this.xCoordinates;
	}

	/**
	 * Returns the array containing the Y-Coordinates for the landscape Path.
	 * 
	 * @Return The yCoordinates array containing the Y-Coordinates for the
	 *         landscapePath.
	 **/
	public int[] getYCoordinates() {
		return this.yCoordinates;
	}
}
