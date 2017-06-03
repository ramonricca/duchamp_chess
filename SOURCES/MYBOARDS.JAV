//	============================================================================================
//  MyBoardSquare.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 26 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class represents a square on the board view. It has the x and y dimensions of the
//	array of boards, returns the rect, path to the image file, and other information.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class MyBoardSquare {
	private NSRect rect;
	private int x;
	private int y;
	private int index;
	private int offset;
	private String imagePath;
	private NSPoint point;
	private String[] imageName = {"WP","WB","WN","WR","WQ","WK","BP","BB","BN","BR","BQ","BK"};
	
	//	-------------------------------------------------------------------------
	//	Constructor():	int, int, NSRect, int
	//	Sets the x and y coordinates of the board array, the Rect itself, and
	//	the Index of the image.	
	//	-------------------------------------------------------------------------
	public MyBoardSquare(int xx, int yy, NSRect initRect, int in, int off) {
		x = xx;
		y = yy;
		rect = initRect;
		index = in;
		offset = off;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		x():		no arguments
	//	returns the x index of the board array
	//	-------------------------------------------------------------------------
	public int x() {
		return x;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		y():		no arguments
	//	returns the y index of the board array
	//	-------------------------------------------------------------------------
	public int y() {
		return y;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		rect():		no arguments
	//	returns the rect
	//	-------------------------------------------------------------------------
	public NSRect rect() {
		return rect;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		index():		no arguments
	//	returns the index of the image of the piece in the square
	//	-------------------------------------------------------------------------
	public int index() {
		return index;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		setIndex():		int
	//	sets the index value
	//	-------------------------------------------------------------------------
	public void setIndex(int id) {
		index = id;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		point():		no arguments
	//	returns the point at the lower left corner of the rect
	//	-------------------------------------------------------------------------
	public NSPoint point() {
		return new NSPoint(rect.x(), rect.y());
	}
	
	//	-------------------------------------------------------------------------
	//	method:		getPath():		no arguments
	//	returns path to the image of the piece in the rect
	//	-------------------------------------------------------------------------
	public String getPath() {
		if ((x != -1) && (y != -1) && (rect != null) && (index != -1)) 
			imagePath = NSBundle.mainBundle().pathForResource(imageName[index] + MyPreferences.returnPieceChoice(), "tif");
		else imagePath = "";
		return imagePath;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		getImage():		no arguments
	//	returns a NSImage representation of the image
	//	-------------------------------------------------------------------------
	public NSImage getImage() {
		return new NSImage(imagePath, false);
	}
	
	//	-------------------------------------------------------------------------
	//	method:		getAlgebNot():		no arguments
	//	returns the algebraic notation of the rect
	//	-------------------------------------------------------------------------
	public String getAlgebNot() {
		String alpha = "abcdefgh";
		String coord = String.valueOf(alpha.charAt(x));
		coord += String.valueOf(y+1);
		return coord;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		offset():		no arguments
	//	returns the offset of the square in terms of the board dump
	//	-------------------------------------------------------------------------
	public int offset() {
		return offset;
	}
		
}
