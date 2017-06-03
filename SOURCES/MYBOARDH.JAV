//	============================================================================================
//  MyBoardHeaderView.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 14 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class is a custom view for the header at the top of the board window. This view
//	handles the timers, the highlighting of whose move it is, and is dynamically created
//	according to the size of the window.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.lang.*;

public class MyBoardHeaderView extends NSView {
	private boolean playerOnesTurn;
	
	public NSTextField name1;
	public NSTextField name2;

	private MyTimerThread timer;
	
	private NSRect head1;
	private NSRect head2;
	
	private String player1Name;
	private String player2Name;
	
	//	-------------------------------------------------------------------------
	//	Constructor():	NSRect
	//	Morphs the constructor in NSView.  Initializes the colors, fonts from
	//	prefs and also creates the text fields to hold the players names. The
	//	size of the split view is determined by halving the entire rect.
	//	-------------------------------------------------------------------------
	public MyBoardHeaderView(NSRect frameRect) {
		super(frameRect);
		NSRect temp = convertRectFromView(frameRect, superview());
		
		NSImage whitePawn = new NSImage(NSBundle.mainBundle().pathForResource("WP0", "tif"), false);
		NSImage blackPawn = new NSImage(NSBundle.mainBundle().pathForResource("BP0", "tif"), false);
		whitePawn.setFlipped(true);
		blackPawn.setFlipped(true);
		
		float half = temp.width() / 2f;
		head1 = new NSRect(temp.x(), temp.y(), half, temp.height());
		head2 = new NSRect(temp.x() + half, temp.y(), half, temp.height());
		
		name1 = new NSTextField(getNameRect(head1));
		name2 = new NSTextField(getNameRect(head2));
		name1.setCell(new MyHeaderFieldCell(whitePawn));
		name2.setCell(new MyHeaderFieldCell(blackPawn));
				
		addSubview(name1);
		addSubview(name2);		
	}
	
	//	-------------------------------------------------------------------------
	//	method:		drawRect():	NSRect
	//	Morphs the method in NSView.  Highlights the correct rect according to
	//	whose turn it is.
	//	-------------------------------------------------------------------------
	public void drawRect(NSRect viewRect) {
		if (playerOnesTurn) {
			name1.setTextColor(NSColor.redColor());
			name2.setTextColor(NSColor.blackColor());
		} else {
			name1.setTextColor(NSColor.blackColor());
			name2.setTextColor(NSColor.redColor());
		}
		
		name1.setNeedsDisplay(true);
		name2.setNeedsDisplay(true);

	}
	
	//	-------------------------------------------------------------------------
	//	method:		initialize():	String, String
	//	Sets the players' names in the header
	//	-------------------------------------------------------------------------
	public void initialize(String p1, String p2) {
		player1Name = p1;
		player2Name = p2;
		
		for (int i = 0; i < (17 - p1.length()); i++) {
			player1Name += " ";
		}
		for (int i = 0; i < (17 - p2.length()); i++) {
			player2Name += " ";
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		toggle():	Integer, Integer, boolean
	//	Sets the new time in the header, and sets whose turn it is
	//	-------------------------------------------------------------------------
	public void toggle(Integer t1, Integer t2, boolean whitesTurn) {
		setTime(t1, t2);
		playerOnesTurn = whitesTurn;
		stopTime();
		startTime(t1, t2, playerOnesTurn);
	}
		
	//	-------------------------------------------------------------------------
	//	method:		getNameRect():	NSRect
	//	returns the Name rect from the header rect, for constructing the textview
	//	-------------------------------------------------------------------------
	public NSRect getNameRect(NSRect frame) {
/* 		float y = frame.y(); */
/* 		float x = frame.x() + ((frame.width() - 192f) / 2f); */
/* 		float width = 192f; */
/* 		float height = 20f; */
		return new NSRect(frame.x(), frame.y(), frame.width(), 20f);
	}
	
	//	-------------------------------------------------------------------------
	//	method:		startTime():	Integer, Integer, boolean
	//	starts the timer according to whose turn it is
	//	-------------------------------------------------------------------------
	public void startTime(Integer secs1, Integer secs2, boolean p1Turn) {
		NSTextField temp = (p1Turn) ? name1 : name2;
		Integer secs = (p1Turn) ? secs1 : secs2;
		String name = (p1Turn) ? player1Name : player2Name;
		
		timer = new MyTimerThread(secs, temp, name);
		timer.setPriority(Thread.MIN_PRIORITY);
		timer.start();
	}
	
	//	-------------------------------------------------------------------------
	//	method:		stopTime():	no arguments
	//	Stops the current timer
	//	-------------------------------------------------------------------------
	public void stopTime() {
		if (timer != null) {
			timer.setStop();
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		setTime():	Integer, Integer
	//	Sets the time in the header view
	//	-------------------------------------------------------------------------
	public void setTime(Integer t1, Integer t2) {
		name1.setStringValue(player1Name + getTimeFormat(t1));
		name2.setStringValue(player2Name + getTimeFormat(t2));
	}

	//	-------------------------------------------------------------------------
	//	static method:		getTimeFormat():	Integer
	//	returns the time, formatted like this:
	//	hours:minutes:seconds
	//	-------------------------------------------------------------------------
	public static String getTimeFormat(Integer seconds) {
		String neg = (seconds.intValue() < 0) ? "-" : " ";
		int h = seconds.intValue() / 3600;
		int m = seconds.intValue() / 60;
		int s = seconds.intValue() - (h * 3600) - (m * 60);

		String hstr = String.valueOf(Math.abs(h));
		int lh = hstr.length();
		if (lh == 1) hstr = "0" + hstr;
		
		String mstr = String.valueOf(Math.abs(m));
		int lm = mstr.length();
		if (lm == 1) mstr = "0" + mstr;
		
		String sstr = String.valueOf(Math.abs(s));
		int ls = sstr.length();
		if (ls == 1) sstr = "0" + sstr;
		
		return (neg + hstr + ":" + mstr + ":" + sstr);
	} 
	
	public boolean playerOnesTurn() {
		return playerOnesTurn;
	}

}
