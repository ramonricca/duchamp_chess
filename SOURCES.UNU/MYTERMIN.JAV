//	============================================================================================
//  MyTerminalWindowController.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 14 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class controls the terminal window. It gets the socket, and initializes the streams.
//	It also controls the actual input to stream methods.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.io.*;
import java.net.*;

public class MyTerminalWindowController {

    public MyInputLine inputLine; /* IBOutlet */
    public NSWindow terminalWindow; /* IBOutlet */
    public MyTerminalView textView; /* IBOutlet */
    
    private String username;
    private String password;
    private String server;
    private Integer port;
            
    private PrintWriter to_server;
    private BufferedReader from_user;
    private Socket s;
    public MyClientThread t;

	//	-------------------------------------------------------------------------
	//	Constructor():	String, String, String
	//	Default constructor. Sets the server, username, and password variables
	//	and sets up the notification listeners.
	//	-------------------------------------------------------------------------
    public MyTerminalWindowController(String sv, String un, String pw, Integer pt) {
		server = sv;
		username = un;
		password = pw;
		port = pt;

		NSNotificationCenter center = NSDistributedNotificationCenter.defaultCenter();
		center.addObserver(this, new NSSelector("sendInput", new Class[] {Object.class}),"SendTheInput", null);
		center.addObserver(this, new NSSelector("sendLogin", new Class[] {}),"SendLogin", null);
		center.addObserver(this, new NSSelector("sendTextNotification", new Class[] {NSNotification.class}),"sendExtra", null);
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

			t = new MyClientThread(from_server, textView);
			
			t.setPriority(Thread.NORM_PRIORITY + 2);

			t.start();
		
		} catch (Exception e) {
			System.err.println(e);
		}
	}
    
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
	
	//	-------------------------------------------------------------------------
	//	method:		windowShouldClose():	Object
	//	delegate method of the Terminal Window
	//	Removes the window object from the array of boards. Sends a final string
	//	to show that it has removed the window.
	//	-------------------------------------------------------------------------
	public boolean windowShouldClose(Object sender) {
		MyConnectionController control = (MyConnectionController)(NSApplication.sharedApplication()).delegate();
		if (!control.isConnected())  {
			terminalWindow.saveFrameUsingName("Duchamp.Terminal");
			return true;
		} else if (! control.returnBoardController().haveBoardsOpen()) {
			NSAlertPanel.beginInformationalAlertSheet("You are still connected","OK", null,null,
				terminalWindow, null,null,null,null,"Please type \'exit\' in the terminal window first.");
			return false;
		} else {
			NSAlertPanel.beginInformationalAlertSheet("You have boards open.","OK", null,null,
				terminalWindow, null,null,null,null,"You can not close the terminal window while you have boards open. Close all boards and type exit to disconnect cleanly.");
			return false;
		}
	} 

	public void disconnect() {
		try {
			s.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
