//  MyClientThread.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 26 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	30130
//	This class is the outputs the streams from the server. Right now it parses the text
//	within this class. I am thinking about implementing a separate thread to parse board
//	information. This thread is created in the MyTerminalWindowController class.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import com.stevesoft.pat.*;

public class MyClientThread extends Thread {
	private	Reader from_server;
	private MyTerminalView view;
	private boolean stop = false;
	
	//	The Regular Expressions
	private Regex RENewObserve;
	private Regex RENewGame;
	private Regex RELogin;
	private Regex REEndGame;
	private Regex RERemove1;
	private Regex RERemove2;
	private Regex REBoardDump;
	private Regex REAllBoards;
	private Regex REDoubleNewline;
	private Regex RESeek;
	private Regex REUserIsPresent;
	private Regex REUserHasArrived;
	private Regex REUserHasLeft;
	private Regex REExamine;
	
	private MyBoardController control;
	private MyMainWindowController myConnect;
	private MySeekView seek;
	private boolean GameInProgress = false;
	private boolean ObsInProgress = false;
	private boolean loginFlag = false;
	private boolean isSeeking = true;
	static final boolean observe = false;
	static final boolean play = true;
	
	private long timeout;
	
	
	//	-------------------------------------------------------------------------
	//	Constructor():	Reader, MyTerminalView
	//	Creates the Thread, and sets the view where text is displayed
	//	-------------------------------------------------------------------------
	public MyClientThread(Reader in_server, MyTerminalView in_view) {
		from_server = in_server;
		view = in_view;
		myConnect = ((MyMainWindowController)NSApplication.sharedApplication().delegate());
		control = myConnect.returnBoardController();
		seek = myConnect.returnSeekView();
		NSArray reArray = MyRegularExpressions.sharedInstance().getRExpressions(control.returnServerType());

		RENewObserve = (Regex)reArray.objectAtIndex(0);
		RENewGame = (Regex)reArray.objectAtIndex(1);
		RELogin = (Regex)reArray.objectAtIndex(2);
		REEndGame = (Regex)reArray.objectAtIndex(3);
		RERemove1 = (Regex)reArray.objectAtIndex(4);
		RERemove2 = (Regex)reArray.objectAtIndex(5);
		REBoardDump = (Regex)reArray.objectAtIndex(6);
		REAllBoards = (Regex)reArray.objectAtIndex(7);
		REDoubleNewline = (Regex)reArray.objectAtIndex(8);
		RESeek = (Regex)reArray.objectAtIndex(9);
		REUserIsPresent = (Regex)reArray.objectAtIndex(10);
		REUserHasArrived = (Regex)reArray.objectAtIndex(11);
		REUserHasLeft = (Regex)reArray.objectAtIndex(12);
		REExamine = (Regex)reArray.objectAtIndex(13);

		timeout = (MyPreferences.intValueForKey(MyPreferences.serverTime) + 1L) * 15000L;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		run():	no parameters
	//	Starts the thread running, until the server closes it. Also parses the
	//	text for pertinent board information
	//	-------------------------------------------------------------------------
	public void run() {
		int myPool = NSAutoreleasePool.push();
		
		char[]	buffer = new char[1024];
		int 	chars_read;
		String	theText = "";
		long start = System.currentTimeMillis();
		try {
			while (((chars_read = from_server.read(buffer)) != -1) && (!stop)) {
				for (int i = 0; i < chars_read; i++) {
					if (buffer[i] != '\n') {
						theText += buffer[i];
					} 
				}
				
				//	If it is time to login, send login notification
				if (!loginFlag) {
					if (System.currentTimeMillis() < (start + timeout)) {
						RELogin.search(theText);
						if (RELogin.stringMatched() != null) {
							loginFlag = true;
							myConnect.sendLogin();
						}
					} else timeout();
				}
	
				//	If a new observation, add a new board to the list
				RENewObserve.search(theText);
				while (RENewObserve.stringMatched() != null) {
					control.addBoard(observe, RENewObserve.stringMatched());
					theText = RENewObserve.replaceFirst(theText);
					RENewObserve.search(theText);
				}
				
				//	If a new examine
				REExamine.search(theText);
				while (REExamine.stringMatched() != null) {
					control.addBoard(play, REExamine.stringMatched());
					theText = REExamine.replaceFirst(theText);
					REExamine.search(theText);
				}
				
				//	If a new game, add a new board to the list and make it playable
				RENewGame.search(theText);
				if (RENewGame.stringMatched() != null) {
					control.addBoard(play, RENewGame.stringMatched());
				}
				
				//	If there are boards open, route from server
				if (control.haveBoardsOpen()) {
					REBoardDump.search(theText);
					while (REBoardDump.stringMatched() != null) {
						control.routeBoard(REBoardDump.stringMatched());
						theText = REBoardDump.replaceFirst(theText);
						REBoardDump.search(theText);
					}
				}
				//	If a game is over, send message to remove it
				REEndGame.search(theText);
				while ( REEndGame.stringMatched() != null) {
					control.removeWindow(REEndGame.stringMatched(1), REEndGame.stringMatched());
					theText = REEndGame.replaceFirst(theText);
					REEndGame.search(theText);
				}
								
				//	If the end of an observation, remove the game
				RERemove1.search(theText);
				while  (RERemove1.stringMatched() != null ) {
					control.removeWindow(RERemove1.stringMatched(1), RERemove1.stringMatched());
					theText = RERemove1.replaceFirst(theText);
					RERemove1.search(theText);
				}
				RERemove2.search(theText);
				while  (RERemove2.stringMatched() != null ) {
					control.removeWindow(RERemove2.stringMatched(1), RERemove2.stringMatched());
					theText = RERemove2.replaceFirst(theText);
					RERemove2.search(theText);
				}
				
				//	Get the seek text and add seeks to the seek view			
				if (isSeeking) {
					RESeek.search(theText);
					while (RESeek.stringMatched() != null) {
						seek.addSeek( RESeek.stringMatched() );
						theText = RESeek.replaceFirst(theText);
						RESeek.search(theText);
					}
				}
				
				if (myConnect.hasBuddies()) {
					REUserIsPresent.search(theText);
					while (REUserIsPresent.stringMatched() != null) {
						myConnect.highlightBuddy(REUserIsPresent.stringMatched(1), true);
						theText = REUserIsPresent.replaceFirst(theText);
						REUserIsPresent.search(theText);
					}
					REUserHasArrived.search(theText);
					while (REUserHasArrived.stringMatched() != null) {
						myConnect.highlightBuddy(REUserHasArrived.stringMatched(1), true);
						theText = REUserHasArrived.replaceFirst(theText);
						REUserHasArrived.search(theText);
					}
					REUserHasLeft.search(theText);
					while (REUserHasLeft.stringMatched() != null) {
						myConnect.highlightBuddy(REUserHasLeft.stringMatched(1), false);
						theText = REUserHasLeft.replaceFirst(theText);
						REUserHasLeft.search(theText);
					}
				}
				
				//	Print what is left of the text
				theText = stripBoardInfo(theText);
				
				view.setEditable(true);
				NSRange theRange = new NSRange(view.string().length(), 0);
				view.replaceCharactersInRange(theRange,theText);
				theRange = new NSRange(view.string().length(), 0);
				view.scrollRangeToVisible(theRange);
				view.setEditable(false);
				theText = "";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		NSAutoreleasePool.pop(myPool);
		myConnect.disconnect();
	}
		
	//	-------------------------------------------------------------------------
	//	method:		stripBoardInfo():	String
	//	returns text without the board information
	//	-------------------------------------------------------------------------
	public String stripBoardInfo(String fullDump) {
		String temp = "";
		//temp = REAllBoards.replaceAll(fullDump);
		//temp = RESeek.replaceAll(temp);
		temp = REDoubleNewline.replaceAll(fullDump);
		return temp;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		setStop():	
	//	stops the thread
	//	-------------------------------------------------------------------------
	public void setStop() {
		stop = true;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		timeout():	
	//	called if connected but server is not responding
	//	-------------------------------------------------------------------------
	public void timeout() {
		setStop();
		view.setEditable(true);
		NSRange theRange = new NSRange(view.string().length(), 0);
		view.replaceCharactersInRange(theRange,"The Connection Attempt has timed out. Check your settings.");
		theRange = new NSRange(view.string().length(), 0);
		view.scrollRangeToVisible(theRange);
		view.setEditable(false);
	}
	
	//	-------------------------------------------------------------------------
	//	method:		setSeek():	boolean
	//	sets whether to record seeks or not
	//	-------------------------------------------------------------------------
	public void setSeek(boolean inSeeking) {
		isSeeking = inSeeking;
	}
}
