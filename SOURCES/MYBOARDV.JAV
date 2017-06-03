//	============================================================================================
//  MyBoardView.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 24 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class is a custom view that draws the actual chess board. It handles the drag and
//	drop movements. It draws the pieces in the specified squares of the board dump.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class MyBoardView extends NSView implements NSDraggingSource {			
	private NSRect squares[][] = new NSRect[8][8];
	private NSArray images;
	private NSColor lightColor;
	private NSColor darkColor;
	private String currentDump;
	private String lastDump;
	private String currentMove;
	private boolean isPlaying = false;
	private boolean myMove;
	private int amWhite;
	private boolean whitesMove;
	private boolean flip = false;
	private float squareSize;
	private NSRect pieceRect;
	private NSImage mask;
	private boolean cMask = false;
	
	private NSSound mSound = null;
	private NSSound tSound = null;
	
	MyMainWindowController cont;
	MyBoardSquare originSquare = null;
	
	//	-------------------------------------------------------------------------
	//	Constructor():	no arguments
	//	Default constructor, does nothing but morph the super class constructor
	//	-------------------------------------------------------------------------
	public MyBoardView() {
		super();
	}
	
	//	-------------------------------------------------------------------------
	//	Constructor():	NSRect
	//	Initializes the board array, images of the chess pieces, initial dump
	//	-------------------------------------------------------------------------
	public MyBoardView(NSRect frameRect) {
		super(frameRect);
		//	Set the main controller variable
		cont = (MyMainWindowController)NSApplication.sharedApplication().delegate();
		//	Get the chess sets, board array, and colors from the Preferences
		images = MyPreferences.returnChessSet();
		squares = MyPreferences.returnSquares();
		lightColor = MyPreferences.colorValueForKey(MyPreferences.lightSquaresColor);
		darkColor = MyPreferences.colorValueForKey(MyPreferences.darkSquaresColor);
		if (MyPreferences.intValueForKey(MyPreferences.cubeMask) == NSCell.OnState) cMask = true;
		//	Initialize parameters
		squareSize = 48f;	//	Will be gotten from the Preferences eventually
		pieceRect = new NSRect(0f,0f,squareSize,squareSize);
		mask = new NSImage(NSBundle.mainBundle().pathForResource(
			"squareMask", "tif"), false);
		//	Initialize dump to empty			
		currentDump = "                                                                ";
		lastDump = currentDump;
		
		registerForDraggedTypes(new NSArray(NSPasteboard.TIFFPboardType));
		mSound = MyPreferences.getMySound();
		tSound = MyPreferences.getTheirSound();
	}

	//	-------------------------------------------------------------------------
	//	method:		setPlaying():	boolean
	//	Sets the flag whether the board is a playing board or nor
	//	-------------------------------------------------------------------------
	public void setPlaying(boolean in) {
		isPlaying = in;
	}
			
	//	-------------------------------------------------------------------------
	//	method:		drawRect():	NSRect
	//	Draws the entire view, then calls another method to draw the pieces
	//	-------------------------------------------------------------------------
	public void drawRect(NSRect viewRect) {
		//	Entire Board Needs to Be Drawn
		for (int j = 0; j < 8; j++) {
			for (int i = 0; i < 8; i++) {
				if (isSquareBlack(i, j)) darkColor.set();
				else lightColor.set();
				int x = convertCoord(i);
				int y = convertCoord(j);
				NSBezierPath.fillRect(squares[x][y]);
				NSColor.blackColor().set();
				NSGraphics.frameRect(squares[x][y]);
				if (cMask)
					mask.drawInRect(squares[x][y], new NSRect(0f,0f,48f,48f), NSImage.CompositeSourceOver,1f);
				int imageIndex = getImageIndex(currentDump.charAt((j * 8) + i));
				if (imageIndex != -1) {
					//	Pieces are already scaled and drawn with the chosen anti-alias setting
					((NSImage)images.objectAtIndex(imageIndex)).drawInRect
						(squares[x][y], pieceRect, NSImage.CompositeSourceOver,1f);
				}
			}
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		update():	no arguments
	//	Checks to see if there is a difference between the current dump and the
	//	previous dump, and draws individual pieces if there is a difference
	//	-------------------------------------------------------------------------
	public void update() {
		for (int off = 0; off < 64; off++) {
			if (currentDump.charAt(off) != lastDump.charAt(off)) {
				int y = convertCoord(off / 8);
				int x = convertCoord(off - ((off / 8) * 8));
				int imageIndex = getImageIndex(currentDump.charAt(off));
		
				lockFocus();
				if (isSquareBlack(x, y)) darkColor.set();
				else lightColor.set();
				
				NSBezierPath.fillRect(squares[x][y]);
				NSColor.blackColor().set();
				NSGraphics.frameRect(squares[x][y]);
				if (cMask)
					mask.drawInRect(squares[x][y], new NSRect(0f,0f,48f,48f), NSImage.CompositeSourceOver,1f);
				if (imageIndex != -1) {
					//	Pieces are already scaled and drawn with the chosen anti-alias setting
					NSImage theImage = (NSImage)images.objectAtIndex(imageIndex);
					((NSImage)images.objectAtIndex(imageIndex)).drawInRect(squares[x][y], 
						pieceRect, NSImage.CompositeSourceOver,1f);
				}
				unlockFocus();
				displayRect(squares[x][y]);
			}
		}
		currentMove = "";
		if (isPlaying) {
			if ((myMove) && (mSound != null)) mSound.play();
			else if ((myMove) && (tSound != null)) tSound.play();
		} else {
			if ((whitesMove) && (mSound != null)) mSound.play();
			else if ((!whitesMove) && (tSound != null)) tSound.play();
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		setDump():	String
	//	Sets the last dump to the current one, and then sets the current dump
	//	-------------------------------------------------------------------------
	public void setDump(String theDump) {
		String temp = "";
		for (int i = 7; i >= 0; i--) {
			temp += theDump.substring(i*8, i*8 + 8);
		}
		lastDump = currentDump;
		currentDump = temp;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		getImageIndex():	char
	//	returns the index of the image by looking at the character passed
	//	-------------------------------------------------------------------------
	public int getImageIndex (char c) {
		String imlist = "PBNRQKpbnrqk";
		int imageIndex = imlist.lastIndexOf(c);
		return imageIndex;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		mouseDown():	NSEvent
	//	Checks to see if user has clicked on a piece, and then drags it
	//	-------------------------------------------------------------------------
	public void mouseDown(NSEvent theEvent) {
		if ((isPlaying) && (myMove)) {
			NSPoint pt = convertPointFromView(theEvent.locationInWindow(), null);
			currentMove = "";
			originSquare = getSquare(pt);
			if ((isMyPiece(originSquare.index())) && (originSquare.index() != -1)) {
				currentMove = originSquare.getAlgebNot();
				
/* 				NSImage newImage = (NSImage)images.objectAtIndex(originSquare.index()); */
/* 				NSImage tempImage = new NSImage(originSquare.getPath(), false); */
/* 				tempImage.setScalesWhenResized(true); */
/* 				tempImage.setSize(new NSSize(squareSize,squareSize)); */
/* 				 */
/* 				newImage.lockFocus(); */
/* 				NSGraphicsContext.currentContext().setImageInterpolation(NSGraphicsContext.ImageInterpolationHigh); */
/* 				tempImage.compositeToPoint(new NSPoint(0f,0f), NSImage.CompositeSourceOver); */
/* 				newImage.unlockFocus(); */

				NSPasteboard pb = NSPasteboard.pasteboardWithName("DragPboard");
				//pb.setStringForType(originSquare.getAlgebNot(), NSPasteboard.TIFFPboardType);
				dragImage((NSImage)images.objectAtIndex(originSquare.index()), 
					originSquare.point(), new NSSize(0f, 0f), theEvent, pb, this, false);
			}
		} else {
			originSquare = null;
		}
	}
					
	//	-------------------------------------------------------------------------
	//	method:		getSquare():	NSPoint
	//	returns a MyBoardSquare object corresponding to the point passed
	//	-------------------------------------------------------------------------
	public MyBoardSquare getSquare(NSPoint pt) {
		int y, x, j, i, uncorrectedX, uncorrectedY;
		
		x = ((new Float(pt.x()).intValue()) / (new Float(squareSize)).intValue());
		y = ((new Float(pt.y()).intValue()) / (new Float(squareSize)).intValue());
		if ((x >= 0) && (x <= 7) && (y >= 0) && (y <= 7)) {
			uncorrectedX = x;
			uncorrectedY = y;
			i = convertCoord(x);
			j = convertCoord(y);
			int index = getImageIndex(currentDump.charAt((j * 8) + i));
			//	If there is a piece in the clicked square return a new MyBoardSquare instance
			if (index != -1) return new MyBoardSquare(i, j, squares[uncorrectedX][uncorrectedY], index, ((j * 8) + i));
			//	If there isn't, return what info we can
			else return new MyBoardSquare(i, j, squares[uncorrectedX][uncorrectedY], -1, ((j * 8) + i));
		//	if the square clicked isn't on the grid, return a null instance
		} else return new MyBoardSquare(-1,-1,null,-1, -1);
	}
	
	//	-------------------------------------------------------------------------
	//	method:		draggingSourceOperationMaskForLocal():	boolean
	//	Required to be restrict the types of drags locally
	//	-------------------------------------------------------------------------
	public int draggingSourceOperationMaskForLocal(boolean isLocal) {
		if (isLocal) return NSDraggingInfo.DragOperationMove;
		else return NSDraggingInfo.DragOperationNone;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		startedDraggingImage():	NSImage, NSPoint
	//	Outlines the origin for drag square with white
	//	-------------------------------------------------------------------------
	public void startedDraggingImage( NSImage anImage, NSPoint aPoint) {
		if (originSquare.index() != -1) {
			int x = convertCoord(originSquare.x());
			int y = convertCoord(originSquare.y());
			NSMutableRect rect = new NSMutableRect(squares[x][y]);
			rect.insetRect(1f,1f);
			
			lockFocus();
			NSColor.whiteColor().set();
			NSGraphics.frameRectWithWidth(rect, 2f);
			unlockFocus();
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		movedDraggingImage():	NSImage, NSPoint
	//	required for NSDraggingSource implementation. Does nothing.
	//	-------------------------------------------------------------------------
	public void movedDraggingImage( NSImage draggedImage, NSPoint screenPoint) {
	}
	
	//	-------------------------------------------------------------------------
	//	method:		ignoreModifierKeysWhileDragging():	No Parameters
	//	ignores all modifier keys while dragging
	//	-------------------------------------------------------------------------
	public boolean ignoreModifierKeysWhileDragging() {
		return true;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		namesOfPromisedFilesDroppedAtDestination():	URL
	//	doesn't drop anything on any other application
	//	-------------------------------------------------------------------------
	public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
		return null;
	}

	//	-------------------------------------------------------------------------
	//	method:		finishedDraggingImage():	NSImage, NSPoint, boolean
	//	Gets the MyBoardSquare where the image was dropped, and sends the move
	//	to the input stream
	//	-------------------------------------------------------------------------
	public void finishedDraggingImage( NSImage anImage, NSPoint aPoint, boolean flag) {
		NSPoint pt = window().convertScreenToBase(aPoint);
		MyBoardSquare bs = getSquare(convertPointToView(pt, this));
		if ((bs.x() != originSquare.x()) || (bs.y() != originSquare.y())) {
			currentMove += "-" + bs.getAlgebNot();
			if (((currentMove.equals("e1-g1")) && (amWhite == 1) && (originSquare.index() == 5)) || 
				((currentMove.equals("e8-g8")) && (amWhite == 0) && (originSquare.index() == 11))) {
					currentMove = "0-0";
			} else if (((currentMove.equals("e1-c1")) && (amWhite == 1) && (originSquare.index() == 5)) || 
						((currentMove.equals("e8-c8")) && (amWhite == 0) && (originSquare.index() == 11))) {
							currentMove = "0-0-0";
			}
				
			cont.sendText(currentMove);
		}
		
		int x = convertCoord(originSquare.x());
		int y = convertCoord(originSquare.y());
		NSMutableRect rect = new NSMutableRect(squares[x][y]);
		rect.insetRect(1f,1f);
		
		lockFocus();
		if (isSquareBlack(x, y)) darkColor.set();
		else lightColor.set();
		NSGraphics.frameRectWithWidth(rect, 2f);
		unlockFocus();
		currentMove = "";
		originSquare = null;
	}

	//	-------------------------------------------------------------------------
	//	method:		finishedDraggingImage():	NSImage, NSPoint, int
	//	Gets the MyBoardSquare where the image was dropped, and sends the move
	//	to the input stream
	//	-------------------------------------------------------------------------
	public void finishedDraggingImage( NSImage anImage, NSPoint aPoint, int operation) {
		NSPoint pt = window().convertScreenToBase(aPoint);
		MyBoardSquare bs = getSquare(convertPointToView(pt, this));
		if ((bs.x() != originSquare.x()) || (bs.y() != originSquare.y())) {
			currentMove += "-" + bs.getAlgebNot();
			if (((currentMove.equals("e1-g1")) && (amWhite == 1) && (originSquare.index() == 5)) || 
				((currentMove.equals("e8-g8")) && (amWhite == 0) && (originSquare.index() == 11))) {
					currentMove = "0-0";
			} else if (((currentMove.equals("e1-c1")) && (amWhite == 1) && (originSquare.index() == 5)) || 
						((currentMove.equals("e8-c8")) && (amWhite ==0) && (originSquare.index() == 11))) {
							currentMove = "0-0-0";
			}
				
			cont.sendText(currentMove);
		}
		
		int x = convertCoord(originSquare.x());
		int y = convertCoord(originSquare.y());
		NSMutableRect rect = new NSMutableRect(squares[x][y]);
		rect.insetRect(1f,1f);
		
		lockFocus();
		if (isSquareBlack(x, y)) darkColor.set();
		else lightColor.set();
		NSGraphics.frameRectWithWidth(rect, 2f);
		unlockFocus();
		currentMove = "";
		originSquare = null;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		setMyMove():	boolean
	//	Sets whether it is my move or not
	//	-------------------------------------------------------------------------
	public void setMyMove(boolean move, boolean wh) {
		myMove = move;
		whitesMove = wh;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		setFlip():	boolean
	//	Sets whether to flip the board or not
	//	-------------------------------------------------------------------------
	public void setFlip(boolean flp) {
		flip = flp;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		convertCoord():	int
	//	Converts the board array index to flipped view, or returns the same index
	//	-------------------------------------------------------------------------
	public int convertCoord(int coord) {
		if (flip) return (7 - coord);
		else return coord;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		isMyPiece():	int
	//	returns whether the user has clicked on their own piece or not
	//	-------------------------------------------------------------------------
	public boolean isMyPiece(int index) {
		boolean myPiece = ((((index >= 0) && (index <= 5)) && (amWhite == 1)) ||
			(((index >= 6) && (index <= 11)) && (amWhite == 0))) ? true: false;
		if (amWhite == 2) myPiece = true;
		return myPiece;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		setWhite():	boolean
	//	Sets whether I am playing white or not
	//	-------------------------------------------------------------------------
	public void setWhite(int wh) {
		amWhite = wh;
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
	
	//	-------------------------------------------------------------------------
	//	method:		acceptsFirstMouse():	NSEvent
	//	-------------------------------------------------------------------------
	public boolean acceptsFirstMouse(NSEvent event) {
		return false;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		drawFinal():	String,String,String,String
	//	-------------------------------------------------------------------------
	public void drawFinal(String first, String middle, String next, String last) {
		NSTextField text = new NSTextField(new NSRect(0f,(squareSize*3f) + 2f,squareSize*8f,squareSize*2f));
		NSFont theFont = NSFont.fontWithNameAndSize("Helvetica", (squareSize/12f)*5f);
		text.setFont(theFont);
		NSColor white = NSColor.colorWithDeviceRGB(1f,0f,0f,0.65f);
		text.setTextColor(white);
		text.setEditable(false);
		text.setSelectable(false);
		text.setDrawsBackground(false);
		text.setBordered(false);
		text.setBezeled(false);
		text.setAlignment(NSText.CenterTextAlignment);
		text.setStringValue(first + "\n" + middle + "\n" + next + "\n" + last);
		addSubview(text);
		setNeedsDisplay(true);
	}

}
