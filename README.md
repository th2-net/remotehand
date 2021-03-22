RemoteHand
==========

This is the official RemoteHand project repository.

## About RemoteHand

RemoteHand is a tool to automate web GUI actions.

It is written in Java and uses Selenium to interact with web GUI via browser.

RemoteHand can be used by supplying it a script to execute. Or it can receive scripts from external source application.

Anyway, scripts are multiheader CSV files similar to (but not the same as) ClearTH matrices.


## Starting RemoteHand

RemoteHand is written in Java and is started as usual Java application:
```
java -jar remotehand.jar
```

There are 3 modes for RemoteHand to work: 
1. to execute actions from given script file;
1. to execute incoming commands using local web driver;
1. to execute incoming commands using remote web driver.

The last 2 modes turn RemoteHand into an HTTP server. Commands to execute will come from external application.

To start RemoteHand in 1st mode, use the following command:
```
java -jar remotehand.jar -input <script file> -output <file for script output>
```

Optional parameter is:
```
-dynamicinput <additional script file>
```

It makes RemoteHand wait for new script to arrive in specified file. After executing this script, RemoteHand will remove it and wait for new one.


To start RemoteHand in 2nd mode, use the following command:
```
java -jar remotehand.jar -httpserver
```

To make RemoteHand work in 3rd mode, use the following command:
```
java -jar remotehand.jar -grid
```

In these modes RemoteHand will wait for script from HTTP request and send its output as HTTP response. 

RemoteHand will use "Port" setting from config.ini to start HTTP server on.

These modes are usual for external application to trigger RemoteHand to operate.

Alternatively, you can make RemoteHand connect to external application to serve further requests:
```
java -jar remotehand.jar -tcpclient
```

In this mode RemoteHand connects to another application via TCP/IP. The application after that can trigger RemoteHand to operate using connected socket.

RemoteHand will use "Host" and "Port" settings from config.ini to connect to.


## Configuration

RemoteHand can be configured by using config.ini file.

Alternative configuration file can be specified in optional -config parameter like this:
```
java -jar remotehand.jar -input <script file> -output <file for script output> -config <path to config>
```

## WebP image encoding
[Here](https://github.com/th2-net/remotehand/blob/master/www/WebpLibrary.MD) you can see how to use webp library to encode images.

## A word about Selenium drivers

RemoteHand uses Selenium to interact with web GUI, thus it needs a browser available for automation.

Automation via Selenium is done by using special drivers, sometimes specific for particular version of browser.

Please use the following URL to find the latest drivers for your browser:
https://www.seleniumhq.org/download/



## List of actions

Here is the list of currently supported actions of RemoteHand with possible parameters and descriptions.

Some useful info can be found below the list.


### Open
```
#action,#url
Open,url
```

Opens resource at <url>.


### Click
```
#action,#wait,#locator,#matcher,#button,#xoffset,#yoffset,#modifiers
Click,5,locator,matcher,mouse_button,10,10,"ctrl,shift"
```

Optionally waits <wait> seconds for element specified by <matcher> and clicks on it with <button> ("left" by default). Element is found using <locator>.

Optional <xoffset> and <yoffset> parameters define position from element top left corner to click on.

Optional <modifiers> parameter is expected to be enumeration of modifier keys, e.g.: "ctrl,shift,alt"

Possible values of <button>: left, right, middle, double.


### SetCheckbox
```
#action,#wait,#locator,#matcher,#checked
SetCheckbox,5,locator,matcher,true
```

Instead of Click this action checks state of the checkbox before clicking. 

If #checked = true and checkbox is checked action will not click to element. 

You can clear checkbox using #checked = false (n, no, f, 0, -). Default value is true (y, yes, t, 1, +).


### SendKeys
```
#action,#wait,#locator,#matcher,#text,#wait2,#locator2,#matcher2,#text2,#CanBeDisabled,#Clear,#CheckInput,#NeedClick
SendKeys,5,locator,matcher,text,5,locator2,matcher2,text2,yes,yes,no,no
```

"Clear" parameter can be true or yes when we want to clear element before sending.

Optionally waits <wait> seconds for element specified by <matcher> and sends keys to it, inputting <text>. Element is found using <locator>.

In some cases it is needed to wait for some element to appear after first input and then continue the input 
(for example, need to wait for autocomplete box to appear). 

In these cases optional parameters <wait2>, <locator2>, <matcher2> and <text2> come in handy: 
after inputing <text>, RemoteHand will wait <wait2> seconds for element specified by <matcher2> and then continue the input, inputing <text2>.

Element to wait is found using <locator2>. 

Also, you can skip <locator2> and <matcher2> and RemoteHand will just wait <wait2> seconds before continuing the input with <text2>.

If you want to fill disabled field use #CanBeDisabled=yes. Field will be enabled and disabled back after text input.

If you want to send keys without checking sending result value use #CheckInput=no ('yes' by default).

If you want to send keys without clicking on element first use #NeedClick=no ('yes' by default).

You can tell RemoteHand to press the following special keys by specifying corresponding codes:
1. Up arrow - #up#
1. Down arrow - #down#
1. Left arrow - #left#
1. Right arrow - #right#
1. Enter - #return# or #enter#
1. Space - #space#
1. "#" char (Shift+3) - #hash#
1. "$" char (Shift+4) - #dollar#
1. "%" char (Shift+5) - #percent#
1. Tab - #tab#
1. Shift - #shift#
1. Ctrl - #ctrl#
1. Alt - #alt#
1. Escape - #esc#
1. End (for example, to move to the end of the line) - #end#
1. Home (for example, to move to the start of the line) - #home#
1. Insert - #insert#
1. Delete - #delete#
1. Functional keys, i.e. F1, F2 etc. - #f1#, #f2# etc.
1. Numpad keys - num0, num1, ..., num9
1. Non-breaking space (Alt+0160, only for Windows) - #nbsp#
1. Backspace - #backspace# 

The following example writes "text" in a field, then presses Down arrow to select something in a dropdown list, then presses Enter key to confirm the selection:
```
text#down##return#
```

The keys and special codes can be combined in order to press a combination of keys. The combination is specified in "#" chars with keys delimited by "+". For example, to simulate input of '(' char you can use the following:
```
#shift+9#
```

Special keys (Shift, Ctrl, Alt) can be hold during keys sending. To hold key you can place it in round brackets (ex. '(#shift#)a' will cause pressing key 'a' with shift holded).


### WaitForElement
```
#action,#seconds,#locator,#matcher
WaitForElement,5,locator,matcher
```

Waits <seconds> seconds for element specified by <matcher>, element is found using <locator>.

Useful to make sure that certain element has appeared (page has been loaded till this element) before doing something on the page.


### WaitForNew
```
#action,#seconds,#checkmillis,#locator,#matcher
WaitForNew,5,300,locator,matcher
```

Waits <seconds> seconds for new elements specified by <matcher> to appear, elements are found using <locator>.

Check for new elements is performed every <checkmillis> milliseconds.


### Wait
```
#action,#seconds
Wait,5
```

Makes a pause in execution for <seconds> seconds. 

Good to use this action when you're not sure which element to wait for and just want to give a page more time to be downloaded.


### GetElement
```
#action,#wait,#locator,#matcher
GetElement,5,locator,matcher
```

Optionally waits <wait> seconds for element specified by <matcher> and prints value of its outerHTML. Element is found using <locator>.

Optional parameter <id> defines identifier to make references to element text in next actions: if <id>="Button", @{Button} is a reference to text of found element.

### GetElementInnerHtml

The same as GETELEMENT but retrieves innerHTML text of the node.

### GetElementValue
```
#action,#wait,#locator,#matcher
GetElementValue,5,locator,matcher
```

Allows to obtain element value. For example value of tag <input>.

### GetElementValue
```
#action,#wait,#locator,#matcher,#attribute
GetElementValue,5,locator,matcher,class
```

Allows to obtain element's attribute value. For example, class of element.


### GetDynamicTable
```
#action,#wait,#locator,#matcher
GetDynamicTable,5,locator,matcher
```

Optionally waits <wait> seconds for table element specified by <matcher> and prints its contents as <tbody>, <tr> and <td> tags. Table element is found using <locator>.

If possible, action scrolls the table down to force it to be loaded completelly if lazy loading is enabled for the table.


### ScrollTo
```
#action,#wait,#locator,#matcher
ScrollTo,5,locator,matcher
```

Optionally waits <wait> seconds for element specified by <matcher> and scrolls browser window to it. Element is found using <locator>.

Note that all of actions working with elements (Click, SendKeys, etc.) will scroll to the element automatically if it is invisible.


### ScrollDivTo
```
#action,#wait,#locator,#matcher,#wait2,#locator2,#matcher2,#yoffset
ScrollDivTo,5,locator,matcher,5,locator2,matcher2,-100
```

Optionally waits <wait> seconds for element specified by <matcher>, this element is found using <locator>. Scroll of this element will be manipulated by this action.

Then optionally waits <wait2> seconds for element specified by <matcher2>, this element is found using <locator2>. Element with scroll-bar will be scrolled to 'offsetTop' of this element.

Action can optionally move scroll by <yoffset> (positive or negative integer) pixels after scrolling to specified element.


### ScrollDivUntil
```
#action,#wait,#locator,#matcher,#wait2,#locator2,#matcher2,#searchdir,#searchoffset,#doscrollto,#yoffset
ScrollDivUntil,5,locator,matcher,5,locator2,matcher2,both,300,y,-100
```

Optionally waits <wait> seconds for element specified by <matcher>, this element is found using <locator>. Scroll of this element will be manipulated by this action.

Then optionally waits <wait2> seconds for element specified by <matcher2>, this element is found using <locator2>. Previous element will scroll until this element will appear.

Search can be performed in different directions, user can control it with <searchdir> variable. Possible values: 'up', 'down' and 'both'.

<searchoffset> is number of pixels to scroll on each searching iteration (100 by default).

If logical variable <doscrollto> is TRUE then action will perform same functional, as SCROLLDIVTO: element with scroll-bar will be scrolled to 'offsetTop' of this element with <yoffset> offset.


### PageSource
```
#action
PageSource
```

Prints current URL, title and source code of the page.

Useful for debugging, because source code of the page will contain elements seen only in browser window, JavaScript, Firebug, but not seen when the page is saved.


### Refresh
```
#action
Refresh
```

Refreshes current page, loading it again. Useful to collapse all tree nodes and other elements, which state is affected by page refresh.


### Select
```
#action,#wait,#locator,#matcher,#text,#default,#nooptionalfail
Select,5,locator,matcher,selectedValue,defaultValue,true
```

Selects specified in selectedValue value of drop down list. If there is no specified value in the list default value will be
selected. If nooptionalfail is not in list ("y", "yes", "t", "true", "1", "+") action doesn't fail when selectedValue or default value are not founded.
By default nooptionalfail is true.


### ClearElement
```
#action,#wait,#locator,#matcher
ClearElement,5,locator,matcher
```

Optionally waits <wait> seconds for element specified by <matcher> and clears its value. Element is found using <locator>.

Useful for input controls.


### Output
```
#action,#text
Output,text
```

Prints <text> to script output. Useful to divide outputs of actions like GetElement, GetDynamicTable etc.


### FindElement
```
#action,#wait,#locator,#matcher,#id
FindElement,5,locator,matcher,controlID
```

Optionally waits <wait> seconds for element specified by <matcher> and <locator>. If element is found, "<id>=found" is printed in output. Else "<id>=notfound" is printed.


### KeyAction
```
#action,#key,#keyaction
KeyAction,#shift#,down
```

Allows to perform action over key specified for SendKeys action without reference to locator. Available values of #keyaction param: press (press key), down (hold key), up (release key).


### PressKey
```
#action,#key
PressKey,#tab#
```

Allows to send one of keys specified for SendKeys action without reference to locator. Equals to KeyAction with #keyaction=press.


### SendKeysToActive
```
#action,#text,#text2
SendKeysToActive,text,text2
```

Allows to send text to active element like SendKeys but without reference to locator.


### UploadFile
```
#action,#wait,#locator,#matcher,#absolutepath
UploadFile,5,locator,matcher,absolutepath
```

Uploads file by locator, using absolute path to the file or path relative to RemoteHand directory.


### Download

You are able to download files from GUI with this action. First of all you need to make a snapshot of a download directory.

To do this, call the action with #actiontype = snapshot. Then call the action with #actiontype = download.

If there are some new files in the download directory, action output will contain local path or HTTP link of the downloaded file.

The output depends on #localpath parameter. By default it is 'false' which results in HTTP link in the output.


### SwitchWindow
```
#action,#window
SwitchWindow,N
```

Switches the window. N is an order of opened window (e.g. 0 - is a parent window for all windows, 1 - next child window)


### CloseWindow
```
#action
CloseWindow
```

Closes currently opened window and focuses at the firstly opened window (0 window).


### GetCurrentURL
```
#action
GetCurrentURL
```

Returns URL of active open window


### SetZoom
```
#action,#value
SetZoom,140%
```

Sets zoom value for current page. Possible formats: 140% / 1.4 .

### DurationStart
```
#action,#id
DurationStart,Start1
```

Sets start of period to measure duration. ID is used for reference in further GetDuration actions.

### GetDuration
```
#action,#StartId,#Name
GetDuration,Start1,Period1
```

Returns duration of period from DurationStart action till this action. 

DurationStart action is taken by ID specified in optional StartId parameter. If no StartId is specified, last DurationStart action is taken.

Name parameter is used in action result. Example of result:
```
Duration Period1: 20275
```

### StoreElementState
```
#action,#wait,#locator,#matcher,#id
StoreElementState,5,locator,matcher,screen_id
```

Optionally waits `wait` seconds for element specified by `matcher` and takes its screenshot, storing it under `id`.
Element is found using `locator`.\
Screenshot is stored for further verification for changes via WaitForChanges action.


### WaitForChanges
```
#action,#seconds,#checkmillis,#locator,#matcher,#screenshotId
WaitForChanges,20,50,locator,matcher,screen_id
```

Waits `seconds` for changes in element specified by `matcher` and found by using `locator`.\
Changes are found by comparing element screenshot with the one stored under `id`.\
Check for changes is performed (i.e. new screenshot is taken) every `checkmillis` milliseconds.


### AcceptAlert
```
#action,#wait
AcceptAlert,5
```

Waits `wait` seconds for alert dialog to appear and accepts it, i.e. presses OK button.


### DismissAlert
```
#action,#wait
DismissAlert,5
```

Waits `wait` seconds for alert dialog to appeat and dismisses it, i.e. presses Cancel button.


### CheckImageAvailability
```
#action,#wait,#locator,#matcher
CheckImageAvailability,5,locator,matcher
```

This action checks that the specified image is available on page. 

Optionally waits `wait` seconds for <img src="img_source"...> element specified by <matcher> and <locator>. 

If element is found and the image is available, "true" is printed in output. Else "false" is printed.


### GetElementScreenshot
```
#action,#wait,#locator,#matcher,#name
GetElementScreenshot,5,locator,matcher,screen1
```

Takes a screenshot of the *element* and saves it to the disk.\
`name` is optional and will be used as a part of screenshot filename.


### GetScreenshot
```
#action,#name
GetScreenshot,screen1
```

Takes a screenshot of the *whole application* and saves it to the disk.\
`name` is optional and will be used as a part of screenshot filename.

## Notes

All actions that wait for some element will check the page contents till given element appears or till given number of seconds passes, throwing ScriptExecuteException if no element had appeared in time. 

The exception can be suppressed and further action execution can be skipped if NotFoundFail action parameter is not true (y, yes, t, 1, +)

In case of FindElement, no exception is thrown because the action handles this case.

All actions support optional parameter #execute. If this parameter is set to false (n, no, f, 0, -), action will not be executed.

Default value is true.

Possible <locator> values:
1. cssSelector - in this case <matcher> should contain CSS path to the element like this: div.v-button.v-widget.default.v-button-default;
1. xpath - <matcher> should contain XPath to the element, for example: //button[contains(@class, 'v-nativebutton v-widget')];
1. tagName - in this case <matcher> is a tag name of the element;
1. id - then <matcher> contains id of the element.

You can specify non-breaking space (&nbsp;) in xpath using special value #nbsp#. For example: //span[text()="#nbsp#"].

RemoteHand can use web element dictionary, which contains list of web elements by tags: #webId,#locator,#matcher,#type,#desc.

Example:
```
#webId,#locator,#matcher,#type,#desc
click1,xpath,<matcher>,button,some button...
click2,cssSelector,<matcher>,link,some link...
//you can specify comment
sendKeys1,xpath,<matcher>,input,some input...
```

You can use in script the parameter 'webId', RemoteHand will replace it with 'locator' and 'matcher'. But these parameters can not be used together!
