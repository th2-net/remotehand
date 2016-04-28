1. List of actions
Here is the list of currently supported actions of RemoteHand with possible parameters and descriptions.
Some useful info can be found below the list.


#action,#url
Open,url

Opens resource at <url>.


#action,#wait,#locator,#matcher
Click,5,locator,matcher

Optionally waits <wait> seconds for element specified by <matcher> and clicks on it. Element is found using <locator>.


#action,#wait,#locator,#matcher,#checked
SetCheckbox,5,locator,matcher,true

Instead of Click this action checks state of the checkbox before clicking. 
If #checked = true and checkbox is checked action will not click to element. 
You can clear checkbox using #checked = false (n, no, f, 0, -). Default value is true (y, yes, t, 1, +).


#action,#wait,#locator,#matcher,#text,#wait2,#locator2,#matcher2,#text2
SendKeys,5,locator,matcher,text,5,locator2,matcher2,text2

Optionally waits <wait> seconds for element specified by <matcher> and sends keys to it, inputing <text>. Element is found using <locator>.
In some cases it is needed to wait for some element to appear after first input and then continue the input 
(for example, need to wait for autocomplete box to appear). 
In these cases optional parameters <wait2>, <locator2>, <matcher2> and <text2> come in handy: 
after inputing <text>, RemoteHand will wait <wait2> seconds for element specified by <matcher2> and then continue the input, inputing <text2>.
Element to wait is found using <locator2>. 
Also, you can skip <locator2> and <matcher2> and RemoteHand will just wait <wait2> seconds before continuing the input with <text2>.

You can tell RemoteHand to press the following special keys by specifying corresponding codes:
1. Up arrow - #up#
2. Down arrow - #down#
3. Left arrow - #left#
4. Right arrow - #right#
5. Enter - #return# or #enter#
6. Space - #space#
7. "#" char (Shift+3) - #hash#
8. "$" char (Shift+4) - #dollar#
9. "%" char (Shift+5) - #percent#
10. Tab - #tab#
11. Shift - #shift#
12. Ctrl - #ctrl#
13. Alt - #alt#
14. Escape - #esc#
15. End (for example, to move to the end of the line) - #end#
16. Home (for example, to move to the start of the line) - #home#
17. Insert - #insert#
18. Delete - #delete#
19. Functional keys, i.e. F1, F2 etc. - #f1#, #f2# etc.
20. Numpad's keys - num0, num1, ..., num9
21. Non-breaking space (Alt+0160, only for Windows) - #nbsp#

The following example writes "text" in a field, then presses Down arrow to select something in a dropdown list, then presses Enter key to confirm the selection:
text#down##return#

The keys and special codes can be combined in order to press a combination of keys. The combination is specified in "#" chars with keys delimited by "+". For example, to simulate input of '(' char you can use the following:
#shift+9#


#action,#seconds,#locator,#matcher
WaitForElement,5,locator,matcher

Waits <seconds> seconds for element specified by <matcher>, element is found using <locator>.
Useful to make sure that certain element has appeared (page has been loaded till this element) before doing something on the page.


#action,#seconds,#checkmillis,#locator,#matcher
WaitForNew,5,300,locator,matcher

Waits <seconds> seconds for new elements specified by <matcher> to appear, elements are found using <locator>.
Check for new elements is performed every <checkmillis> milliseconds.


#action,#seconds
Wait,5

Makes a pause in execution for <seconds> seconds. 
Good to use this action when you're not sure which element to wait for and just want to give a page more time to be downloaded.


#action,#wait,#locator,#matcher
GetElement,5,locator,matcher

Optionally waits <wait> seconds for element specified by <matcher> and prints value of its outerHTML. Element is found using <locator>.


#action,#wait,#locator,#matcher
GetDynamicTable,5,locator,matcher

Optionally waits <wait> seconds for table element specified by <matcher> and prints its contents as <tbody>, <tr> and <td> tags. Table element is found using <locator>.
If possible, action scrolls the table down to force it to be loaded completelly if lazy loading is enabled for the table.


#action,#wait,#locator,#matcher
ScrollTo,5,locator,matcher

Optionally waits <wait> seconds for element specified by <matcher> and scrolls browser window to it. Element is found using <locator>.
Note that all of actions working with elements (Click, SendKeys, etc.) will scroll to the element automatically if it is invisible.


#action
PageSource

Prints current URL, title and source code of the page.
Useful for debugging, because source code of the page will contain elements seen only in browser window, JavaScript, Firebug, but not seen when the page is saved.


#action
Refresh

Refreshes current page, loading it again. Useful to collapse all tree nodes and other elements, which state is affected by page refresh.


#action,#wait,#locator,#matcher
ClearElement,5,locator,matcher

Optionally waits <wait> seconds for element specified by <matcher> and clears its value. Element is found using <locator>.
Useful for input controls.


#action,#text
Output,text

Prints <text> to script output. Useful to divide outputs of actions like GetElement, GetDynamicTable etc.


#action,#wait,#locator,#matcher,#id
FindElement,5,locator,matcher,controlID

Optionally waits <wait> seconds for element specified by <matcher> and <locator>. If element is found, "<id>=found" is printed in output. Else "<id>=notfound" is printed.


2. Notes
All actions that wait for some element will check the page contents till given element appears or till given number of seconds passes, throwing ScriptExecuteException if no element had appeared in time. 
The exception can be supressed and further action execution can be skipped if NotFoundFail action parameter is not true (y, yes, t, 1, +)
In case of FindElement, no exception is thrown because the action handles this case.

All actions support optional parameter #execute. If this parameter is set to false (n, no, f, 0, -), action will not be executed.
Default value is true.

Possible <locator> values:
1. cssSelector - in this case <matcher> should contain CSS path to the element like this: div.v-button.v-widget.default.v-button-default;
2. xpath - <matcher> should contain XPath to the element, for example: //button[contains(@class, 'v-nativebutton v-widget')];
3. tagName - in this case <matcher> is a tag name of the element;
4. id - then <matcher> contains id of the element.

You can specify non-breaking space (&nbsp;) in xpath using special value #nbsp#. For example: //span[text()="#nbsp#"].

RemoteHand can use web element dictionary, which contains list of web elements by tags: #webId,#locator,#matcher,#type,#desc.
Example:
#webId,#locator,#matcher,#type,#desc
click1,xpath,<matcher>,button,some button...
click2,cssSelector,<matcher>,link,some link...
//you can specify comment
sendKeys1,xpath,<matcher>,input,some input...

You can use in script the parameter 'webId', RemoteHand will replace it with 'locator' and 'matcher'. But these parameters can not be used together!
