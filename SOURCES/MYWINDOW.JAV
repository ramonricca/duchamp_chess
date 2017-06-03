//	============================================================================================
//  MyWindow.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sat Jan 25 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//
//	Extends NSWindow. Necessary to pass key down events to the input line.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class MyWindow extends NSWindow {
	private MyInputLine inline;
	public String number;
	public boolean playing;
	public boolean gameOver = false;
	public boolean closeMe = false;
	
	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Default constructor. Just calls super. Never called because the windows
	//	are made in Interface Builder.
	//	-------------------------------------------------------------------------
	public MyWindow() {
		super();
	}
	
	//	-------------------------------------------------------------------------
	//	Constructor():	NSRect, int, int, boolean
	//	Called because the window is made in Interface Builder. Sets up the
	//	input line so that all key down events can be passed to it.
	//	-------------------------------------------------------------------------
	public MyWindow(NSRect contentRect, int styleMask, int backingType, boolean defer) {
		super(contentRect, styleMask, backingType, defer);
	}
	
	//	-------------------------------------------------------------------------
	//	Constructor():	NSRect, int, int, boolean, NSScreen
	//	Called because the window is made in Interface Builder. Sets up the
	//	input line so that all key down events can be passed to it.
	//	-------------------------------------------------------------------------
	public MyWindow(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
		super(contentRect, styleMask, bufferingType, defer, aScreen);
	}
	
	//	-------------------------------------------------------------------------
	//	methods:		keyDown():	NSEvent
	//	Passes all key down events to the input line
	//	-------------------------------------------------------------------------
	public void keyDown(NSEvent theEvent) {
		inline.keyDown(theEvent);
	}
	
	//	-------------------------------------------------------------------------
	//	methods:		setGameNumber():	String, boolean
	//	sets flags so that closing the window behaves accordingly
	//	-------------------------------------------------------------------------
	public void setGameNumber(String game, boolean play) {
		number = game;
		playing = play;
	}
	
	//	-------------------------------------------------------------------------
	//	methods:		setGameOver():	boolean
	//	sets flags so that closing the window behaves accordingly
	//	-------------------------------------------------------------------------
	public void setGameOver(boolean go) {
		gameOver = go;
	}
	
	public void setInputLine(MyInputLine in) {
		inline = in;
	}
	
	
}
