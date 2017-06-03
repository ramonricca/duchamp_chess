//	============================================================================================
//  MySeekElement.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Feb 16 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.lang.*;

public class MySeekElement {
	private String playCommand;
	private String name;
	private Integer rating;
	private Integer mins;
	private Integer incr;
	private String rated;
	private String type;
	
	private NSRect rect;
	private long initTime;
	private long timeout = 30000L;
	
	public MySeekElement(	String nm, String rtng, String mi, String ic, 
						String rtd, String tp, String pcom, NSRect bounds) {
		name = nm;
		try {
			rating = new Integer(rtng);
		} catch (Exception e) {
			rating = new Integer(0);
		}
		try {
			mins = new Integer(mi);
			incr = new Integer(ic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rated = rtd;
		type = tp;
		playCommand = pcom;
		initTime = System.currentTimeMillis();

		float x = (rating.floatValue() == 0f) ? bounds.x() : (bounds.width() / 2500f) * rating.floatValue();
		float yDiv = bounds.height() / 60f;
		float y = (yDiv * mins.floatValue()) + (incr.floatValue()) + bounds.y();
		rect = new NSRect(x - 3f, y - 3f, 5f, 5f);
	}
	
	public boolean hasExpired() {
		if (System.currentTimeMillis() > (initTime + timeout)) return true;
		else return false;
	}
	
	public String returnPlayCommand() {
		return playCommand;
	}
	
	public boolean ptInRect(NSPoint point) {
		return rect.containsPoint(point, false) ;
	}
	
	public NSRect rect() {
		return rect;
	}
	
	public boolean isRated() {
		if (rated.equals("rated")) return true;
		else return false;
	}
	
	public String type() {
		return type;
	}
}
