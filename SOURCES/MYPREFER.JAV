//	============================================================================================
//  MyPreferences.java
//  Duchamp
//
//  Created by Ramon G. Ricca on Sun Jan 24 2003.
//  Copyright (c) 2003 CLique Software. All rights reserved.
//
//	This class is the controller for the Preferences Window
//	============================================================================================

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class MyPreferences implements NSToolbarItem.ItemValidation {
	
	//	--------------------------------------------------
	//	Interface Builder Outlets
	//	--------------------------------------------------
    public NSPopUpButton antialias; /* IBOutlet */
    public NSButton autoflag; /* IBOutlet */
    public NSButton blitz; /* IBOutlet */
    public NSView boardPanel; /* IBOutlet */
    public NSButton crazyhouse; /* IBOutlet */
    public NSButton cshouts; /* IBOutlet */
    public NSButton cube; /* IBOutlet */
    public NSColorWell darkSquares; /* IBOutlet */
    public MyExampleBoard examBoard; /* IBOutlet */
    public NSTextView examTerm; /* IBOutlet */
    public NSButton flipButton; /* IBOutlet */
    public NSTextField fontName; /* IBOutlet */
    public NSView generalPanel; /* IBOutlet */
    public NSButton guestTells; /* IBOutlet */
    public NSColorWell lightSquares; /* IBOutlet */
    public NSView loadingPanel; /* IBOutlet */
    public NSPopUpButton mySound; /* IBOutlet */
    public NSPopUpButton pieces; /* IBOutlet */
    public NSColorWell rated; /* IBOutlet */
    public NSColorWell seekBackground; /* IBOutlet */
    public NSColorWell seekForeground; /* IBOutlet */
    public NSView seekPanel; /* IBOutlet */
    public NSPopUpButton seekTimeout; /* IBOutlet */
    public NSPopUpButton serverTimeout; /* IBOutlet */
    public NSButton setFont; /* IBOutlet */
    public NSButton shouts; /* IBOutlet */
    public NSPopUpButton squareSize; /* IBOutlet */
    public NSButton suicide; /* IBOutlet */
    public NSColorWell termBackground; /* IBOutlet */
    public NSColorWell termForeground; /* IBOutlet */
    public NSView terminalPanel; /* IBOutlet */
    public NSPopUpButton theirSound; /* IBOutlet */
    public NSWindow theWindow; /* IBOutlet */
    public NSColorWell unrated; /* IBOutlet */
    public NSButton wild; /* IBOutlet */
	
	//	--------------------------------------------------
	//	Static Final Strings to identify dict objects
	//	--------------------------------------------------
	public static final String blitzPref			= "showBlitz";
	public static final String crazyhousePref		= "showCrazyhouse";
	public static final String darkSquaresColor		= "darkSquaresColor";
	public static final String flipBoard			= "flipBoard";
	public static final String terminalFont			= "terminalFont";
	public static final String lightSquaresColor	= "lightSquaresColor";
	public static final String mSound				= "mySound";
	public static final String chessSet				= "chessSet";
	public static final String ratedColor			= "ratedColor";
	public static final String seekingTimeout		= "seekTimeout";
	public static final String boardSize			= "boardSize";
	public static final String suicidePref			= "showSuicide";
	public static final String terminalBackColor	= "termBackColor";
	public static final String terminalForeColor	= "termForeColor";
	public static final String tSound				= "theirSound";
	public static final String unratedColor			= "unratedColor";
	public static final String wildPref				= "showWild";
	public static final String aalias				= "antialias";
	public static final String aFlag				= "autoflag";
	public static final String cShouts				= "cShouts";
	public static final String gTells				= "gTells";
	public static final String serverTime			= "serverTimeout";
	public static final String Shouts				= "shouts";
	public static final String seekBackColor		= "seekBackColor";
	public static final String seekForeColor		= "seekForeColor";
	public static final String cubeMask				= "cubeMask";
	
	//	--------------------------------------------------
	//	Preference access variables
	//	--------------------------------------------------
	private static NSDictionary defaultValues = null;
	private static MyPreferences sharedInstance = null;
	private NSDictionary curValues;
	private NSMutableDictionary displayedValues;
	
	//	--------------------------------------------------
	//	Toolbar and Misc
	//	--------------------------------------------------
	public NSToolbar	myToolbar;
	private NSRect squares[][] = new NSRect[8][8];
	private NSMutableArray cpieces = new NSMutableArray();
	private String[] imageName = {"WP","WB","WN","WR","WQ","WK","BP","BB","BN","BR","BQ","BK"};
	
	//	-------------------------------------------------------------------------
	//	Default Constructor:	Creates toolbar instance
	//	-------------------------------------------------------------------------
	public MyPreferences() {
		NSApplication.loadNibNamed("Preferences", this);
		
		myToolbar = new NSToolbar("DuchampPreferencesToolbar");
		myToolbar.setDelegate(this);
		myToolbar.setAllowsUserCustomization(false);
		myToolbar.setVisible(true);
		theWindow.setToolbar(myToolbar);
		theWindow.setDelegate(this);

		MyPreferences.sharedInstance = this;
		curValues = (NSDictionary)MyPreferences.preferencesFromDefaults();
		discardDisplayedValues();
		updateBoard();
	}
	
	public boolean windowShouldClose(Object sender) {
		((MyMainWindowController)NSApplication.sharedApplication().delegate()).setAllPrefs();
		return true;
	}

	public NSDictionary preferences() {
		return curValues;
	}
	
	public void showFontInField(NSFont font) {
		fontName.setStringValue(font != null ? font.fontName() + " " + font.pointSize() : "");
	}
	
	public void updateUserInterface() {
		//	On/Off Buttons
		blitz.setState(((Integer)displayedValues.objectForKey(blitzPref)).intValue());
		crazyhouse.setState(((Integer)displayedValues.objectForKey(crazyhousePref)).intValue());
		suicide.setState(((Integer)displayedValues.objectForKey(suicidePref)).intValue());
		wild.setState(((Integer)displayedValues.objectForKey(wildPref)).intValue());
		flipButton.setState(((Integer)displayedValues.objectForKey(flipBoard)).intValue());
		autoflag.setState(((Integer)displayedValues.objectForKey(aFlag)).intValue());
		cshouts.setState(((Integer)displayedValues.objectForKey(cShouts)).intValue());
		guestTells.setState(((Integer)displayedValues.objectForKey(gTells)).intValue());
		shouts.setState(((Integer)displayedValues.objectForKey(Shouts)).intValue());
		cube.setState(((Integer)displayedValues.objectForKey(cubeMask)).intValue());
		
		//	PopUpButtons
		mySound.selectItemAtIndex(((Integer)displayedValues.objectForKey(mSound)).intValue());
		theirSound.selectItemAtIndex(((Integer)displayedValues.objectForKey(tSound)).intValue());
		pieces.selectItemAtIndex(((Integer)displayedValues.objectForKey(chessSet)).intValue());
		seekTimeout.selectItemAtIndex(((Integer)displayedValues.objectForKey(seekingTimeout)).intValue());
		squareSize.selectItemAtIndex(((Integer)displayedValues.objectForKey(boardSize)).intValue());
		antialias.selectItemAtIndex(((Integer)displayedValues.objectForKey(aalias)).intValue());
		serverTimeout.selectItemAtIndex(((Integer)displayedValues.objectForKey(serverTime)).intValue());
		
		//	ColorWells
		darkSquares.setColor((NSColor)displayedValues.objectForKey(darkSquaresColor));
		lightSquares.setColor((NSColor)displayedValues.objectForKey(lightSquaresColor));
		rated.setColor((NSColor)displayedValues.objectForKey(ratedColor));
		unrated.setColor((NSColor)displayedValues.objectForKey(unratedColor));
		termBackground.setColor((NSColor)displayedValues.objectForKey(terminalBackColor));
		termForeground.setColor((NSColor)displayedValues.objectForKey(terminalForeColor));
		seekBackground.setColor((NSColor)displayedValues.objectForKey(seekBackColor));
		seekForeground.setColor((NSColor)displayedValues.objectForKey(seekForeColor));
		
		//	Fonts
		showFontInField((NSFont)displayedValues.objectForKey(terminalFont));
		
		//	Show example panes
		showNewTerm(this);
		showNewBoard(this);

	}
	
	public void changePrefs(Object sender) {
		displayedValues.setObjectForKey(new Integer(blitz.state()), blitzPref);
		displayedValues.setObjectForKey(new Integer(crazyhouse.state()), crazyhousePref);
		displayedValues.setObjectForKey(new Integer(suicide.state()), suicidePref);
		displayedValues.setObjectForKey(new Integer(wild.state()), wildPref);
		displayedValues.setObjectForKey(new Integer(flipButton.state()), flipBoard);
		displayedValues.setObjectForKey(new Integer(autoflag.state()), aFlag);
		displayedValues.setObjectForKey(new Integer(cshouts.state()), cShouts);
		displayedValues.setObjectForKey(new Integer(guestTells.state()), gTells);
		displayedValues.setObjectForKey(new Integer(shouts.state()), Shouts);
		displayedValues.setObjectForKey(new Integer(cube.state()), cubeMask);
		
		displayedValues.setObjectForKey(new Integer(mySound.indexOfSelectedItem()), mSound);
		displayedValues.setObjectForKey(new Integer(theirSound.indexOfSelectedItem()), tSound);
		displayedValues.setObjectForKey(new Integer(pieces.indexOfSelectedItem()), chessSet);
		displayedValues.setObjectForKey(new Integer(seekTimeout.indexOfSelectedItem()), seekingTimeout);
		displayedValues.setObjectForKey(new Integer(squareSize.indexOfSelectedItem()), boardSize);
		displayedValues.setObjectForKey(new Integer(antialias.indexOfSelectedItem()), aalias);
		displayedValues.setObjectForKey(new Integer(serverTimeout.indexOfSelectedItem()), serverTime);
		
		displayedValues.setObjectForKey(darkSquares.color(), darkSquaresColor);
		displayedValues.setObjectForKey(lightSquares.color(), lightSquaresColor);
		displayedValues.setObjectForKey(rated.color(), ratedColor);
		displayedValues.setObjectForKey(unrated.color(), unratedColor);
		displayedValues.setObjectForKey(termBackground.color(), terminalBackColor);
		displayedValues.setObjectForKey(termForeground.color(), terminalForeColor);
		displayedValues.setObjectForKey(seekBackground.color(), seekBackColor);
		displayedValues.setObjectForKey(seekForeground.color(), seekForeColor);
		
		if ((sender == (Object)mySound) && (mySound.indexOfSelectedItem() != 0)) {
			(NSSound.soundNamed((mySound.selectedItem()).title())).play();
		}
		
		if ((sender == (Object)theirSound) && (theirSound.indexOfSelectedItem() != 0)) {
			(NSSound.soundNamed((theirSound.selectedItem()).title())).play();
		}
				
		commitDisplayedValues();
	}
	
	public static NSMutableDictionary preferencesFromDefaults() {
		NSUserDefaults defaults = NSUserDefaults.standardUserDefaults();
		NSMutableDictionary dict = new NSMutableDictionary();
		
		getIntegerDefault(defaults, blitzPref, dict);
		getIntegerDefault(defaults, crazyhousePref, dict);
		getIntegerDefault(defaults, suicidePref, dict);
		getIntegerDefault(defaults, wildPref, dict);
		getIntegerDefault(defaults, flipBoard, dict);
		getIntegerDefault(defaults, mSound, dict);
		getIntegerDefault(defaults, tSound, dict);
		getIntegerDefault(defaults, chessSet, dict);
		getIntegerDefault(defaults, seekingTimeout, dict);
		getIntegerDefault(defaults, boardSize, dict);
		getIntegerDefault(defaults, aalias, dict);
		getIntegerDefault(defaults, aFlag, dict);
		getIntegerDefault(defaults, cShouts, dict);
		getIntegerDefault(defaults, gTells, dict);
		getIntegerDefault(defaults, serverTime, dict);
		getIntegerDefault(defaults, Shouts, dict);
		getIntegerDefault(defaults, cubeMask, dict);
		
		getColorDefault(defaults, darkSquaresColor, dict);
		getColorDefault(defaults, lightSquaresColor, dict);
		getColorDefault(defaults, ratedColor, dict);
		getColorDefault(defaults, unratedColor, dict);
		getColorDefault(defaults, terminalBackColor, dict);
		getColorDefault(defaults, terminalForeColor, dict);
		getColorDefault(defaults, seekBackColor, dict);
		getColorDefault(defaults, seekForeColor, dict);
		
		getFontDefault(defaults, terminalFont, dict);
	
		return dict;
	}
	
	public void commitDisplayedValues() {
		curValues = new NSDictionary(displayedValues);
	}
	
	public void discardDisplayedValues() {
		displayedValues = new NSMutableDictionary(curValues);
		updateUserInterface();
	}
	
	public void revertToDefault() {
		curValues = defaultValues();
		discardDisplayedValues();
	}
	
	public void revert() {
		discardDisplayedValues();
	}
	
	public static void setIntegerDefault(NSUserDefaults defaults, String name, NSDictionary dict) {
		if (defaultValues().objectForKey(name).equals(dict.objectForKey(name))) {
			defaults.removeObjectForKey(name);
		} else {
			defaults.setIntegerForKey(((Integer)dict.objectForKey(name)).intValue(), name);
		}
	}
	
	public static void setColorDefault(NSUserDefaults defaults, String name, NSDictionary dict) {
		if (defaultValues().objectForKey(name).equals(dict.objectForKey(name))) {
			defaults.removeObjectForKey(name);
		} else {
			defaults.setObjectForKey(NSArchiver.archivedDataWithRootObject((NSColor)dict.objectForKey(name)), name);
		}
	}
	
	public static void setFontDefault(NSUserDefaults defaults, String name, NSDictionary dict) {
		if (defaultValues().objectForKey(name).equals(dict.objectForKey(name))) {
			defaults.removeObjectForKey(name);
		} else {
			defaults.setObjectForKey(NSArchiver.archivedDataWithRootObject((NSFont)dict.objectForKey(name)), name);
		}
	}
	
	public static void getIntegerDefault(NSUserDefaults defaults, String name, NSMutableDictionary dict) {
		Object obj = defaults.objectForKey(name);
		if (obj != null) {
			dict.setObjectForKey(new Integer(defaults.integerForKey(name)), name);
		} else {
			dict.setObjectForKey(defaultValues().objectForKey(name), name);
		}
	}
	
	public static void getColorDefault(NSUserDefaults defaults, String name, NSMutableDictionary dict) {
		Object obj = defaults.objectForKey(name);
		if (obj != null) {
			dict.setObjectForKey((NSColor)(NSUnarchiver.unarchiveObjectWithData(defaults.dataForKey(name))), name);
		} else {
			dict.setObjectForKey(defaultValues().objectForKey(name), name);
		}
	}

	public static void getFontDefault(NSUserDefaults defaults, String name, NSMutableDictionary dict) {
		Object obj = defaults.objectForKey(name);
		if (obj != null) {
			dict.setObjectForKey((NSFont)(NSUnarchiver.unarchiveObjectWithData(defaults.dataForKey(name))), name);
		} else {
			dict.setObjectForKey(defaultValues().objectForKey(name), name);
		}
	}
	
	public static void savePreferencesToDefaults(NSDictionary dict) {
		NSUserDefaults defaults = NSUserDefaults.standardUserDefaults();
		setIntegerDefault(defaults, blitzPref, dict);
		setIntegerDefault(defaults, crazyhousePref, dict);
		setIntegerDefault(defaults, suicidePref, dict);
		setIntegerDefault(defaults, wildPref, dict);
		setIntegerDefault(defaults, flipBoard, dict);
		setIntegerDefault(defaults, mSound, dict);
		setIntegerDefault(defaults, chessSet, dict);
		setIntegerDefault(defaults, seekingTimeout, dict);
		setIntegerDefault(defaults, boardSize, dict);
		setIntegerDefault(defaults, tSound, dict);
		setIntegerDefault(defaults, aalias, dict);
		setIntegerDefault(defaults, aFlag, dict);
		setIntegerDefault(defaults, cShouts, dict);
		setIntegerDefault(defaults, gTells, dict);
		setIntegerDefault(defaults, serverTime, dict);
		setIntegerDefault(defaults, Shouts, dict);
		setIntegerDefault(defaults, cubeMask, dict);
		
		setColorDefault(defaults, darkSquaresColor, dict);
		setColorDefault(defaults, lightSquaresColor, dict);
		setColorDefault(defaults, terminalBackColor, dict);
		setColorDefault(defaults, terminalForeColor, dict);
		setColorDefault(defaults, ratedColor, dict);
		setColorDefault(defaults, unratedColor, dict);
		setColorDefault(defaults, seekBackColor, dict);
		setColorDefault(defaults, seekForeColor, dict);
		
		setFontDefault(defaults, terminalFont, dict);
	}
	
	//	--------------------------------------------------
	//	Set default Values
	//	--------------------------------------------------
	private static NSDictionary defaultValues() {
		if (defaultValues == null) {
			NSMutableDictionary dict = new NSMutableDictionary();
			
			dict.setObjectForKey(new Integer(NSCell.OnState), blitzPref);
			dict.setObjectForKey(new Integer(NSCell.OnState), crazyhousePref);
			dict.setObjectForKey(new Integer(NSCell.OnState), suicidePref);
			dict.setObjectForKey(new Integer(NSCell.OnState), wildPref);
			dict.setObjectForKey(new Integer(NSCell.OnState), flipBoard);
			dict.setObjectForKey(new Integer(NSCell.OnState), aFlag);
			dict.setObjectForKey(new Integer(NSCell.OnState), cShouts);
			dict.setObjectForKey(new Integer(NSCell.OnState), gTells);
			dict.setObjectForKey(new Integer(NSCell.OnState), Shouts);
			dict.setObjectForKey(new Integer(NSCell.OffState), cubeMask);
			
			dict.setObjectForKey(new Integer(6), mSound);
			dict.setObjectForKey(new Integer(0), chessSet);
			dict.setObjectForKey(new Integer(0), seekingTimeout);
			dict.setObjectForKey(new Integer(0), boardSize);
			dict.setObjectForKey(new Integer(10), tSound);
			dict.setObjectForKey(new Integer(2), aalias);
			dict.setObjectForKey(new Integer(0), serverTime);
			
			dict.setObjectForKey(NSColor.colorWithCalibratedRGB(119f/255f,162f/255f,109f/255f, 1f), darkSquaresColor);
			dict.setObjectForKey(NSColor.colorWithCalibratedRGB(200f/255f,195f/255f,101f/255f, 1f), lightSquaresColor);
			dict.setObjectForKey(NSColor.whiteColor(), terminalBackColor);
			dict.setObjectForKey(NSColor.blackColor(), terminalForeColor);
			dict.setObjectForKey(NSColor.redColor(), ratedColor);
			dict.setObjectForKey(NSColor.blueColor(), unratedColor);
			dict.setObjectForKey(NSColor.whiteColor(), seekBackColor);
			dict.setObjectForKey(NSColor.blackColor(), seekForeColor);
			
			dict.setObjectForKey(NSFont.fontWithNameAndSize("Monaco", 9f), terminalFont);
			
			defaultValues = new NSDictionary(dict);
		}
		return defaultValues;
	}
//	============================================================================================
//	These methods return specific prefs. Coded this way to simplify any conversion that is 
//	necessary for the stored prefs to be useful to the other classes
//	============================================================================================
	//	--------------------------------------------------
	//	getBoardSize: returns the board size
	//	--------------------------------------------------
	public static float getBoardSize() {
		float sizes[] = { 48f, 52f, 56f, 60f, 64f, 68f, 72f, 76f, 80f, 84f, 88f, 92f, 96f };
		return sizes[((Number)sharedInstance.preferences().objectForKey(boardSize)).intValue()];
	}
	
	//	--------------------------------------------------
	//	intValueForKey: returns the pref in integer form
	//	--------------------------------------------------
	public static int intValueForKey(Object key) {
		return ((Number)sharedInstance.preferences().objectForKey(key)).intValue();
	}
	
	//	--------------------------------------------------
	//	colorValueForKey: returns the color pref	
	//	--------------------------------------------------
	public static NSColor colorValueForKey(Object key) {
		return ((NSColor)sharedInstance.preferences().objectForKey(key));
	}
	
	//	--------------------------------------------------
	//	booleanValueForKey: returns the boolean
	//	--------------------------------------------------
	public static boolean booleanValueForKey(Object key) {
		return (((Number)sharedInstance.preferences().objectForKey(key)).intValue() == NSCell.OnState) ? true : false;
	}
	
	//	--------------------------------------------------
	//	aliasValue: returns the anti-alias pref
	//	--------------------------------------------------
	public static int aliasValue() {
		switch (((Number)sharedInstance.preferences().objectForKey(aalias)).intValue()) {
			case 0: return NSGraphicsContext.ImageInterpolationNone;
			case 1: return NSGraphicsContext.ImageInterpolationLow;
			case 2: return NSGraphicsContext.ImageInterpolationHigh;
			default: return NSGraphicsContext.ImageInterpolationHigh;
		}
	}
	
	//	--------------------------------------------------
	//	getFontForKey: returns the font
	//	--------------------------------------------------
	public static NSFont getFontForKey(Object key) {
		return ((NSFont)sharedInstance.preferences().objectForKey(key));
	}
	
	//	--------------------------------------------------
	//	returnPieceChoice: returns the piece choice
	//	--------------------------------------------------
	public static String returnPieceChoice() {
		return String.valueOf(((Integer)sharedInstance.preferences().objectForKey(chessSet)).intValue());
	}

	//	--------------------------------------------------
	//	getSquares:	returns the board array
	//	--------------------------------------------------
	public static NSRect[][] returnSquares() {
		return sharedInstance.squares;
	}
	
	//	--------------------------------------------------
	//	returnChessSet:	returns the chesspiece array
	//	--------------------------------------------------
	public static NSArray returnChessSet() {
		return (NSArray)sharedInstance.cpieces;
	}
	
	//	--------------------------------------------------
	//	getMySound:	returns mySound
	//	--------------------------------------------------
	public static NSSound getMySound() {
		if (sharedInstance.intValueForKey(mSound) != 0) 
			return NSSound.soundNamed(sharedInstance.mySound.itemTitleAtIndex(sharedInstance.intValueForKey(mSound)));
		else return null;
	}

	//	--------------------------------------------------
	//	getTheirSound:	returns their Sound
	//	--------------------------------------------------
	public static NSSound getTheirSound() {
		if (sharedInstance.intValueForKey(tSound) != 0) 
			return NSSound.soundNamed(sharedInstance.theirSound.itemTitleAtIndex(sharedInstance.intValueForKey(tSound)));
		else return null;
	}

	//	--------------------------------------------------
	//	saveDefaults: saves to defaults
	//	--------------------------------------------------
	public static void saveDefaults() {
		if (sharedInstance != null) {
			MyPreferences.savePreferencesToDefaults(sharedInstance.preferences());
		}
	}
	
	public void saveToDefaults() {
		if (sharedInstance != null) {
			changePrefs(this);
			commitDisplayedValues();
			MyPreferences.savePreferencesToDefaults(sharedInstance.preferences());
		}
	}
	
	public static MyPreferences sharedInstance() {
		if (sharedInstance == null) new MyPreferences();
		return sharedInstance;
	}
	
	//	-------------------------------------------------------------------------
	//	setView(int):	sets the view according to the toolbar icon pressed
	//	-------------------------------------------------------------------------
	public void setView(int which) {
		NSView temp;
		switch (which) {
			case 0:
				temp = generalPanel;
				break;
			case 1:
				temp = boardPanel;
				break;
			case 2:
				temp = terminalPanel;
				break;
			case 3:
				temp = seekPanel;
				break;
			default:
				temp = generalPanel;
				break;
		}
		
		//	Get our original information about the Preference Window
		NSRect viewRect = temp.frame();
		NSRect windowRect = theWindow.frame();
		float h = toolbarHeightForWindow(theWindow);
		float x = windowRect.origin().x();
		float y = windowRect.origin().y();
		
		//	Save the top left point
		NSPoint tl = new NSPoint(x, windowRect.origin().y() + windowRect.height());
		
		//	Switch to minimum size after loading the "Loading..." panel
		theWindow.setContentView(loadingPanel);
		theWindow.setFrameTopLeftPoint(tl);
		NSSize contsize = new NSSize(viewRect.width(), viewRect.height() + h);
		NSPoint point = new NSPoint(x, tl.y() - viewRect.height() - h) ;
		NSRect frame = NSWindow.frameRectForContentRect(new NSRect(point, contsize), theWindow.styleMask());
		float diff = tl.y() - frame.height();
			
		NSRect finalFrame = new NSRect(frame.x(), diff, frame.width(), frame.height());
		theWindow.setFrame(finalFrame, true, true);
		theWindow.setContentView(temp);
	}
		
	//	-------------------------------------------------------------------------
	//	setFont(Object):	called from button. Displays font panel
	//	-------------------------------------------------------------------------
    public void setFont(Object sender) { /* IBAction */
    	(fontName.window()).makeFirstResponder(fontName.window());
    	NSFontManager.sharedFontManager().setSelectedFont((NSFont)curValues.objectForKey(terminalFont), false);
      	NSFontManager.sharedFontManager().orderFrontFontPanel(this);
  	}
    
	//	-------------------------------------------------------------------------
	//	changeFont(Object):	called from the font panel. Works because we are the
	//	window's delegate and it is the first responder
	//	-------------------------------------------------------------------------
    public void changeFont(Object sender) {
    	NSFontManager fontManager = (NSFontManager)sender;
    	displayedValues.setObjectForKey(fontManager.convertFont((NSFont)curValues.objectForKey(terminalFont)), terminalFont);
    	showFontInField((NSFont)displayedValues.objectForKey(terminalFont));
  		showNewTerm(this);
   }	

	//	-------------------------------------------------------------------------
	//	showNewBoard(Object):	called from any pref that changes board prefs
	//	-------------------------------------------------------------------------
    public void showNewBoard(Object sender) { /* IBAction */
    	changePrefs(this);
		examBoard.getImages(pieces.indexOfSelectedItem());
		examBoard.display();
		updateBoard();		
    }

	//	-------------------------------------------------------------------------
	//	showNewTerm(Object):	called by any changes to the terminal prefs
	//	-------------------------------------------------------------------------
    public void showNewTerm(Object sender) { /* IBAction */
    	changePrefs(this);
  		examTerm.setSelectable(true);
  		examTerm.setEditable(true);
  		examTerm.setBackgroundColor((NSColor)displayedValues.objectForKey(terminalBackColor));
  		examTerm.setSelectedRange(new NSRange(0, examTerm.string().length()));
 		examTerm.setTextColor((NSColor)displayedValues.objectForKey(terminalForeColor));
  		examTerm.setFont((NSFont)displayedValues.objectForKey(terminalFont));
 		examTerm.setSelectedRange(new NSRange(0,0));
 		examTerm.setEditable(false);
  		examTerm.setSelectable(false);
    }

	//	-------------------------------------------------------------------------
	//	Following methods all show prefs according to toolbar item pressed
	//	-------------------------------------------------------------------------
	public void showBoardPrefs() {
		setView(1);
	}
	
	public void showTermPrefs() {
		setView(2);
	}
	
	public void showSeekPrefs() {
		setView(3);
	}
	
	public void showGeneralPrefs() {
		setView(0);
	}
	
	public void doDefaults() {
		revertToDefault();
	}
	
	public void updateBoard() {
		//	Create Board Array
		float squareSize = 48f;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				squares[i][j] = new NSRect((i * squareSize), (j * squareSize), squareSize, squareSize);
			}
		}
		cpieces = new NSMutableArray();
		for (int i = 0; i < 12; i++) {
			NSImage tempImage = new NSImage(NSBundle.mainBundle().pathForResource(imageName[i]+returnPieceChoice(), "tif"), false);
			NSImage newImage = new NSImage(new NSSize(squareSize,squareSize));
			tempImage.setScalesWhenResized(true);
			tempImage.setSize(new NSSize(squareSize,squareSize));
				
			newImage.lockFocus();
			NSGraphicsContext.currentContext().setImageInterpolation(aliasValue());
			tempImage.compositeToPoint(new NSPoint(0f,0f), NSImage.CompositeSourceOver);
			newImage.unlockFocus();
			cpieces.addObject(newImage);
		}
	}

	//	-------------------------------------------------------------------------
	//	method:		isSquareBlack():	int, int
	//	returns boolean whether the square is black
	//	-------------------------------------------------------------------------
	public boolean isSquareBlack(int x, int y) {
		int remainx = (x % 2);
		int remainy = (y % 2);
		boolean black = (remainx == remainy) ? true : false;
		return black;
	}
	
//	============================================================================================
//	Toolbar Delegate operations
//	============================================================================================
	public boolean validateToolbarItem(NSToolbarItem theItem) {
		return true;
	}
	
	public NSToolbarItem toolbarItemForItemIdentifier( NSToolbar toolbar, String itemIdentifier, boolean flag) {
		NSToolbarItem item = null;
		if (itemIdentifier.equals("Board")) {
			NSImage boardImage = new NSImage(NSBundle.mainBundle().pathForResource("Board", "tif"), false);
			item = new NSToolbarItem("Board");
			item.setImage(boardImage);
			item.setLabel("Board");
			item.setAction(new NSSelector("showBoardPrefs", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		
		if (itemIdentifier.equals("Terminal")) {
			NSImage terminalImage = new NSImage(NSBundle.mainBundle().pathForResource("Terminal", "tif"), false);
			item = new NSToolbarItem("Terminal");
			item.setImage(terminalImage);
			item.setLabel("Terminal");
			item.setAction(new NSSelector("showTermPrefs", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		if (itemIdentifier.equals("Seek")) {
			NSImage seekImage = new NSImage(NSBundle.mainBundle().pathForResource("Seek", "tif"), false);
			item = new NSToolbarItem("Seek");
			item.setImage(seekImage);
			item.setLabel("Seek Graph");
			item.setAction(new NSSelector("showSeekPrefs", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		if (itemIdentifier.equals("General")) {
			NSImage generalImage = new NSImage(NSBundle.mainBundle().pathForResource("General", "tif"), false);
			item = new NSToolbarItem("General");
			item.setImage(generalImage);
			item.setLabel("General");
			item.setAction(new NSSelector("showGeneralPrefs", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		if (itemIdentifier.equals("Defaults")) {
			NSImage defaultsImage = new NSImage(NSBundle.mainBundle().pathForResource("Defaults", "tif"), false);
			item = new NSToolbarItem("Defaults");
			item.setImage(defaultsImage);
			item.setLabel("Defaults");
			item.setAction(new NSSelector("doDefaults", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		if (itemIdentifier.equals("Save")) {
			NSImage saveImage = new NSImage(NSBundle.mainBundle().pathForResource("Save","tif"), false);
			item = new NSToolbarItem("Save");
			item.setImage(saveImage);
			item.setLabel("Save");
			item.setAction(new NSSelector("saveToDefaults", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		if (itemIdentifier.equals("Revert")) {
			NSImage revertImage = new NSImage(NSBundle.mainBundle().pathForResource("Revert","tif"), false);
			item = new NSToolbarItem("Revert");
			item.setImage(revertImage);
			item.setLabel("Revert");
			item.setAction(new NSSelector("discardDisplayedValues", new Class[] {} ));
			item.setTarget(this);
			item.setEnabled(true);
		}
		
		return item;
	}
	
	public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new String[] {"General", "Board","Terminal","Seek","Save","Revert","Defaults",
			NSToolbarItem.SeparatorItemIdentifier, NSToolbarItem.FlexibleSpaceItemIdentifier });
	}
		
	public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
		return new NSArray (new String[] {NSToolbarItem.FlexibleSpaceItemIdentifier,
										"General", "Board","Terminal","Seek",
										NSToolbarItem.FlexibleSpaceItemIdentifier,
										NSToolbarItem.SeparatorItemIdentifier,
										"Revert","Defaults" });
	}
	
    public float toolbarHeightForWindow(NSWindow window)
    {
        NSToolbar toolbar = window.toolbar();
        float toolbarHeight = (float)0.0;
        
        if(toolbar != null && toolbar.isVisible())
        {
            NSRect windowFrame = NSWindow.contentRectForFrameRect(
                                 window.frame(), window.styleMask());
            toolbarHeight = windowFrame.height()
                            - window.contentView().frame().height();
        }
        
        return toolbarHeight;
    }
    
}
