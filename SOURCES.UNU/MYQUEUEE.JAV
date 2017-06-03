//	============================================================================================
//  MyQueueElement.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Tue Feb 11 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//
//	This class encapsulates our queue data. It will be handled by the MyQueue class.
//	============================================================================================

public class MyQueueElement {
	private	String element;
	private MyQueueElement next;
	
	public MyQueueElement(String item) {
		element = item;
		next = null;
	}
	
	public String element() {
		return element;
	}
	
	public void link(MyQueueElement obj) {
		next = obj;
	}
	
	public MyQueueElement next() {
		return next;
	}
	
}
