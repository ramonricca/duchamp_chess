//	============================================================================================
//  MyInputLine.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 14 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class handles the input line, which extends NSTextView. The key down method overrides
//	the standard key down to send the text to the server when return is pressed. As of 30130,
//	the input line key code is hard coded to a decimal number. I am planning on making a 
//	unicode equivalent in the future.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class MyInputLine extends NSTextView {
	MyMainWindowController mainControl;
	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Default constructor. Never gets called since the view is layed out in
	//	Interface Builder.
	//	-------------------------------------------------------------------------
	public MyInputLine() {
		super();
	}
	
	//	-------------------------------------------------------------------------
	//	Constructor():	NSRect
	//	Gets called from the .nib file. 
	//	-------------------------------------------------------------------------
	public MyInputLine(NSRect frameRect) {
		super(frameRect);
	}

	//	-------------------------------------------------------------------------
	//	method:		keyDown():	NSEvent
	//	Overrides keyDown in NSTextView. When the user presses return, the 
	//	method invokes a selector to send the input but getting the text from
	//	the input line in the receiving method, sends it, then clears the line.
	//	-------------------------------------------------------------------------
	public void keyDown(NSEvent theEvent) {
		short ch = theEvent.keyCode();
		if (ch == 36) {
			mainControl.sendInput(string());
		} else {
			super.keyDown(theEvent);
		}
	}
	
	public void setControl(MyMainWindowController main) {
		mainControl = main;
	}
	
	public void setMyPrefs() {
		setRichText(false);
  		//setBackgroundColor(MyPreferences.colorValueForKey(MyPreferences.terminalBackColor));
 		//setTextColor(MyPreferences.colorValueForKey(MyPreferences.terminalForeColor));
  		setFont(MyPreferences.getFontForKey(MyPreferences.terminalFont));
		setNeedsDisplay(true);
	}

}
