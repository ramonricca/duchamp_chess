//	============================================================================================
//  MyExampleBoard.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 24 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class is a custom view that draws a small sample board in the Preferences window
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class MyExampleBoard extends NSView {
	private String[] imageName = {"WP","WB","WN","WR","WQ","WK","BP","BB","BN","BR","BQ","BK"};
	private NSMutableArray images = new NSMutableArray();
	private NSRect squares[][] = new NSRect[6][5];
	private NSColor lightColor;
	private NSColor darkColor;
	//	-------------------------------------------------------------------------
	//	Constructor():	no arguments
	//	Default constructor, does nothing but morph the super class constructor
	//	-------------------------------------------------------------------------
	public MyExampleBoard() {
		super();
	}
	
	//	-------------------------------------------------------------------------
	//	Constructor():	NSRect
	//	Initializes the board array, images of the chess pieces, initial dump
	//	-------------------------------------------------------------------------
	public MyExampleBoard(NSRect frameRect) {
		super(frameRect);
	}

	//	-------------------------------------------------------------------------
	//	method:		drawRect():	NSRect
	//	Draws the entire view, then calls another method to draw the pieces
	//	-------------------------------------------------------------------------
	public void drawRect(NSRect viewRect) {
		//	Entire Board Needs to Be Drawn
		lockFocus();
		boolean cube = false;
		NSImage mask = new NSImage(NSBundle.mainBundle().pathForResource("squareMask", "tif"), false);
		float boardWidth = MyPreferences.getBoardSize();
		lightColor = MyPreferences.colorValueForKey(MyPreferences.lightSquaresColor);
		darkColor = MyPreferences.colorValueForKey(MyPreferences.darkSquaresColor);
		if (MyPreferences.intValueForKey(MyPreferences.cubeMask) == NSCell.OnState) cube = true;
		
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 5; j++) {
				squares[i][j] = new NSRect((i * boardWidth), (j * boardWidth), boardWidth, boardWidth);
				if (isSquareBlack(i, j)) darkColor.set();
				else lightColor.set();
				
				NSBezierPath.fillRect(squares[i][j]);
				NSColor.blackColor().set();
				NSGraphics.frameRect(squares[i][j]);
				if (cube) mask.drawInRect(squares[i][j], new NSRect(0f,0f,48f,48f), NSImage.CompositeSourceOver,1f);
			}
		}
		NSGraphics.frameRect(viewRect);
		NSGraphicsContext.currentContext().setImageInterpolation(MyPreferences.aliasValue());
		((NSImage)images.objectAtIndex(0)).drawInRect(squares[0][1], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(0)).drawInRect(squares[1][2], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(0)).drawInRect(squares[2][1], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(5)).drawInRect(squares[0][0], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(3)).drawInRect(squares[1][0], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(3)).drawInRect(squares[2][0], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(6)).drawInRect(squares[0][2], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(6)).drawInRect(squares[3][2], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(10)).drawInRect(squares[3][1], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(7)).drawInRect(squares[1][1], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		((NSImage)images.objectAtIndex(8)).drawInRect(squares[3][0], 
			new NSRect(0f,0f,108f,108f), NSImage.CompositeSourceOver,1f);
		unlockFocus();
	}

	//	-------------------------------------------------------------------------
	//	method:		isSquareBlack():	int, int
	//	returns boolean whether the square is black
	//	-------------------------------------------------------------------------
	public boolean isSquareBlack(int x, int y) {
		int remainx = (x % 2);
		int remainy = (y % 2);
		boolean black = (remainx == remainy) ? true : false;
		return black;
	}

	public void getImages(int index) {
		float boardWidth = MyPreferences.getBoardSize();
		images = new NSMutableArray();
		for (int i = 0; i < 12; i++) {
			images.addObject(new NSImage(NSBundle.mainBundle().pathForResource(imageName[i]+
				String.valueOf(index), "tif"), false));
		}
	}
}
