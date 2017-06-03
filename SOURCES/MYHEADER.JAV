//	============================================================================================
//  MyHeaderFieldCell.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Mon Feb 17 2003.
//  Copyright (c) 2003 Clique Software. All rights reserved.
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class MyHeaderFieldCell extends NSTextFieldCell {
	private NSImage thePawn;
	
	public MyHeaderFieldCell() {
		super();
	}
	
	public MyHeaderFieldCell(NSImage pawn) {
		super(pawn);
		thePawn = pawn;
		setBezeled(true);
		setBezelStyle(NSTextFieldCell.TextFieldRoundedBezel);
		setEditable(false);
		setSelectable(false);
		setFont(NSFont.fontWithNameAndSize("Copperplate-Light",11f));
	}
	
	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		super.drawInteriorWithFrameInView(cellFrame, controlView);
				
		NSMutableRect rect = new NSMutableRect(cellFrame);
		rect.insetRect(2f,2f);
		NSRect imageRect = new NSRect((rect.x() + rect.width() - 20f), rect.y(), 16f,16f);
		NSGraphicsContext.currentContext().setImageInterpolation(NSGraphicsContext.ImageInterpolationHigh);
		thePawn.drawInRect(imageRect,new NSRect(0f,0f,108f,108f),NSImage.CompositeSourceOver,0.7f);
	}

}
