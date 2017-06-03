//	============================================================================================
//  MyQueue.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Tue Feb 11 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//
//	This class implements a queue for handling a collection of Strings.
//	============================================================================================

public class MyQueue {
	private MyQueueElement head;
	private MyQueueElement tail;
	private int length;
	
	public MyQueue() {
		head = null;
		tail = null;
		length = 0;
	}
	
	public void enqueue(String text) {
		if (length == 0) {
			head = new MyQueueElement(text);
			tail = null;
		} else {
			MyQueueElement newElement = new MyQueueElement(text);
			if (tail == null) {
				head.link(newElement);
			} else {
				tail.link(newElement);
				tail = newElement;
			}
		}
		length++;
	}
	
	public String dequeue() {
		String returnValue = null;
		if (length != 0) {
			returnValue = head.element();
			head = head.next();
			length--;
		}
		return returnValue;
	}
	
	public boolean hasElements() {
		if (length > 0) return true;
		else return false;
	}
	
}
