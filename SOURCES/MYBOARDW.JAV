//	============================================================================================
//  MyBoardWindow.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 14 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class defines the Board window. It interprets the dump sent to it to pass along to
//	the board view to draw. It handles the highest level of board interpretation, including
//	setting the game number, who is playing white, whose turn it is, etc. It defines all 
//	the objects that comprise the board window; instances of the board view, the board header,
//	and the window itself. It has end of game sheets.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import com.stevesoft.pat.*;

public class MyBoardWindow {
    public MyBoardView board; 			/* IBOutlet */
    public MyWindow window; 			/* IBOutlet */
    public MyBoardHeaderView header; 	/* IBOutlet */
    
    private boolean playingGame;
    private boolean isInited = false;
    private boolean IAmWhite;
    private boolean player1isWhite;
    private boolean examine;
    
    private String initialDump;
    private String playerName1;
    private String playerName2;
    private String gameNumber;
    private String whoIsWhite;
    private String whoseMove;
    private String myName;
    
    private Regex extract;
    private Regex spaces;
        	
	//	-------------------------------------------------------------------------
	//	Constructor():	boolean, String, String
	//	Creates the MyBoardWindow instance, designating if it is a playing game
	//	or observation window. Also has the first dump of the board (with
	//	a game in progress or not) and also has the username to compare with the
	//	board dump to decide which color you are playing
	//	-------------------------------------------------------------------------
	public MyBoardWindow(boolean isPlayingGame, String dump, String user) {
		playingGame = isPlayingGame;
		myName = user;
		spaces = MyRegularExpressions.sharedInstance().getSpaces();
		extract = MyRegularExpressions.sharedInstance().getInterpretRE();
		initialDump = dump;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		interpretDump():	String
	//	Takes the input and interprets the time settings, whose move it is,
	//	toggles the headerview, and tells the boardview to redraw either in
	//	entirety or updates
	//	-------------------------------------------------------------------------
	public void interpretDump(String dump) {
		extract.search(dump);
		
		Integer ptime1 = null;
		Integer ptime2 = null;

		//	try to interpret the text into Integer instances
		try {
			ptime1 = new Integer(extract.stringMatched(6));
			ptime2 = new Integer(extract.stringMatched(7));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//	get whose move, and set the board to know whether it is my turn or not
		whoseMove = extract.stringMatched(2);
		boolean white = (whoseMove.equals("W")) ? true : false;
		boolean mm = ((((white) && (IAmWhite)) || ((!white) && (!IAmWhite))) || (examine)) ? true : false;
				
		
		//	toggle whose move it is in the header, and set the board view to know
		//	whether it is my turn or not
		header.toggle(ptime1, ptime2, white);
		board.setMyMove(mm, white);
		
		//	set the board dump to the view, and either update cells or the entire board
		board.setDump(spaces.replaceAll(extract.stringMatched(1)));
		if (!initialized()) {
			board.display();
			isInited = true;
		} else {
			board.update();
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		playing():	no arguments
	//	returns whether this is a playing game or and observation
	//	-------------------------------------------------------------------------
	public boolean playing() {
		return playingGame;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		catchUp():	no arguments
	//	Called after creating the object to initialize the static fields (that is
	//	the players names, window title, whether the board should be flipped
	//	-------------------------------------------------------------------------
	public void catchUp() {
		board.setPlaying(playingGame);
		String boardinfo = initialDump;
		
		Regex names = MyRegularExpressions.sharedInstance().getNames();
		names.search(boardinfo);
		gameNumber = names.stringMatched(1);
		
		playerName1 = names.stringMatched(2);
		playerName2 = names.stringMatched(3);
		
		window.setTitle("Game " + gameNumber + " " + playerName1 + " vs. " + playerName2);
		window.setGameNumber(gameNumber, playingGame);

		if (playingGame) {
			if (!((names.stringMatched(4)).equals("2"))) {
				IAmWhite = ((names.stringMatched(4)).equals("1")) ? true : false;
				int wh = (IAmWhite) ? 1 : 0;
				boolean flip = (IAmWhite) ? false : true;
				board.setWhite(wh);
				board.setFlip(flip);
			} else {
				examine = true;
				board.setWhite(2);
			}
		}
		
		header.initialize(playerName1, playerName2);

		interpretDump(boardinfo);
	}
	
	//	-------------------------------------------------------------------------
	//	method:		initialized():	no arguments
	//	returns whether the board window has been initialized
	//	-------------------------------------------------------------------------
	public boolean initialized() {
		return isInited;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		getGameNumber():	no arguments
	//	returns the game number of the board window. Used for routing, etc...
	//	-------------------------------------------------------------------------
	public String getGameNumber() {
		return gameNumber;
	}
			
	//	-------------------------------------------------------------------------
	//	method:		stripSpaces():	String
	//	returns text returned with the spaces removed from the end
	//	-------------------------------------------------------------------------
	public String stripSpaces(String inName) {
		int space = inName.indexOf(" ");
		if (space != -1) 
			return inName.substring(0, space);
		else return inName;
	}

	//	-------------------------------------------------------------------------
	//	method:		endSession():	String
	//	Creates an alert sheet with the String passed to it. Executes the 
	//	selector "cleanUp" after the user has clicked "OK".
	//	-------------------------------------------------------------------------
	public void endSession(String finalWords) {
		Regex end = new Regex("{(Game \\d+) (\\(.+\\)) (.*)} (\\d/?\\d?-\\d/?\\d?)");
		end.search(finalWords);
		if (end.stringMatched() != null) {
			board.drawFinal(end.stringMatched(1), end.stringMatched(2), end.stringMatched(3), end.stringMatched(4));
		}
		
		window.setDelegate(null);
		if (window.closeMe) window.close();
	}
		
	public void stopTime() {
		header.stopTime();
	}
}
