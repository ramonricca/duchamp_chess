//	============================================================================================
//  MyBoardController.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Tue Jan 07 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//
//	This class adds, removes, and maintains the array of boards that are open.
//	It also iterates through the boards to route the correct board dump to the board window
//	MyBoardController also acts as the delegate to the board window, so when a close is
//	requested by clicking on the close button, it acts accordingly to remove an observation.
//	If the game is a playing game, then it asks if the user wants to resign.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import com.stevesoft.pat.*;

public class MyBoardController {
	private NSMutableArray theBoards;		//	Array of the NSBoardWindow instances of each game
	private static Regex REBoard;			//	Regular Expression of the interpretation RE
	private String username;				//	Username for person logged in
	private int serverType;					//	FICS or ICC
	
	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Creates the empty board array and sets username variable used by
	//	board windows to determine whether user is play white or black
	//	-------------------------------------------------------------------------
	public MyBoardController(String user, int index) {
		theBoards = new NSMutableArray();
		REBoard = MyRegularExpressions.sharedInstance().getInterpretRE();

		username = user;
		serverType = index;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		addBoard():		boolean, String
	//	boolean playing board or observation, first dump of board
	//	constructs a board window and attaches the controller to the board.
	//	adds the new board to the array of boards
	//	-------------------------------------------------------------------------
	public void addBoard(boolean typeOfBoard, String firstDump) {
		//	Add a new board window
		theBoards.addObject(new MyBoardWindow(typeOfBoard, firstDump, username));
		//	Load the Game Window nib
		NSApplication.loadNibNamed("Board", (MyBoardWindow)theBoards.lastObject());
		((MyBoardWindow)theBoards.lastObject()).window.setFrameAutosaveName("Duchamp.Board" + String.valueOf(theBoards.count()));
		((MyBoardWindow)theBoards.lastObject()).window.makeKeyAndOrderFront(this);
		((MyBoardWindow)theBoards.lastObject()).window.setDelegate(this);
		((MyBoardWindow)theBoards.lastObject()).window.setInputLine(
			((MyMainWindowController)NSApplication.sharedApplication().delegate()).returnInputLine());
		
		//	Initialize the new board
		((MyBoardWindow)theBoards.lastObject()).catchUp();
	}
	
	//	-------------------------------------------------------------------------
	//	method:		haveBoardsOpen():	no parameters
	//	returns true if there are boards open
	//	-------------------------------------------------------------------------
	public boolean haveBoardsOpen() {
		if (theBoards.count() > 0) return true;
		else return false;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		routeBoard():	String
	//	iterates through the board game numbers until it finds a match, then
	//	tells the corresponding board to route the dump
	//	-------------------------------------------------------------------------
	public void routeBoard(String theDump) {
		REBoard.search(theDump);
		if (REBoard.stringMatched() != null) {
			String rawGame = REBoard.stringMatched(3);
			for (int i=0; i < theBoards.count(); i++) {
				if ((((MyBoardWindow)theBoards.objectAtIndex(i)).getGameNumber()).equals(rawGame)) {
					((MyBoardWindow)theBoards.objectAtIndex(i)).interpretDump(REBoard.stringMatched());
				}
			}
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		removeWindow():	String, String
	//	the game number, final words are passed in. Final words are displayed in
	//	sheet. Iterates through the board game numbers until it finds one to 
	//	remove.
	//	-------------------------------------------------------------------------
	public void removeWindow(String theGameToRemove, String finalWords) {
		int invalidIndex = -1;
		String num;
		for (int i=0; i < theBoards.count(); i++) {
			num = ((MyBoardWindow)theBoards.objectAtIndex(i)).getGameNumber();
			if (num.equals(theGameToRemove)) invalidIndex = i;
		}
		
		if (invalidIndex != -1) {
			((MyBoardWindow)theBoards.objectAtIndex(invalidIndex)).window.setGameOver(true);
			if (finalWords != null) {
				((MyBoardWindow)theBoards.objectAtIndex(invalidIndex)).stopTime();
				((MyBoardWindow)theBoards.objectAtIndex(invalidIndex)).endSession(finalWords);
			}
			theBoards.removeObjectAtIndex(invalidIndex);
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		getBoardDump():		String
	//	Uses regular expressions to get the board dump
	//	returns the match
	//	-------------------------------------------------------------------------
	public static String getBoardDump(String fullDump) {
		REBoard.search(fullDump);
		return REBoard.stringMatched();
	}
	
	//	-------------------------------------------------------------------------
	//	method:		stripBoardInfo():	Uses regular expressions to remove the 
	//	board info returns the text without the board dump
	//	-------------------------------------------------------------------------
	public static String stripBoardInfo(String fullDump) {
		String temp = "";
		try {
			temp = REBoard.replaceAll(fullDump);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		windowShouldClose():	Object
	//	delegate method of MyWindow
	//	Removes the window object from the array of boards. Sends a final string
	//	to show that it has removed the window.
	//	-------------------------------------------------------------------------
	public boolean windowShouldClose(Object sender) {
		if (!((MyWindow)sender).playing)  {
			((MyWindow)sender).closeMe = true;
			((MyWindow)sender).close();
			((MyMainWindowController)NSApplication.sharedApplication().delegate()).sendText("unobserve " + ((MyWindow)sender).number);
		} else if (!((MyWindow)sender).gameOver) {
			NSAlertPanel.beginAlertSheet("Do you want to resign?","NO!","Yes",null,((MyWindow)sender),this,
				new NSSelector ("sheetDidEnd", new Class[] {NSWindow.class, int.class, Object.class}),
				null, ((MyWindow)sender), "You are in the middle of a Game. Do you want to resign?");
		} else ((MyWindow)sender).close();
		return false;
	} 

	//	-------------------------------------------------------------------------
	//	method:		sheetDidEnd():	NSWindow, int, Object
	//	method executed to see if the user wants to resign
	//	Removes the window object from the array of boards. Sends a final string
	//	to show that it has removed the window.
	//	-------------------------------------------------------------------------
	public void sheetDidEnd(NSWindow sheet, int returnCode, Object contextInfo) {
		if (returnCode == NSAlertPanel.AlternateReturn) {
			removeWindow(((MyWindow)contextInfo).number,null);
			((MyWindow)contextInfo).close();
			((MyMainWindowController)NSApplication.sharedApplication().delegate()).sendText("resign");
		} 
	}
	
	//	-------------------------------------------------------------------------
	//	method:		returnServerType():	no parameters
	//	-------------------------------------------------------------------------
	public int returnServerType() {
		return serverType;
	}
	
}
