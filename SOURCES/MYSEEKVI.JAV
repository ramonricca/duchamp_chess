//	============================================================================================
//  MySeekView.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 24 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class is a custom view that draws the Seek Graph.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import com.stevesoft.pat.*;

public class MySeekView extends NSView {
	private	MySeekThread t;
	private NSRect graphBounds;
	private MyMainWindowController cont;
	private float topX;
	private float lefY;
	private float botY;
	private float rigX;
	private NSColor backColor;
	private NSColor foreColor;
	private NSTextField ratingAxis;
	private NSTextField timeAxis;
	private NSTextField origin;
	private NSTextField maxRating;
	private NSTextField maxTime;
	
	//	-------------------------------------------------------------------------
	//	Constructor():	no arguments
	//	Default constructor, does nothing but morph the super class constructor
	//	-------------------------------------------------------------------------
	public MySeekView() {
		super();
	}
	
	//	-------------------------------------------------------------------------
	//	Constructor():
	//	-------------------------------------------------------------------------
	public MySeekView(NSRect frame) {
		super(frame);
		t = new MySeekThread(this);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		topX = frame.x() + 18f;
		lefY = frame.y() + frame.height() - 18f;
		botY = frame.y() + 18f;
		rigX = frame.x() + frame.width() - 18f;
		graphBounds = new NSRect(topX + 1f,botY + 1f, rigX - topX, lefY - botY);

		//	ratingAxis
		ratingAxis = new NSTextField();
		ratingAxis.setFont(NSFont.fontWithNameAndSize("Helvetica", 9f));
		ratingAxis.setStringValue("Rating");
		ratingAxis.sizeToFit();
		NSRect frm = ratingAxis.frame();
		ratingAxis.setFrame(new NSRect((rigX - frm.width())/2f, 
			(botY - frm.height())/2f, frm.width(), frm.height()));
			
		//	timeAxis
		timeAxis = new NSTextField();
		timeAxis.setFont(NSFont.fontWithNameAndSize("Helvetica", 9f));
		timeAxis.setStringValue("Time");
		timeAxis.sizeToFit();
		frm = timeAxis.frame();
		timeAxis.setFrame(new NSRect((topX - 10f), lefY + 2, frm.width(), frm.height()));
		
		//	origin
		origin = new NSTextField();
		origin.setFont(NSFont.fontWithNameAndSize("Helvetica", 9f));
		origin.setStringValue("0");
		origin.sizeToFit();
		frm = origin.frame();
		origin.setFrame(new NSRect((topX - frm.width() - 1), (botY - frm.height() - 1), frm.width(), frm.height()));
		
		//	maxRating
		maxRating = new NSTextField();
		maxRating.setFont(NSFont.fontWithNameAndSize("Helvetica", 9f));
		maxRating.setStringValue("2500");
		maxRating.sizeToFit();
		frm = maxRating.frame();
		maxRating.setFrame(new NSRect((rigX - (frm.width()/2f)), (botY - frm.height())/2f, frm.width(), frm.height()));
		
		//	maxTime
		maxTime = new NSTextField();
		maxTime.setFont(NSFont.fontWithNameAndSize("Helvetica", 9f));
		maxTime.setStringValue("60");
		maxTime.sizeToFit();
		frm = maxTime.frame();
		maxTime.setFrame(new NSRect((topX - frm.width()), (lefY - frm.height())+(frm.height()/2f), frm.width(), frm.height()));
	}
	
	//	-------------------------------------------------------------------------
	//	drawRect():	draws the view
	//	-------------------------------------------------------------------------
	public void drawRect(NSRect frame) {
		backColor.set();
		NSBezierPath.fillRect(frame);
		foreColor.set();
		NSBezierPath.strokeLineFromPoint(new NSPoint(topX,lefY), new NSPoint(topX,botY));
		NSBezierPath.strokeLineFromPoint(new NSPoint(topX,botY), new NSPoint(rigX,botY));
		
		Object[][] obj = t.getAllRects();
		NSRect[] rects = new NSRect[obj.length];
		NSColor[] colors = new NSColor[obj.length];
		String[] type = new String[obj.length];
		
		for (int i = 0; i < obj.length; i++) {
			rects[i] = (NSRect)obj[i][0];
			colors[i] = (NSColor)obj[i][1];
			type[i] = (String)obj[i][2];
		}
		NSGraphics.fillRectListWithColors(rects, colors);
		for (int i = 0; i < obj.length; i++) {
			NSMutableRect tempRect = new NSMutableRect(rects[i]);
			tempRect.insetRect(1f,1f);
			if (type[i].equals("crazyhouse")) {
				NSGraphics.drawDarkBezel(rects[i], tempRect);
			} else if (type[i].equals("suicide")) {
				NSGraphics.drawGrayBezel(rects[i], tempRect);
			} else if (type[i].equals("wild")) {
				NSGraphics.drawGroove(rects[i], tempRect);
			}
		}
	}

	//	-------------------------------------------------------------------------
	//	addSeek():	adds a seek to the seek list
	//	-------------------------------------------------------------------------
	public void addSeek(String dump) {
		MySeekElement elem;
		Regex RESeek = MyRegularExpressions.sharedInstance().getSeek();
		int type = ((MyMainWindowController)NSApplication.sharedApplication().delegate()).returnBoardController().returnServerType();
		RESeek.search(dump);
		if (type == 0) {
			elem = new MySeekElement(	RESeek.stringMatched(1), 	/* Name */
									RESeek.stringMatched(2),	/* Rating */
									RESeek.stringMatched(3),	/* Minutes */
									RESeek.stringMatched(4),	/* Increment */
									RESeek.stringMatched(5),	/* Rated */
									RESeek.stringMatched(6),	/* Type */
									RESeek.stringMatched(7),	/* Play String */
									graphBounds );
		} else {
			elem = new MySeekElement( RESeek.stringMatched(1), 	/* Name */
									RESeek.stringMatched(2),	/* Rating */
									RESeek.stringMatched(4),	/* Minutes */
									RESeek.stringMatched(5),	/* Increment */
									RESeek.stringMatched(6),	/* Rated */
									RESeek.stringMatched(3),	/* Type */
									RESeek.stringMatched(7),	/* Play String */
									graphBounds );
		}
		
		t.addElement(elem);
		display();
	}
	
	//	-------------------------------------------------------------------------
	//	mouseDown():	adds a seek to the seek list
	//	-------------------------------------------------------------------------
	public void mouseDown(NSEvent theEvent) {
		NSPoint pt = convertPointFromView(theEvent.locationInWindow(), null);
		String playCommand = t.findElement(pt);
		if (playCommand != null) {
			//System.out.println(playCommand);
			cont.sendText(playCommand);
		}
	}

	//	-------------------------------------------------------------------------
	//	method:		acceptsFirstMouse():	NSEvent
	//	-------------------------------------------------------------------------
	public boolean acceptsFirstMouse(NSEvent event) {
		return false;
	}
	
	//	-------------------------------------------------------------------------
	//	setMyPrefs():	sets the prefs after the instance is created
	//	-------------------------------------------------------------------------
	public void setMyPrefs() {
		backColor = MyPreferences.colorValueForKey(MyPreferences.seekBackColor);
		foreColor = MyPreferences.colorValueForKey(MyPreferences.seekForeColor);
		cont = ((MyMainWindowController)(NSApplication.sharedApplication().delegate()));
		
		setup(ratingAxis);
		setup(timeAxis);
		setup(origin);
		setup(maxRating);
		setup(maxTime);
	}
	
	//	-------------------------------------------------------------------------
	//	setup():	sets up the field to common values
	//	-------------------------------------------------------------------------
	public void setup(NSTextField field) {
		field.setDrawsBackground(false);
		field.setBordered(false);
		field.setBezeled(false);
		field.setTextColor(foreColor);
		addSubview(field);
	}
}
