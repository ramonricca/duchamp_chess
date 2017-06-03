//	============================================================================================
//	MyMainWindowController.java
//	Duchamp
//
//	Created by Ramon G. Ricca on Sun Feb 12 2003.
//	Copyright (c) 2003 CLique Software. All rights reserved.
//
//	30213
//	This class is a composite of both the connection controller and the Terminal Window
//	controller in past versions. It controls the main window, which handles the connection
//	and terminal output.
//
//	30114
//	This class is the connection controller. It handles the connection window, and is the 
//	NSApplication delegate. It creates the terminal window, has it's own sendText method that
//	sends text to the input stream.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.io.*;
import java.net.*;

public class MyMainWindowController {

    public NSButton add; /* IBOutlet */
    public NSButton buddyCancel; /* IBOutlet */
    public NSTableView buddyList; /* IBOutlet */
    public NSTextField buddyName; /* IBOutlet */
    public NSButton buddyOK; /* IBOutlet */
    public NSWindow buddySheet; /* IBOutlet */
    public NSButton connect; /* IBOutlet */
    public NSButton cshout; /* IBOutlet */
    public MyInputLine inputLine; /* IBOutlet */
    public NSButton kibitz; /* IBOutlet */
    public NSButton lagButton; /* IBOutlet */
    public NSButton newSeek; /* IBOutlet */
    public NSSecureTextField passwordText; /* IBOutlet */
    public NSTextField port; /* IBOutlet */
    public NSButton remove; /* IBOutlet */
    public NSButton say; /* IBOutlet */
    public NSButton seekAutoOpen; /* IBOutlet */
    public NSButton seekFormula; /* IBOutlet */
    public NSTextField seekInc; /* IBOutlet */
    public NSPopUpButton seekList; /* IBOutlet */
    public NSTextField seekMins; /* IBOutlet */
    public NSButton seekRated; /* IBOutlet */
    public NSTextField seekRatingRange; /* IBOutlet */
    public NSPopUpButton seekType; /* IBOutlet */
    public MySeekView seekView; /* IBOutlet */
    public NSPopUpButton serverSettings; /* IBOutlet */
    public NSTextField serverText; /* IBOutlet */
    public NSMatrix serverType; /* IBOutlet */
    public NSButton setFormula; /* IBOutlet */
    public NSTextField settingText; /* IBOutlet */
    public NSTabView tab; /* IBOutlet */
    public NSButton tell; /* IBOutlet */
    public MyTerminalView terminal; /* IBOutlet */
    public NSTextField usernameText; /* IBOutlet */
    public NSWindow window; /* IBOutlet */
	
	//	These variables are for the instance of a connection	
	private String username;
	private String password;
	private String server;
	private Integer portNumber;

	//	These variables are for the terminal input output	
	private PrintWriter to_server;
	private BufferedReader from_user;
	private Socket s = null;
	private MyClientThread t;
	private Reader from_server;
	private Runtime rt = null;
	private Process pr = null;
	
	//	These variables are instances of my classes
	private boolean isConnected = false;
	private MyBoardController control;
	private MyPreferences prefs;
	private NSToolbar myToolbar;
	
	//	These variables are misc vars
	private int settingIndex;
	private MyConnectionSettings connectionsList;
	private MyBuddyList buddyArray;
	private NSMutableArray seekArray;
	private String currentChat = null;
	
	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Default constructor does nothing
	//	-------------------------------------------------------------------------
	public MyMainWindowController() {
		//	Handle getting the Connection Settings
		MySettingIO io = new MySettingIO();
		
		if (io.haveConnections)
			connectionsList = new MyConnectionSettings(io.connections());
		else connectionsList = new MyConnectionSettings();
		
		if (io.haveBuddies)
			buddyArray = new MyBuddyList(io.buddies());
		else buddyArray = new MyBuddyList();
		
		if (io.haveSeeks)
			seekArray = new NSMutableArray(io.seeks());
		else seekArray = new NSMutableArray();
		
		//	Initialize Preferences instance
		prefs = new MyPreferences();
	}

	//	-------------------------------------------------------------------------
	//	method:		applicationDidFinishLaunching():	NSNotification
	//	NSApplication delegate method that opens a connection window once
	//	the application is fully launched
	//	-------------------------------------------------------------------------
	public void applicationDidFinishLaunching (NSNotification notification) {
		//	Catch up and open the main window
		inputLine.setControl(this);
		setAllPrefs();
		
		//	Initialize Seek List
    	seekList.removeAllItems();
    	seekList.addItem("Recent Seek Settings");
    	seekList.addItemsWithTitles((NSArray)seekArray);
    	
    	//	Initialize Buddy List
    	buddyList.setDataSource(buddyArray);
    	buddyList.setDelegate(this);
    	
		//	Initialize Connection Settings List
		loadMenu();

		openWindow(this);
	}

	//	-------------------------------------------------------------------------
	//	method:		closeWindow():	Object
	//	Closes the connection window. Called by cancel button, and also when
	//	the connection is requested
	//	-------------------------------------------------------------------------
	public void closeWindow(Object sender) { /* IBAction */
		window.close();
	}

	//	-------------------------------------------------------------------------
	//	method:		connect():	Object
	//	Creates the connection to the chess server. Resizes the terminal window
	//	according to the desired columns and widths.
	//	-------------------------------------------------------------------------
	public void connect(Object sender) { /* IBAction */
		//	Get current values for a connection
		server = serverText.stringValue();
		username = usernameText.stringValue();
		password = passwordText.stringValue();
		boolean safe = true;
		
		//	Try to convert the port to an integer
		try {
			portNumber = new Integer(port.stringValue());
		} catch (Exception e) {
			NSAlertPanel.beginAlertSheet("Illegal Port!","OK",null,null,window,this,
				null,null,null,"The specified port is an illegal value. Only the digits 0-9 are allowed.");
			safe = false;
		}

		//	If the port is valid, then try to make a connection
		if (safe) {
			//	If there is a server entry and we are not connected
			if ((server.length() > 0) && (!isConnected) && (username.length() > 0)) {
				//	Initialize the Board Controller
				control = new MyBoardController(username, serverType.selectedRow());
				//	Set Title of main Window and start the connection streams
				window.setTitle("Duchamp: " + username + "@" + server);
				startStreams(serverType.selectedRow(), lagButton.state());
				//terminal.becomeFirstResponder();
			//	If the server entry, username, or already connected, then complain	
			} else {
				NSAlertPanel.beginAlertSheet("Error: Server specifications incorrect.","OK",null,null,window,this,
					null,null,null,"You must specify at least the server name and Username before you can connect!");
				//connectButton.setState(NSCell.OffState);
			}
		} else {
			//connectButton.setState(NSCell.OffState);
		}
	}

	//	-------------------------------------------------------------------------
	//	method:		openWindow():	Object
	//	Makes the connection window visible and highlights the connect button
	//	-------------------------------------------------------------------------
	public void openWindow(Object sender) {
		window.makeKeyAndOrderFront(this);
	}
	
	//	-------------------------------------------------------------------------
	//	method:		sendMacro():	Object
	//	Called by the Macro Menu. Sends the appropriate macro to sendText
	//	-------------------------------------------------------------------------
	public void sendMacro(Object sender) {
		sendText(((NSMenuItem)sender).title());
	}

	//	-------------------------------------------------------------------------
	//	method:		seek():		Object
	//	Called by the newSeek button. Takes any settings and creates a seek
	//	-------------------------------------------------------------------------
	public void seek(Object sender) {
		String seekString;
		if (sender == (Object)seekList) {
			seekString = (seekList.selectedItem()).title();
			seekList.selectItemAtIndex(-1);
		} else {
			seekString = "seek ";
			if (seekMins.stringValue().length() > 0) seekString += seekMins.stringValue() + " ";
			if (seekMins.stringValue().length() > 0) seekString += seekInc.stringValue() + " ";
			if (seekRated.state() == NSCell.OnState) seekString += "rated ";
			if (seekType.indexOfSelectedItem() != 0) seekString += (seekType.selectedItem()).title() + " ";
			if (seekAutoOpen.state() == NSCell.OnState) seekString += "a ";
			else seekString += "m ";
			if (seekFormula.state() == NSCell.OnState) seekString += "f ";
			if (seekRatingRange.stringValue().length() > 0) seekString += seekRatingRange.stringValue();
			addSeekToList(seekString);
		}
		
		sendInput(seekString);
	}
		
	//	-------------------------------------------------------------------------
	//	method:		setFormula():		Object
	//	Called by the setFormula button. Sends a new formula to the server
	//	-------------------------------------------------------------------------
	public void setFormula(Object sender) {
	}

    public void addBuddy(Object sender) { /* IBAction */
    	NSApplication.sharedApplication().beginSheet(buddySheet, window, null, null, null);
    }
    
    public void handleBuddy(Object sender) {
    	if (((sender == (Object)buddyOK) && (buddyName.stringValue().length() > 0)) ||
    		((sender == (Object)buddyName) && (buddyName.stringValue().length() > 0))) {
    			String name = buddyName.stringValue();
    			buddyArray.addName(name);
    			buddyList.reloadData();
    			buddyList.setNeedsDisplay(true);
    			NSApplication.sharedApplication().endSheet(buddySheet);
    			buddySheet.close();
    			sendText("+notify " + name);
    	} else {
    		buddyName.setStringValue("");
    		NSApplication.sharedApplication().endSheet(buddySheet);
    		buddySheet.close();
    	}
	}
	
    public void chat(Object sender) { /* IBAction */
    }

    public void removeBuddy(Object sender) { /* IBAction */
    	int index = buddyList.selectedRow();
    	String name = buddyArray.getNameAtIndex(index);
    	sendText("-notify " + name);
    	buddyArray.removeName(index);
    	buddyList.reloadData();
    }

    public void addSeekToList(String seek) {
     	seekArray = new NSMutableArray();
		if (seekList.itemArray().count() > 0) {
    		NSArray temp = new NSArray(seekList.itemArray());
    		for (int i = 0; i < temp.count(); i++) {
    			seekArray.addObject(((NSMenuItem)temp.objectAtIndex(i)).title());
    		}
    		seekArray.removeObjectAtIndex(0);
    	}
    	
    	if (seekArray.count() < 11) seekArray.addObject(seek);
    	else {
    		seekArray.removeObjectAtIndex(0);
    		seekArray.addObject(seek);
    	}
    	
    	seekList.removeAllItems();
    	seekList.addItem("Recent Seek Settings");
    	seekList.addItemsWithTitles((NSArray)seekArray);
    	seekList.selectItemAtIndex(-1);
    }
    
    public void setAllPrefs() {
    	terminal.setMyPrefs();
		seekView.setMyPrefs();
		inputLine.setMyPrefs();
	}


	//	-------------------------------------------------------------------------
	//	method:		applicationShouldTerminate():	NSApplication
	//	delegate method to clean up a bit before quitting
	//	-------------------------------------------------------------------------
	public boolean applicationShouldTerminate(NSApplication sender) {
		int result;
		if (isConnected) {
			if (control.haveBoardsOpen()) {
				result = NSAlertPanel.runAlert("Quit", "You have boards open. Do you really want to quit?",
					"Cancel", "Disconnect and Quit", null);
			} else {
				result = NSAlertPanel.runAlert( "Quit", "You are still connected to " + server + ". Do you really want to quit?", 
				"Cancel", "Disconnect and Quit", null);
			}
			
			if (result == NSAlertPanel.DefaultReturn) {
				return false;
			}
/* 			if (result == NSAlertPanel.AlternateReturn) { */
/* 				return true; */
/* 			} */
		} 
						
		prefs.saveToDefaults();
		if (connectionsList.count() > 0)
			MySettingIO.writeSettings(connectionsList.returnList(), MySettingIO.connectionsPath);
		if (buddyArray.count() > 0)
			MySettingIO.writeSettings(buddyArray.returnList(), MySettingIO.buddyPath);
		if (seekArray.count() > 0)
			MySettingIO.writeSettings(new NSArray(seekArray), MySettingIO.seekPath);
			
		return true;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		disconnect():	no parameters
	//	Called when the terminal window gets the end of the stream
	//	-------------------------------------------------------------------------
	public void disconnect() {
		tab.selectTabViewItemAtIndex(0);
		isConnected = false;
		if (s != null)
			try {
				s.close();
				s = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		if (rt != null) {
			rt.runFinalization();
			rt = null;
			pr = null;
		}
	}
	
	public MyBoardController returnBoardController() {
		return this.control;
	}
		
	public boolean isConnected() {
		return isConnected;
	}
	
	public MyInputLine returnInputLine() {
		return inputLine;
	}
	
	public MySeekView returnSeekView() {
		return seekView;
	}
	
	public void openPrefs(Object sender) {
		prefs.theWindow.makeKeyAndOrderFront(this);
	}
	
	//	-------------------------------------------------------------------------
	//	method:		startStreams():		no parameters
	//	Gets the streams going and starts the thread that monitors the connection
	//	-------------------------------------------------------------------------
   public void startStreams(int stype, int lag) {
 		try {
			if (lag != NSCell.OnState) {
				s = new Socket(server, port.intValue());
	
				from_server	= new InputStreamReader(s.getInputStream());
				to_server	= new PrintWriter(s.getOutputStream());
			} else if (stype == 0) {
				String pth = NSBundle.mainBundle().pathForResource("timeseal", "");				
				rt = Runtime.getRuntime();
				String cmd = pth + " " + server + " " + portNumber.toString();
				pr = rt.exec(cmd);
				
				from_server = new InputStreamReader(pr.getInputStream());
				to_server = new PrintWriter(pr.getOutputStream());
			} else {
				String pth = NSBundle.mainBundle().pathForResource("timestamp", "");				
				rt = Runtime.getRuntime();
				pr = rt.exec(pth);
				
				from_server = new InputStreamReader(pr.getInputStream());
				to_server = new PrintWriter(pr.getOutputStream());
			}

			t = new MyClientThread(from_server, terminal);
			
			//t.setPriority(t.getPriority() + 1);

			t.start();
			isConnected = true;
			tab.selectTabViewItemAtIndex(1);
		} catch (Exception e) {
			terminal.setEditable(true);
			NSRange theRange = new NSRange(terminal.string().length(), 0);
			terminal.replaceCharactersInRange(theRange,"The Connection Attempt failed. Check your settings.");
			theRange = new NSRange(terminal.string().length(), 0);
			terminal.scrollRangeToVisible(theRange);
			terminal.setEditable(false);
		}
	}
	
//	============================================================================================
//	This code block contains the functions relating to input output for the terminal
//	============================================================================================
	//	-------------------------------------------------------------------------
	//	method:		sendInput():		
	//	Sends the input from the input line and then clears the input line
	//	-------------------------------------------------------------------------
	public void sendInput(String input) { /* IBAction */
		if (isConnected) {
			try {
				to_server.println(input);
				to_server.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
			clearInputLine();
		}
	}
   
	//	-------------------------------------------------------------------------
	//	method:		sendLogin():		no parameters
	//	Gets called by the thread to login automatically.
	//	-------------------------------------------------------------------------
   public void sendLogin() {
		to_server.println(username);
		to_server.println(password);
		to_server.println("set style 12");
		to_server.flush();
		if (MyPreferences.booleanValueForKey(MyPreferences.aFlag)) to_server.println("set autoflag 1");
		if (MyPreferences.booleanValueForKey(MyPreferences.cShouts)) to_server.println("set cshout 1");
		if (MyPreferences.booleanValueForKey(MyPreferences.gTells)) to_server.println("set tell 1");
		if (MyPreferences.booleanValueForKey(MyPreferences.Shouts)) to_server.println("set shout 1");
		to_server.flush();
	}
	
	//	-------------------------------------------------------------------------
	//	method:		clearInputLine():		no parameters
	//	Sets the string of the input line to nothing
	//	-------------------------------------------------------------------------
	public void clearInputLine() {
		inputLine.setString("");
		inputLine.setSelectedRange(new NSRange(0, 0));
	}
		
	//	-------------------------------------------------------------------------
	//	method:		sendText():		String
	//	Another method to send text to the server. Called by the connection
	//	controller, and other classes as well
	//	-------------------------------------------------------------------------
	public void sendText(String text) {
		if (isConnected) {
			to_server.println(text);
			to_server.flush();
		}
	}

//	============================================================================================
//	This code block contains the functions called by the toolbar in the connection drawer.
//	============================================================================================
	//	-------------------------------------------------------------------------
	//	method:		addSetting():	Object
	//	Adds the current settings to the array
	//	-------------------------------------------------------------------------
	public void addSetting() {
		String lag = (lagButton.state() == NSCell.OffState ) ? "0" : "1";
		String type = String.valueOf(serverType.selectedRow());
		Object[] ob =	{	serverText.stringValue(), 
							port.stringValue(), 
							usernameText.stringValue(), 
							passwordText.stringValue(), 
							settingText.stringValue(), 
							type,
							lag
						};
		if (((settingText.stringValue()).length()) != 0) {
			connectionsList.addDict(new NSDictionary(ob, MySettingIO.PLIST));
			settingIndex = connectionsList.count() - 1;
			loadMenu();
		} else {
			NSAlertPanel.beginAlertSheet("No Setting ID!","OK",null,null,window,this,
					null,null,null,"You must specify a settings name to save your current settings.");
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		removeSetting():	Object
	//	Removes the current setting
	//	-------------------------------------------------------------------------
	public void removeSetting() {
		if ((settingIndex) >= 0) connectionsList.removeDict(settingIndex);
		loadMenu();
	}
	
	//	-------------------------------------------------------------------------
	//	method:		settingSelected():	Object
	//	Called when a row is selected in the connections list
	//	Table delegate operation
	//	-------------------------------------------------------------------------
	public void settingSelected(Object sender) {
		int index = serverSettings.indexOfSelectedItem();
		if (index == 1) {
			addSetting();
		} else if (index == 2) {
			removeSetting();
		} else if (index >= 4) {
			NSDictionary dict = connectionsList.getDictAtIndex(index - 4);
			settingIndex = index - 4;
			serverText.setStringValue((String)dict.objectForKey(MySettingIO.serverPLIST)); 
			port.setStringValue((String)dict.objectForKey(MySettingIO.portPLIST));
			usernameText.setStringValue((String)dict.objectForKey(MySettingIO.usernamePLIST)); 
			passwordText.setStringValue((String)dict.objectForKey(MySettingIO.passwordPLIST)); 
			settingText.setStringValue((String)dict.objectForKey(MySettingIO.settingPLIST));
			
			if (((String)dict.objectForKey(MySettingIO.lagPLIST)).equals("0")) 
				lagButton.setState(NSCell.OffState);
			else lagButton.setState(NSCell.OnState);
			
			int radio = (((String)dict.objectForKey(MySettingIO.typePLIST)).equals("0")) ? 0 : 1;
			serverType.selectCellAtLocation(radio, 0);
		}
	}
	
	public void loadMenu() {
		serverSettings.removeAllItems();
		serverSettings.addItem("Saved Settings");
		serverSettings.addItem("Add Setting");
		serverSettings.addItem("Remove Setting");
		serverSettings.addItem("");
		serverSettings.addItemsWithTitles(connectionsList.getAllSettings());
		serverSettings.selectItemAtIndex(-1);
	}	
	
	//	-------------------------------------------------------------------------
	//	method:		tableViewShouldSelectRow():	NSTableView, int
	//	Called when a row is selected in the connections list
	//	-------------------------------------------------------------------------
	public boolean tableViewShouldSelectRow( NSTableView aTableView, int rowIndex) {
		currentChat = buddyArray.getNameAtIndex(rowIndex);
		return true;
	}
	
	public boolean hasBuddies() {
		if (buddyArray.count() > 0) return true;
		else return false;
	}
	
	public void highlightBuddy(String name, boolean red) {
		for (int i = 0; i < buddyArray.count(); i++) {
			if ((buddyArray.getNameAtIndex(i)).equals(name)) {
				NSArray colArray = buddyList.tableColumns();
				NSTableColumn col = (NSTableColumn)colArray.objectAtIndex(0);
				NSCell cell = col.dataCellForRow(i);
				NSMutableDictionary atts = new NSMutableDictionary();
				if (red) {
					atts.setObjectForKey(NSColor.redColor(), NSAttributedString.ForegroundColorAttributeName);
				} else {
					atts.setObjectForKey(NSColor.blackColor(), NSAttributedString.ForegroundColorAttributeName);
				}
				cell.setAttributedStringValue(new NSAttributedString(cell.stringValue(), (NSDictionary)atts));
				buddyList.setNeedsDisplay(true);
			}
		}
	}
	
}
