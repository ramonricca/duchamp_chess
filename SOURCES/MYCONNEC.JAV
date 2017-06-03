//	============================================================================================
//  MyConnectionSettings.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Fri Jan 31 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//
//	This class defines data and methods that define a connection setting that can be 
//	displayed in the NSBrowser. 
//
//	30301	Kept old NSTableView code so that when we switch back with new interface in
//			next version, we don't have to recode. 
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.lang.*;

public class MyConnectionSettings implements NSTableView.DataSource {
	private NSMutableArray list;
	
	//	-------------------------------------------------------------------------
	//	Constructor():	NSArray
	//	Constructor when there are settings from the disk
	//	-------------------------------------------------------------------------
	public MyConnectionSettings(NSArray inArray) {
		list = new NSMutableArray(inArray);
	}
	
	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Constructor when there are are no settings from disk
	//	-------------------------------------------------------------------------
	public MyConnectionSettings() {
		list = new NSMutableArray();
	}
	
	//	-------------------------------------------------------------------------
	//	numberOfRowsInTableView():	NSTableView
	//	returns the number of rows
	//	-------------------------------------------------------------------------
	public int numberOfRowsInTableView(NSTableView aTableView) {
		return list.count();
	}
	
	//	-------------------------------------------------------------------------
	//	tableViewObjectValueForLocation():	lot of parameters
	//	returns the string that shows up in the table view
	//	-------------------------------------------------------------------------
	public Object tableViewObjectValueForLocation( NSTableView aTableView, NSTableColumn aTableColumn, int rowIndex) {
		return (String)((NSDictionary)list.objectAtIndex(rowIndex)).objectForKey(MySettingIO.settingPLIST);
	}
	
	//	-------------------------------------------------------------------------
	//	tableViewSetObjectValueForLocation():	lots of parameters
	//	inserts the new setting at the index
	//	-------------------------------------------------------------------------
	public void tableViewSetObjectValueForLocation( NSTableView aTableView, Object anObject, 
		NSTableColumn aTableColumn, int rowIndex) {
		list.insertObjectAtIndex(anObject, rowIndex);
	}
	
	//	-------------------------------------------------------------------------
	//	tableViewAcceptDrop():	lots of parameters
	//	returns false (not implemented)
	//	-------------------------------------------------------------------------
	public boolean tableViewAcceptDrop( NSTableView tableView, NSDraggingInfo info, int row, int operation) {
		return false;
	}
	
	//	-------------------------------------------------------------------------
	//	addDict():	NSDictionary
	//	add a new setting
	//	-------------------------------------------------------------------------
	public void addDict(NSDictionary dict) {
		list.addObject(dict);
	}
	
	//	-------------------------------------------------------------------------
	//	returnList():	no parameters
	//	returns the entire list
	//	-------------------------------------------------------------------------
	public NSArray returnList() {
		if (count() > 0 ) return (NSArray)list;
		else return null;
	}
	
	//	-------------------------------------------------------------------------
	//	tableViewWriteRowsToPasteboard():	lots of parameters
	//	returns false (not implemented)
	//	-------------------------------------------------------------------------
	public boolean tableViewWriteRowsToPasteboard( NSTableView tableView, NSArray rows, NSPasteboard pboard) {
		return false;
	}
	
	//	-------------------------------------------------------------------------
	//	count():	no parameters
	//	returns the number of entries
	//	-------------------------------------------------------------------------
	public int count() {
		return list.count();
	}
	
	//	-------------------------------------------------------------------------
	//	tableViewValidateDrop():	lots of parameters
	//	returns "DragOperationNone" because we don't accept drags
	//	-------------------------------------------------------------------------
	public int tableViewValidateDrop( NSTableView tableView, NSDraggingInfo info, int row, int operation) {
		return NSDraggingInfo.DragOperationNone;
	}
	
	//	-------------------------------------------------------------------------
	//	getDictAtIndex():	int
	//	returns the dictionary at the index
	//	-------------------------------------------------------------------------
	public NSDictionary getDictAtIndex(int index) {
		return (NSDictionary)list.objectAtIndex(index);
	}
	
	//	-------------------------------------------------------------------------
	//	updateDict():	NSDictionary, int
	//	update the replaced dictionary
	//	-------------------------------------------------------------------------
	public void updateDict(NSDictionary dict, int index) {
		list.replaceObjectAtIndex( index, dict);
	}
	
	//	-------------------------------------------------------------------------
	//	removeDict():	int
	//	removes the dictionary at index
	//	-------------------------------------------------------------------------
	public void removeDict(int index) {
		list.removeObjectAtIndex(index);
	}
	
	public NSArray getAllSettings() {
		NSMutableArray names = new NSMutableArray();
		for (int i = 0; i < list.count(); i++) {
			names.addObject((String)((NSDictionary)list.objectAtIndex(i)).objectForKey(MySettingIO.settingPLIST));
		}
		return (NSArray)names;
	}
}
