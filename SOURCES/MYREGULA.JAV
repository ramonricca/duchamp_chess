//	============================================================================================
//  MyRegularExpressions.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Thu Feb 06 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//	This class compiles and returns the regular expressions according to the type of
//	server specified, e.g. FICS, ICS, AICS
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import com.stevesoft.pat.*;

public class MyRegularExpressions {

	//	The Regular Expressions
	private Regex RENewObserve;
	private Regex RENewGame;
	private Regex RELogin;
	private Regex REEndGame;
	private Regex RERemove1;
	private Regex RERemove2;
	private Regex REBoardDump;
	private Regex REAllBoards;
	private Regex RESeek;
	private Regex REDoubleNewline;
	private Regex REExamine;
	
	//	Regular Expressions used for buddy list
	private Regex REUserIsPresent;
	private Regex REUserHasArrived;
	private Regex REUserHasLeft;
	
	//	Board Regex
	private Regex REInterpret;
	private Regex RENames;
	
	private static MyRegularExpressions sharedInstance;
	
	public MyRegularExpressions() {
		MyRegularExpressions.sharedInstance = this;
		//					 Dump   Whose                     Game#  		                    WScore BScore WTime  BTime  Move#			 Note
		REInterpret = new Regex("<12> (.{72})([WB]) .{1,2} . . . . \\d+ (\\d+) \\S+ \\S+ .{1,2} \\d+ \\d+ (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) [\\S]+ .+ (.+) [01](?: \\d+)?", ReplaceRule.perlCode(""));

		//					                        	   	 Game#  WName	BName                   WScore BScore WTime  BTime  Move#			 Note
		RENames = new Regex("<12> .{72}[WB] .{1,2} . . . . \\d+ (\\d+) (\\S+) (\\S+) (.{1,2}) \\d+ \\d+ (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) [\\S]+ .+ (.+) [01](?: \\d+)?");
	}
	
	public NSArray getRExpressions(int index) {
		if (index == 0 ) {
			RENewObserve = new Regex("You are now observing game (\\d+).\\r*.+\\s[01](?: \\d+)?\\r", ReplaceRule.perlCode(""));
			RENewGame = new Regex("{Game (\\d+).*Creating.*}.*\\r*.+[01](?: \\d+)?\\r", ReplaceRule.perlCode(""));
			RELogin = new Regex("login:");
			REEndGame = new Regex("{Game (\\d+) ([^\\r]*)} (\\d/?\\d?-\\d/?\\d?)\\r", ReplaceRule.perlCode(""));
			RERemove1 = new Regex("Removing game (\\d+) from observation list.\\r", ReplaceRule.perlCode(""));
			RERemove2 = new Regex("You are no longer observing game (\\d+).\\r", ReplaceRule.perlCode(""));
			REBoardDump = new Regex("<12>[^\\r]+[01](?: \\d+)?\\r", ReplaceRule.perlCode(""));
			REAllBoards = new Regex("<12>[^\\r]+[01](?: \\d+)?\\r", ReplaceRule.perlCode(""));
			REDoubleNewline = new Regex("\\r\\r+", ReplaceRule.perlCode("\\r"));
			RESeek = new Regex("(\\w+)(?:\\(.*\\))? \\((.+)\\) seeking (\\d+) (\\d+) (\\w+) (\\w+).*\"(.+)\".*\\)\\r", ReplaceRule.perlCode(""));
			REUserIsPresent = new Regex("Present company includes: ([^\\r]+)\\r", ReplaceRule.perlCode(""));
			REUserHasArrived = new Regex("Notification: (.+) has arrived\\r", ReplaceRule.perlCode(""));
			REUserHasLeft = new Regex("Notification: (.+) has departed\\r", ReplaceRule.perlCode(""));
			REExamine = new Regex("Starting.+examine.+\\r+.+\\s[01](?: \\d+)?\\r", ReplaceRule.perlCode(""));
		} else if (index == 1) {
			RENewObserve = new Regex("You are now observing game (\\d+).\\r\\r.+\\s[01]\\r", ReplaceRule.perlCode(""));
			RENewGame = new Regex("{Game (\\d+).*Creating.*}.*\\r+.+\\s[01]\\r", ReplaceRule.perlCode(""));
			RELogin = new Regex("login:");
			REEndGame = new Regex("{Game (\\d+) ([^\\r]*)} (\\d/?\\d?-\\d/?\\d?)\\r", ReplaceRule.perlCode(""));
			RERemove1 = new Regex("Removing game (\\d+) from observation list.\\r", ReplaceRule.perlCode(""));
			RERemove2 = new Regex("You are no longer observing game (\\d+).\\r", ReplaceRule.perlCode(""));
			REBoardDump = new Regex("<12>[^\\r]+[01]\\r", ReplaceRule.perlCode(""));
			REAllBoards = new Regex("<12>[^\\r]+[01]\\r", ReplaceRule.perlCode(""));
			REDoubleNewline = new Regex("\\r\\r+", ReplaceRule.perlCode("\\r"));
			RESeek = new Regex("(\\w+)(?:\\(.*\\))? \\((.+)\\) seeking (.+) (\\d+) (\\d+) (\\w+).*\"(.+)\".*\\)\\r", ReplaceRule.perlCode(""));
			REUserIsPresent = new Regex("Present company includes: ([^\\r]+)\\r", ReplaceRule.perlCode(""));
			REUserHasArrived = new Regex("Notification: (.+) has arrived\\r", ReplaceRule.perlCode(""));
			REUserHasLeft = new Regex("Notification: (.+) has departed\\r", ReplaceRule.perlCode(""));
			REExamine = new Regex("Starting.+examine.+\\r+.+\\s[01]\\r", ReplaceRule.perlCode(""));
		} else return null;
	
		return new NSArray(new Regex[] {
									RENewObserve,
									RENewGame,
									RELogin,
									REEndGame,
									RERemove1,
									RERemove2,
									REBoardDump,
									REAllBoards,
									REDoubleNewline,
									RESeek,
									REUserIsPresent,
									REUserHasArrived,
									REUserHasLeft,
									REExamine	} );
	}
	
	public Regex getInterpretRE() {
/* 		Regex temp = null; */
/* 		//					 Dump   Whose                     Game#  		                    WScore BScore WTime  BTime  Move#			 Note */
/* 		temp = new Regex("<12> (.{72})([WB]) .{1,2} . . . . \\d+ (\\d+) \\S+ \\S+ .{1,2} \\d+ \\d+ (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) [\\S]+ .+ (.+) [01](?: \\d+)?", ReplaceRule.perlCode("")); */
		return REInterpret;
	}
	
	public Regex getNames() {
/* 		Regex temp = null; */
/* 		//					                        	   	 Game#  WName	BName                   WScore BScore WTime  BTime  Move#			 Note */
/* 		temp = new Regex("<12> .{72}[WB] .{1,2} . . . . \\d+ (\\d+) (\\S+) (\\S+) (.{1,2}) \\d+ \\d+ (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) [\\S]+ .+ (.+) [01](?: \\d+)?"); */
		return RENames;
	}
	
	public Regex getSpaces() {
		return new Regex(" ", ReplaceRule.perlCode(""));
	}
	
	public static MyRegularExpressions sharedInstance() {
        if (sharedInstance == null) {
            new MyRegularExpressions();
        }
        
        return sharedInstance;
       }
       
    public Regex getBoards() {
    	return REAllBoards;
    }
    
    public Regex getNewlines() {
    	return REDoubleNewline;
    }
    
    public Regex getSeek() {
    	return RESeek;
    }

}
