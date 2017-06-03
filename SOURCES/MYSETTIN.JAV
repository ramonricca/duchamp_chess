//	============================================================================================
//  MySettingIO.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Fri Jan 31 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//
//	This class is the input output class of the connection settings. It handles filing and 
//	conversion of XML data to the MyMainWindowController class.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.io.*;
import java.net.*;

public class MySettingIO {
	public static final String connectionsPath = "~/Library/Preferences/Duchamp.connections.plist";
	public static final String buddyPath = "~/Library/Preferences/Duchamp.buddies.plist";
	public static final String seekPath = "~/Library/Preferences/Duchamp.seeks.plist";
	
	private NSMutableArray connections = null;
	private NSMutableArray buddies = null;
	private NSMutableArray seeks = null;
	private NSData rawdata = null;
	private NSArray list;
	
	private boolean readFromFile = false;

	public static final String serverPLIST = "server";
	public static final String portPLIST = "port";
	public static final String usernamePLIST = "username";
	public static final String passwordPLIST = "password";
	public static final String settingPLIST = "setting";
	public static final String typePLIST = "type";
	public static final String lagPLIST = "lag";
	
	public static final String[] PLIST= {	serverPLIST,
											portPLIST,
											usernamePLIST,
											passwordPLIST,
											settingPLIST, 
											typePLIST,
											lagPLIST	};
	
	public boolean haveConnections = false;
	public boolean haveSeeks = false;
	public boolean haveBuddies = false;
	
	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Default constructor.
	//	-------------------------------------------------------------------------
	public MySettingIO() {
		//	Get the Settings Dictionary
		String fullpath = NSPathUtilities.stringByExpandingTildeInPath(connectionsPath);
		File file = new File(fullpath);
		if (file.exists()) {
			connections = new NSMutableArray((NSArray)NSUnarchiver.unarchiveObjectWithFile(fullpath));
			haveConnections = true;
			readFromFile = true;
		}
		
		fullpath = NSPathUtilities.stringByExpandingTildeInPath(seekPath);
		file = new File(fullpath);
		if (file.exists()) {
			seeks = new NSMutableArray((NSArray)NSUnarchiver.unarchiveObjectWithFile(fullpath));
			haveSeeks = true;
		}
		
		fullpath = NSPathUtilities.stringByExpandingTildeInPath(buddyPath);
		file = new File(fullpath);
		if (file.exists()) {
			buddies = new NSMutableArray((NSArray)NSUnarchiver.unarchiveObjectWithFile(fullpath));
			haveBuddies = true;
		}

	}
	
	//	-------------------------------------------------------------------------
	//	writeSettings():	NSArray
	//	writes the settings to disk
	//	-------------------------------------------------------------------------
	public static void writeSettings(NSArray arr, String path) {
		String fullpath = NSPathUtilities.stringByExpandingTildeInPath(path);
		NSArchiver.archiveRootObjectToFile(arr, fullpath);
	}
	
	//	-------------------------------------------------------------------------
	//	connections():	NSArray
	//	returns the array of dictionaries
	//	-------------------------------------------------------------------------
	public NSArray connections() {
		return connections;
	}
	
	//	-------------------------------------------------------------------------
	//	settings():	NSArray
	//	returns the array of dictionaries
	//	-------------------------------------------------------------------------
	public NSArray seeks() {
		return seeks;
	}
	//	-------------------------------------------------------------------------
	//	settings():	NSArray
	//	returns the array of dictionaries
	//	-------------------------------------------------------------------------
	public NSArray buddies() {
		return buddies;
	}
	//	-------------------------------------------------------------------------
	//	hasSavedSettings():	no parameters
	//	returns whether there were any entries that were read in
	//	-------------------------------------------------------------------------
	public boolean hasSavedSettings() {
		return readFromFile;
	}
	
}
