//	============================================================================================
//  MyCommandQueue.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Thu Feb 06 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//
//	This class handles the moves sent to the console. It runs on its own thread to speed up
//	the terminal output, and also makes the board routing more responsive.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.lang.*;
import com.stevesoft.pat.*;

public class MyCommandQueue extends Thread {

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
	
	private MyBoardController control;
	private boolean GameInProgress = false;
	private boolean ObsInProgress = false;
	private boolean loginFlag = false;
	static final boolean observe = false;
	static final boolean play = true;
	
	private MyQueue moves;
	private boolean popping = false;
	private boolean stop = false;
	
	public MyCommandQueue(int index, MyBoardController inControl) {
		control = inControl;
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
		
		moves = new MyQueue();
	}
	
	public synchronized void queue(String newText) {
		while (popping) {
			try {
				wait();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		moves.enqueue(newText);
		notify();
	}
	
	public synchronized String pop() {
		popping = true;
		String obj = null;
		if (moves.hasElements()) {
			obj = moves.dequeue();
		}
		popping = false;
		notify();
		return obj;
	}
	
	public void run() {
		int myPool = NSAutoreleasePool.push();
		
		while (!stop) {
			String theText = pop();
			if (theText != null) {
			
				//	If it is time to login, send login notification
				if (!loginFlag) {
					RELogin.search(theText);
					if (RELogin.stringMatched() != null) {
						loginFlag = true;
						NSDistributedNotificationCenter.defaultCenter().postNotification("SendLogin",null);
					}
				}
	
				//	If there are boards open, route from server
				if (control.haveBoardsOpen()) {
					REBoardDump.search(theText);
					while (REBoardDump.stringMatched() != null) {
						control.routeBoard(REBoardDump.stringMatched());
						REBoardDump.search(REBoardDump.right());
					}
				}
				
				//	If a new observation, add a new board to the list
				RENewObserve.search(theText);
				while (RENewObserve.stringMatched() != null) {
					control.addBoard(observe, RENewObserve.stringMatched());
					RENewObserve.search(RENewObserve.right());
				}
				
				//	If a new game, add a new board to the list and make it playable
				RENewGame.search(theText);
				if (RENewGame.stringMatched() != null) {
					control.addBoard(play, RENewGame.stringMatched());
				}
				
				//	If a game is over, send message to remove it
				REEndGame.search(theText);
				while ( REEndGame.stringMatched() != null) {
					control.removeWindow(REEndGame.stringMatched(1), REEndGame.stringMatched());
					REEndGame.search(REEndGame.right());
				}
				
				//	If the end of an observation, remove the game
				RERemove1.search(theText);
				while  (RERemove1.stringMatched() != null ) {
					control.removeWindow(RERemove1.stringMatched(1), RERemove1.stringMatched());
					RERemove1.search(RERemove1.right());
				}
				RERemove2.search(theText);
				while  (RERemove2.stringMatched() != null ) {
					control.removeWindow(RERemove2.stringMatched(1), RERemove2.stringMatched());
					RERemove2.search(RERemove2.right());
				}
			}
		}	
		NSAutoreleasePool.pop(myPool);
	}
	
	public void setStop() {
		stop = true;
	}
}
