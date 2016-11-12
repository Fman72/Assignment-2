package com.classes.assignment2;

import com.example.assignment2.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

/** This is the MainActivity for the application that is displayed when it starts.**/

public class MainActivity extends Activity {

	/** The SurfaceView the SpaceCraft and Background is animated on. **/
	private GamePanel gamePanel;
	/** A view used as the root when inflating dialogs views. **/
	public static ViewGroup dialogWrapperView;

	/**
	 * Function that creates the MainActivity, initializes key variables and
	 * sets the content view of the MainActivity.
	 **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Initializing view group used to build dialogs.
		dialogWrapperView = (ViewGroup) this
				.findViewById(R.layout.layout_container);
		this.gamePanel = (GamePanel) this.findViewById(R.id.game_panel);
		gamePanel.registerOnTouchListeners(this);
	}
}
