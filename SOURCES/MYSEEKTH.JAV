//	============================================================================================
//  MySeekThread.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Feb 16 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.lang.*;

public class MySeekThread extends Thread {
	private NSMutableArray list;
	private boolean stop = false;
	private boolean working = false;
	private MySeekView view;
	
	public MySeekThread(MySeekView theView) {
		view = theView;
		list = new NSMutableArray();
	}
	
	public synchronized void addElement(MySeekElement element) {
		while (working) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		working = true;
		list.addObject(element);
		working = false;
		notify();
	}
	
	public synchronized void removeElement(int index) {
		while (working) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		working = true;
		list.removeObjectAtIndex(index);
		working = false;
		notify();
	}
	
	public synchronized String findElement(NSPoint point) {
		while (working) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		working = true;
		int i = 0;
		int index = -1;
		boolean found = false;
		while ((i < list.count()) && (!found)) {
			if (((MySeekElement)list.objectAtIndex(i)).ptInRect(point)) {
				index = i;
				found = true;
			}
			i++;
		}
		working = false;
		notify();
		if (found) return ((MySeekElement)list.objectAtIndex(index)).returnPlayCommand();
		else return null;
	}
	
	public void run() {
		int myPool = NSAutoreleasePool.push();
		int ind = 0;
		while (!stop) {
			if (list.count() > 0) {			
				if (((MySeekElement)list.objectAtIndex(ind)).hasExpired()) {
					removeElement(ind);
					view.display();
				}
			}
			ind++;
			if (ind >= list.count()) ind = 0;
		}
		NSAutoreleasePool.pop(myPool);
	}
	
	public synchronized Object[][] getAllRects() {
		while (working) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		working = true;
		Object[][] theElements = new Object[list.count()][3];
		for (int i = 0; i < list.count(); i++) {
			theElements[i][0] = (((MySeekElement)list.objectAtIndex(i)).rect());
			theElements[i][1] = (((MySeekElement)list.objectAtIndex(i)).isRated()) ? 
				NSColor.redColor() : NSColor.blueColor();
			theElements[i][2] = (((MySeekElement)list.objectAtIndex(i)).type());
		}
		working = false;
		notify();
		return theElements;
	}
	
}
