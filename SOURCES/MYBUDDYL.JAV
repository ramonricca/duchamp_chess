//	============================================================================================
//  MyBuddyList.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Mar 02 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.lang.*;

public class MyBuddyList implements NSTableView.DataSource {
	private NSMutableArray list;
	
	//	-------------------------------------------------------------------------
	//	Constructor():	NSArray
	//	Constructor when there are settings from the disk
	//	-------------------------------------------------------------------------
	public MyBuddyList(NSArray inArray) {
		list = new NSMutableArray(inArray);
	}
	
	//	-------------------------------------------------------------------------
	//	Constructor():	no parameters
	//	Constructor when there are are no settings from disk
	//	-------------------------------------------------------------------------
	public MyBuddyList() {
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
		return list.objectAtIndex(rowIndex);
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
	public void addName(String name) {
		list.addObject(name);
	}
	
	//	-------------------------------------------------------------------------
	//	returnList():	no parameters
	//	returns the entire list
	//	-------------------------------------------------------------------------
	public NSArray returnList() {
		if (count() > 0) return (NSArray)list;
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
	public String getNameAtIndex(int index) {
		return (String)list.objectAtIndex(index);
	}
	
	//	-------------------------------------------------------------------------
	//	updateDict():	NSDictionary, int
	//	update the replaced dictionary
	//	-------------------------------------------------------------------------
	public void updateName(String name, int index) {
		list.replaceObjectAtIndex(index, name);
	}
	
	//	-------------------------------------------------------------------------
	//	removeDict():	int
	//	removes the dictionary at index
	//	-------------------------------------------------------------------------
	public void removeName(int index) {
		list.removeObjectAtIndex(index);
	}
	
	public NSArray getAllSettings() {
		NSMutableArray names = new NSMutableArray();
		for (int i = 0; i < list.count(); i++) {
			names.addObject((String)list.objectAtIndex(i));
		}
		return (NSArray)names;
	}
}
