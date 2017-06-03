//	============================================================================================
//	MyConnectionController.java
//	Duchamp
//
//	Created by Ramon G. Ricca on Sun Jan 14 2003.
//	Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class is the connection controller. It handles the connection window, and is the 
//	NSApplication delegate. It creates the terminal window, has it's own sendText method that
//	sends text to the input stream.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.io.*;
import java.net.*;

public class MyConnectionController implements NSToolbarItem.ItemValidation {

	public NSButton		connectButton;			/* IBOutlet */
	public NSTableView	connectionsList;		/* IBOutlet */
	public NSWindow		connectionWindow;		/* IBOutlet */
	public NSTextField	serverText;				/* IBOutlet */
	public NSTextField	port;					/* IBOutlet */
	public NSTextField	usernameText;			/* IBOutlet */
	public NSSecureTextField	passwordText;	/* IBOutlet */
	public NSButton		lagButton;				/* IBOutlet */
	public NSDrawer		drawer;					/* IBOutlet */
	public NSButton		cancelButton;			/* IBOutlet */
	public NSTextField	settingText;			/* IBOutlet */
	public NSMatrix		serverType;				/* IBOutlet */
	
	private MyTerminalWindowController term = null;
	public  MyInputLine theInputLine;
	
	private String username;
	private String password;
	private String server;
	private Integer portNumber;
	
	private boolean isConnected = false;
	private MyBoardController control;
	private MyConnectionSettings list;
	private MyPreferences prefs;
	private NSToolbar myToolbar;

	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Default constructor does nothing
	//	-------------------------------------------------------------------------
	public MyConnectionController() {
		//	Handle getting the Connection Settings
		MySettingIO io = new MySettingIO();
		if (!io.hasSavedSettings()) list = new MyConnectionSettings();
		else list = new MyConnectionSettings(io.settings());
	}

	//	-------------------------------------------------------------------------
	//	method:		applicationDidFinishLaunching():	NSNotification
	//	NSApplication delegate method that opens a connection window once
	//	the application is fully launched
	//	-------------------------------------------------------------------------
   public void applicationDidFinishLaunching (NSNotification notification) {
		//	Handle the Connection Toolbar
		myToolbar = new NSToolbar("DuchampConnectionToolbar");
		myToolbar.setDelegate(this);
		myToolbar.setAllowsUserCustomization(false);
		myToolbar.setVisible(true);
		connectionWindow.setToolbar(myToolbar);
		connectionsList.setDataSource(list);
		connectionsList.setDelegate(this);
		
		openConnectionWindow(this);
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
		connectionWindow.close();
	}

	//	-------------------------------------------------------------------------
	//	method:		connect():	Object
	//	Creates the connection to the chess server. Resizes the terminal window
	//	according to the desired columns and widths.
	//	-------------------------------------------------------------------------
	public void connect(Object sender) { /* IBAction */
		connectButton.setState(NSCell.OnState);
		server = serverText.stringValue();
		username = usernameText.stringValue();
		password = passwordText.stringValue();
		boolean safe = true;
		
		try {
			portNumber = new Integer(port.stringValue());
		} catch (Exception e) {
			NSAlertPanel.beginAlertSheet("Illegal Port!","OK",null,null,connectionWindow,this,
				null,null,null,"The specified port is an illegal value. Only the digits 0-9 are allowed.");
			safe = false;
		}

		if (safe) {
			if ((server.length() > 0) && (!isConnected)) {
				closeWindow(this);
				
				control = new MyBoardController(username, serverType.selectedRow());
	
				term = new MyTerminalWindowController(server, username, password, portNumber);
				NSApplication.sharedApplication().loadNibNamed("Terminal", term);
				NSRect theRect = term.textView.frame();
				NSSize newSize = NSScrollView.frameSizeForContentSize(new NSSize (theRect.width(),theRect.height()),
								 false, true, NSView.NoBorder);
				NSRect winRect = term.terminalWindow.frame();
				NSRect newWinRect = new NSRect(winRect.x(),winRect.y(), newSize.width(), newSize.height() + 52f);
				term.terminalWindow.setFrame(newWinRect, true);
				term.textView.setAutoresizingMask(NSView.ViewWidthSizable | NSView.ViewHeightSizable);
				term.terminalWindow.setFrameAutosaveName("Duchamp.Terminal");
				term.terminalWindow.setTitle(username + "@" + server);
				term.terminalWindow.setDelegate(term);
				term.terminalWindow.makeKeyAndOrderFront(this);
				term.startStreams();
				
				theInputLine = term.inputLine;
								
				isConnected = true;
			} else {
				NSAlertPanel.beginAlertSheet("No Server Specified!","OK",null,null,connectionWindow,this,
					null,null,null,"You must specify at least the server name before you can connect!");
				connectButton.setState(NSCell.OffState);
			}
		} else {
			connectButton.setState(NSCell.OffState);
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		openConnectionWindow():	Object
	//	Makes the connection window visible and highlights the connect button
	//	-------------------------------------------------------------------------
	public void openConnectionWindow(Object sender) {
		if (!isConnected) {
			connectionWindow.makeKeyAndOrderFront(this);
			if (drawer.state() == NSDrawer.ClosedState) drawer.open();
			connectButton.highlight(true);
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
	//	method:		sendText():	String
	//	Sends text to the input stream (sent to the terminal window)
	//	-------------------------------------------------------------------------
	public void sendText(String text) {
		if (isConnected) {
			term.sendText(text);
		}
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
		
		if (term != null) {
			if (term.terminalWindow != null) term.terminalWindow.saveFrameUsingName("Duchamp.Terminal");
		}
		
		if (list.count() > 0) MySettingIO.writeSettings(list.returnList());

		return shouldIClose;
	}
	
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
			connectionsList.reloadData();
			connectionsList.setNeedsDisplay(true);
		} else {
			NSAlertPanel.beginAlertSheet("No Setting ID!","OK",null,null,connectionWindow,this,
					null,null,null,"You must specify a settings name to save your current settings.");
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		removeSetting():	Object
	//	Removes the current setting
	//	-------------------------------------------------------------------------
	public void removeSetting() {
		int index = connectionsList.selectedRow();
		list.removeDict(index);
		connectionsList.reloadData();
	}
	
	//	-------------------------------------------------------------------------
	//	method:		tableViewShouldSelectRow():	NSTableView, int
	//	Called when a row is selected in the connections list
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
	
	//	-------------------------------------------------------------------------
	//	method:		disconnect():	no parameters
	//	Called when the terminal window gets the end of the stream
	//	-------------------------------------------------------------------------
	public void disconnect() {
		if (term != null) {
			term.terminalWindow.setDelegate(null);
			term.disconnect();
			term = null;
			theInputLine = null;
			isConnected = false;
		}
	}
	
	//	-------------------------------------------------------------------------
	//	method:		updateSetting():	Object
	//	Updates the setting if user has changed the connection setting
	//	-------------------------------------------------------------------------
	public void updateSetting() {
		int index = connectionsList.selectedRow();
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
			connectionsList.reloadData();
		} else {
			NSAlertPanel.beginAlertSheet("Settings ID has changed!","OK",null,null,connectionWindow,this,
					null,null,null,"Make sure you are editing the same setting. Re-select the setting in the connections list.");
		}
	}
	
	public void toggleDrawer() {
		drawer.toggle(this);
	}
	
	public MyBoardController returnBoardController() {
		return this.control;
	}
	
	public MyTerminalWindowController returnTerminalController() {
		return this.term;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public MyInputLine returnInputLine() {
		return theInputLine;
	}
	
	public void openPrefs(Object sender) {
		prefs.theWindow.makeKeyAndOrderFront(this);
	}
	
	
	
	//	-------------------------------------------------------------------------
	//	Handle Toolbar delegate operations
	//	-------------------------------------------------------------------------
	public boolean validateToolbarItem(NSToolbarItem theItem) {
		return true;
	}
	
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

}

