//	============================================================================================
//  MyTimerThread.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Fri Jan 24 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//
//	This is the Timer class that counts down a players time when it is their move.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.lang.*;

public class MyTimerThread extends Thread {
	private int seconds;
	private boolean stop = false;
	private NSTextField view;
	private String name;
	
	//	-------------------------------------------------------------------------
	//	Constructor():	Integer, NSTextField
	//	Default constructor. Gets the seconds and sets the view to display
	//	-------------------------------------------------------------------------
	public MyTimerThread(Integer secs, NSTextField tf, String nm) {
		if (secs != null) seconds = secs.intValue();
		view = tf;
		name = nm;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		run():	no parameters
	//	The thread run method. Decrements the seconds every second.
	//	-------------------------------------------------------------------------
	public void run() {
		while (!stop) {
			long start = System.currentTimeMillis();
			while ((System.currentTimeMillis() < start + 1000L) && (!stop)) {
			}
			if (!stop) {
				seconds--;
				view.setStringValue(name + MyBoardHeaderView.getTimeFormat(new Integer(seconds)));
				view.setNeedsDisplay(true);
			}
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		setStop():	no parameters
	//	Sets a flag to stop the timer.
	//	-------------------------------------------------------------------------
	public void setStop() {
		stop = true;
	}
	
}
