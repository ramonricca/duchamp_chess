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

public class MyMainWindowController implements NSToolbarItem.ItemValidation {

    public NSButton connect; /* IBOutlet */
    public MyInputLine inputLine; /* IBOutlet */
    public NSButton lagButton; /* IBOutlet */
    public NSButton newSeek; /* IBOutlet */
    public NSSecureTextField passwordText; /* IBOutlet */
    public NSTextField port; /* IBOutlet */
    public NSPopUpButton seekList; /* IBOutlet */
    public MySeekView seekView; /* IBOutlet */
    public NSPopUpButton serverSettings; /* IBOutlet */
    public NSTextField serverText; /* IBOutlet */
    public NSMatrix serverType; /* IBOutlet */
    public NSTextField settingText; /* IBOutlet */
    public MyTerminalView terminal; /* IBOutlet */
    public NSTextField usernameText; /* IBOutlet */
    public MyWindow window; /* IBOutlet */
	
	//	These variables are for the instance of a connection	
	private String username;
	private String password;
	private String server;
	private Integer portNumber;

	//	These variables are for the terminal input output	
    private PrintWriter to_server;
    private BufferedReader from_user;
    private Socket s;
    public MyClientThread t;

	private boolean isConnected = false;
	private MyBoardController control;
	private MyConnectionSettings list;
	private MyPreferences prefs;
	private NSToolbar myToolbar;

	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Default constructor does nothing
	//	-------------------------------------------------------------------------
	public MyMainWindowController() {
		//	Handle getting the Connection Settings
		MySettingIO io = new MySettingIO();
		if (!io.hasSavedSettings()) list = new MyConnectionSettings();
		else list = new MyConnectionSettings(io.settings());
		
		NSNotificationCenter center = NSDistributedNotificationCenter.defaultCenter();
		center.addObserver(this, new NSSelector("sendInput", new Class[] {Object.class}),"SendTheInput", null);
		center.addObserver(this, new NSSelector("sendLogin", new Class[] {}),"SendLogin", null);
		center.addObserver(this, new NSSelector("sendTextNotification", new Class[] {NSNotification.class}),"sendExtra", null);
	}

	//	-------------------------------------------------------------------------
	//	method:		applicationDidFinishLaunching():	NSNotification
	//	NSApplication delegate method that opens a connection window once
	//	the application is fully launched
	//	-------------------------------------------------------------------------
   public void applicationDidFinishLaunching (NSNotification notification) {
		//	Handle the Connection Toolbar
/* 		myToolbar = new NSToolbar("DuchampConnectionToolbar"); */
/* 		myToolbar.setDelegate(this); */
/* 		myToolbar.setAllowsUserCustomization(false); */
/* 		myToolbar.setVisible(true); */
/* 		window.setToolbar(myToolbar); */
		settings.setDataSource(list);
		settings.setDelegate(this);
		window.setInputLine(inputLine);
		openWindow(this);
		
		//	Handle Preferences and Toolbar
		prefs = new MyPreferences();
		NSApplication.sharedApplication().loadNibNamed("Preferences", prefs);
		prefs.setToolbar();
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
		//connectButton.setState(NSCell.OnState);
		
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
			if ((server.length() > 0) && (!isConnected)) {
				//	Initialize the Board Controller
/* 				control = new MyBoardController(username, serverType.selectedRow()); */
				control = new MyBoardController(username, 0);
				//	Set Title of main Window and start the connection streams
				window.setTitle("Duchamp: " + username + "@" + server);
				startStreams();
				isConnected = true;
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
		if (!isConnected) {
			window.makeKeyAndOrderFront(this);
			if (drawer.state() == NSDrawer.ClosedState) drawer.open();
			connect.highlight(true);
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		sendMacro():	Object
	//	Called by the Macro Menu. Sends the appropriate macro to sendText
	//	-------------------------------------------------------------------------
	public void sendMacro(Object sender) {
		sendText(((NSMenuItem)sender).title());
	}
		
	//	-------------------------------------------------------------------------
	//	method:		applicationShouldTerminate():	NSApplication
	//	delegate method to clean up a bit before quitting
	//	-------------------------------------------------------------------------
	public boolean applicationShouldTerminate(NSApplication sender) {
		int result;
		boolean shouldIClose = false;
		if (isConnected) {
			if (control.haveBoardsOpen()) result = NSAlertPanel.runAlert("Quit", "You have boards open. Do you really want to quit?",
				"Cancel", "Disconnect and Quit", null);
				
			else result = NSAlertPanel.runAlert( "Quit", "You are still connected to " + server + ". Do you really want to quit?", 
				"Cancel", "Disconnect and Quit", null);
			if (result == NSAlertPanel.DefaultReturn) shouldIClose = false;
			if (result == NSAlertPanel.AlternateReturn) shouldIClose = true;
		} else {
			shouldIClose = true;
		}
				
		if (list.count() > 0) MySettingIO.writeSettings(list.returnList());

		return shouldIClose;
	}
	
	//	-------------------------------------------------------------------------
	//	method:		disconnect():	no parameters
	//	Called when the terminal window gets the end of the stream
	//	-------------------------------------------------------------------------
	public void disconnect() {
		isConnected = false;
		try {
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void toggleDrawer() {
		drawer.toggle(this);
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
	
	public void openPrefs(Object sender) {
		prefs.theWindow.makeKeyAndOrderFront(this);
	}
	
 	//	-------------------------------------------------------------------------
	//	method:		startStreams():		no parameters
	//	Gets the streams going and starts the thread that monitors the connection
	//	-------------------------------------------------------------------------
   public void startStreams() {
		try {
			s = new Socket(server, port.intValue());

			final Reader from_server	= new InputStreamReader(s.getInputStream());
			to_server	= new PrintWriter(s.getOutputStream());
			
			from_user	= new BufferedReader(new InputStreamReader(System.in));
			final PrintWriter to_user		= new PrintWriter(System.out, true);

			t = new MyClientThread(from_server, terminal);
			
			t.setPriority(Thread.NORM_PRIORITY + 2);

			t.start();
		
		} catch (Exception e) {
			System.err.println(e);
		}
	}
    
//	============================================================================================
//	This code block contains the functions relating to input output for the terminal
//	============================================================================================
  	//	-------------------------------------------------------------------------
	//	method:		sendInput():		Object
	//	Sends the input from the input line and then clears the input line
	//	-------------------------------------------------------------------------
   public synchronized void sendInput(Object sender) { /* IBAction */
    	try {
			to_server.println(inputLine.string());
			to_server.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		clearInputLine();
	}
   
  	//	-------------------------------------------------------------------------
	//	method:		sendLogin():		no parameters
	//	Gets called by the thread to login automatically.
	//	-------------------------------------------------------------------------
   public synchronized void sendLogin() {
   		to_server.println(username);
   		to_server.println(password);
   		to_server.println("set style 12");
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
	//	method:		sendTextNotification():		NSNotification
	//	Sends the text to the server. Text specified in the Notification
	//	-------------------------------------------------------------------------
	public synchronized void sendTextNotification (NSNotification note) {
		String text = (String)note.object();
		to_server.println(text);
		to_server.flush();
	}
	
  	//	-------------------------------------------------------------------------
	//	method:		sendText():		String
	//	Another method to send text to the server. Called by the connection
	//	controller, and other classes as well
	//	-------------------------------------------------------------------------
	public synchronized void sendText(String text) {
		to_server.println(text);
		to_server.flush();
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
			list.addDict(new NSDictionary(ob, MySettingIO.PLIST));
			settings.reloadData();
			settings.setNeedsDisplay(true);
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
		int index = settings.selectedRow();
		list.removeDict(index);
		settings.reloadData();
	}
	
	//	-------------------------------------------------------------------------
	//	method:		updateSetting():	Object
	//	Updates the setting if user has changed the connection setting
	//	-------------------------------------------------------------------------
	public void updateSetting() {
		int index = settings.selectedRow();
		NSDictionary dict = list.getDictAtIndex(index);
		String lag = (lagButton.state() == NSCell.OffState ) ? "0" : "1";
		String type = String.valueOf(serverType.selectedRow());
		if ((settingText.stringValue()).equals((String)dict.objectForKey(MySettingIO.settingPLIST))) {
			Object[] ob =	{	serverText.stringValue(), 
								port.stringValue(), 
								usernameText.stringValue(), 
								passwordText.stringValue(), 
								settingText.stringValue(), 
								type,
								lag
							};
			list.updateDict(new NSDictionary(ob, MySettingIO.PLIST), index);
			settings.reloadData();
		} else {
			NSAlertPanel.beginAlertSheet("Settings ID has changed!","OK",null,null,window,this,
					null,null,null,"Make sure you are editing the same setting. Re-select the setting in the connections list.");
		}
	}
	
//	============================================================================================
//	Handle Toolbar delegate operations
//	============================================================================================
	//	-------------------------------------------------------------------------
	//	validateToolbarItem:	toolbar delegate function that makes sure that
	//	the item in the toolbar is ok.
	//	-------------------------------------------------------------------------
	public boolean validateToolbarItem(NSToolbarItem theItem) {
		return true;
	}
	
	//	-------------------------------------------------------------------------
	//	toolbarItemForItemIdentifier:	toolbar delegate function that returns
	//	the toolbar item. Could be made a little bit faster
	//	-------------------------------------------------------------------------
	public NSToolbarItem toolbarItemForItemIdentifier( NSToolbar toolbar, String itemIdentifier, boolean flag) {
		NSToolbarItem item = null;
		if (itemIdentifier.equals("Add")) {
			NSImage addImage = new NSImage(NSBundle.mainBundle().pathForResource("Add", "tif"), false);
			item = new NSToolbarItem("Add");
			item.setImage(addImage);
			item.setLabel("Add");
			item.setAction(new NSSelector("addSetting", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		if (itemIdentifier.equals("Remove")) {
			NSImage removeImage = new NSImage(NSBundle.mainBundle().pathForResource("Remove", "tif"), false);
			item = new NSToolbarItem("Remove");
			item.setImage(removeImage);
			item.setLabel("Remove");
			item.setAction(new NSSelector("removeSetting", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		if (itemIdentifier.equals("Update")) {
			NSImage updateImage = new NSImage(NSBundle.mainBundle().pathForResource("Update", "tif"), false);
			item = new NSToolbarItem("Update");
			item.setImage(updateImage);
			item.setLabel("Update");
			item.setAction(new NSSelector("updateSetting", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		if (itemIdentifier.equals("Drawer")) {
			NSImage drawerImage = new NSImage(NSBundle.mainBundle().pathForResource("Drawer", "tif"), false);
			item = new NSToolbarItem("Drawer");
			item.setImage(drawerImage);
			item.setLabel("Drawer");
			item.setAction(new NSSelector("toggleDrawer", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		
		return item;
	}
	
	//	-------------------------------------------------------------------------
	//	toolbarAllowedItemIdentifiers:	returns the valid toolbar items
	//	-------------------------------------------------------------------------
	public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new String[] {"Add","Remove","Update","Drawer",
			NSToolbarItem.SeparatorItemIdentifier, NSToolbarItem.FlexibleSpaceItemIdentifier });
	}
		
	public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
		return new NSArray (new String[] {  NSToolbarItem.FlexibleSpaceItemIdentifier, "Add", 
											"Remove",
											"Update", NSToolbarItem.FlexibleSpaceItemIdentifier,
											NSToolbarItem.SeparatorItemIdentifier, "Drawer" } );
	}
	
	//	-------------------------------------------------------------------------
	//	method:		tableViewShouldSelectRow():	NSTableView, int
	//	Called when a row is selected in the connections list
	//	Table delegate operation
	//	-------------------------------------------------------------------------
	public boolean tableViewShouldSelectRow( NSTableView aTableView, int rowIndex) {
		NSDictionary dict = list.getDictAtIndex(rowIndex);
		
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
		return true;
	}
	
	
}
