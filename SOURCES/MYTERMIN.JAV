//	============================================================================================
//  MyTerminalView.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 14 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class defines the terminal text view. It sets the font and size from the prefs(it is
//	hard-coded right now). Otherwise, it is an NSTextView.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class MyTerminalView extends NSTextView {

	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Default constructor. Never gets called since the view is layed out in
	//	Interface Builder.
	//	-------------------------------------------------------------------------
	public MyTerminalView() {
		super();
	}
	
	//	-------------------------------------------------------------------------
	//	Constructor():	NSRect
	//	Gets called because the frame is layed out in Interface Builder. Sets
	//	the fonts and size.
	//	-------------------------------------------------------------------------
	public MyTerminalView(NSRect frameRect) {
		super(frameRect);
	}

	//	-------------------------------------------------------------------------
	//	method:		acceptsFirstResponder():	no parameters
	//	Overrides the NSTextView method so that the user can't enter any text
	//	in the view, but can only enter text on the input line. Returns false
	//	-------------------------------------------------------------------------
	public boolean acceptsFirstResponder() {
		return false;
	}
	
	public void setMyPrefs() {
		setRichText(false);
  		setBackgroundColor(MyPreferences.colorValueForKey(MyPreferences.terminalBackColor));
 		setTextColor(MyPreferences.colorValueForKey(MyPreferences.terminalForeColor));
  		setFont(MyPreferences.getFontForKey(MyPreferences.terminalFont));
		setNeedsDisplay(true);
		setEditable(false);
	}
}
