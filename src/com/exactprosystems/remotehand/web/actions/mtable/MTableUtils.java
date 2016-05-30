////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web.actions.mtable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.actions.WaitForElement;

/**
 * @author daria.plotnikova
 *
 */
public class MTableUtils
{
	private static final Logger logger = Logger.getLogger(MTableUtils.class);
	
	public static final String TABLE_ID = "tableid", 
			TABLE_INDEX = "tableindex",
			ROW = "row",
			COLUMN = "column",
			COLUMNS = "columns",
			COL_DELIMITER = ";";
	
	public static Map<String, String> getTableRow(WebDriver webDriver, String tableId, int tableIndex, int rowNo, List<String> colHeaders)
        {
        Map<String, String> map = new HashMap<String, String>();
        for (String col : colHeaders) 
        {
        	String val = getTableCellDataByColName(webDriver, tableId, tableIndex, rowNo, col);
            map.put(col, val);
        }
        return map;
    }
	
	public static Map<String, String> getTableRow(WebDriver webDriver, String tableId, int tableIndex, int rowNo)
       {
        Map<String, String> map = new HashMap<String, String>();
        List<String> colNames = getColNames(webDriver, tableId, tableIndex);
        map = getTableRow(webDriver, tableId, tableIndex, rowNo, colNames);
        return map;
    }
	
	public static List<String> getColNames(WebDriver webDriver, String tableId, int tableIndex)
	{
		ArrayList<String> colNames = new ArrayList<String>();
		int colCount = getTableColumnCount(webDriver, tableId, tableIndex);
		for (int i = 0; i < colCount; i++) 
        {
        	colNames.add(getColName(webDriver, tableId, tableIndex, i));
        }
		logger.info("Columns: " + colNames );
		return colNames;
	}
	
	public  static String getColName(WebDriver webDriver, String tableId, int tableIndex, int colInd)
	{
		runCommand(webDriver, "getColName:" + colInd, tableId, tableIndex);
        String val = webDriver.findElement(By.className("cmdOut")).getAttribute("value");
        logger.info("Col index: " + colInd + " name: " + val);
        return val;
	}

    public  static String getTableCellDataByColName(WebDriver webDriver, String tableId, int tableIndex, int rowNo, String colHeader) {
        runCommand(webDriver, "getCellDataByColName:" + rowNo + ":" + colHeader, tableId, tableIndex);
        String val = webDriver.findElement(By.className("cmdOut")).getAttribute("value");
        logger.info("Col: " + colHeader + " val: " + val);
        return val;
    }

    public  static String getTableCellDataByColId(WebDriver webDriver, String tableId, int tableIndex, int rowNo, int colId) {
        runCommand(webDriver, "getCellData:" + rowNo + ":" + colId, tableId, tableIndex);
        String val = webDriver.findElement(By.className("cmdOut")).getAttribute("value");
        logger.info("Col: " + colId + " val: " + val);
        return val;
    }


    public  static int getTableColumnCount(WebDriver webDriver, String tableId, int tableIndex) {
        runCommand(webDriver, "getColCount", tableId, tableIndex);
        String val = webDriver.findElement(By.className("cmdOut")).getAttribute("value");
        logger.info("Column count: " + val);
        return Integer.parseInt(val.equals("") ? "0" : val);
    }

    public  static int getTableRowCount(WebDriver webDriver, String tableId, int tableIndex) {
        runCommand(webDriver, "getRowCount", tableId, tableIndex);
        String val = webDriver.findElement(By.className("cmdOut")).getAttribute("value");
        logger.info("Row count: " + val);
        return Integer.parseInt(val.equals("") ? "0" : val);
    }

    public  static void rightClickTableRow(WebDriver webDriver, String tableId, String tableIndex, String rowNo) {
        String commandIn = "document.getElementById('" + tableId + "').getElementsByClassName('cmdIn')[" + tableIndex + "].value='rightClick:" + rowNo + "';";
        runJavaScript(webDriver, commandIn);
    }

    public  static void clickTableContextMenuLink(WebDriver webDriver, String menuItem) throws Exception {
    	WaitForElement.waitForElement(By.xpath("//div[@class='popupContent'])[last()]/div/table"), webDriver, 5);
        String xpath = "(//div[@class='popupContent'])[last()]/div/table/tbody/tr[?]/td";
        int index = 1;
        while (true) {
            String actualPath = xpath.replace("?", String.valueOf(index));
            String text = webDriver.findElement(By.xpath(actualPath)).getText();
            if (menuItem.equals(text)) {
            	webDriver.findElement(By.xpath(actualPath)).click();
                break;
            }
            index++;
        }
    }

    private  static void runCommand(WebDriver webDriver, String command, String tableId, int tableIndex) {
        String commandIn = "document.getElementById(\'" + tableId + "\').getElementsByClassName(\'cmdIn\')["
                + tableIndex + "].value=\'" + command + "\';";
        runJavaScript(webDriver, commandIn);
        String commandOut = "document.getElementsByClassName(\'cmdOut\')[" + tableIndex
                + "]=document.getElementById(\'" + tableId + "\').getElementsByClassName(\'cmdOut\')[" + tableIndex + "]";
        runJavaScript(webDriver, commandOut);
    }

    private  static void runJavaScript(WebDriver webDriver, String javaScript) {
        try {
            if (webDriver instanceof JavascriptExecutor) {
                ((JavascriptExecutor) webDriver).executeScript(javaScript, new Object[0]);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    logger.error("Error while creating javascript",e);
                }
            }
        } catch (Exception e) {
        	 logger.error("Framework Error :Unable to Execute Java Script: " + javaScript + "with error: ",
                     e);
        }

    }
    
   public static int getIntValue(String input, int orDefault)
   {
	   int intValue;
	   if (input == null || input.isEmpty())
	   {
		   intValue = orDefault;
	   }
	   else
		   try
		   {
			   intValue = Integer.valueOf(input);
		   }
		   catch(Exception e)
		   {
			   intValue = orDefault;
		   }
	   return intValue;
   }
}
