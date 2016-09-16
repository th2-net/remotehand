////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.clearth.rhxmldata.FormDesc;
import com.exactprosystems.clearth.rhxmldata.FormFieldDesc;
import com.exactprosystems.clearth.rhxmldata.FormFieldType;
import com.exactprosystems.clearth.rhxmldata.ObjectFactory;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import com.exactprosystems.remotehand.web.WebConfiguration;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;

/**
 * @author anna.bykova.
 */
public class GetFormFields extends WebAction
{
	private static final Logger logger = Logger.getLogger(GetFormFields.class);
	
	private static final String GROUP_PARAM = "group";
	private static final String DEFAULT_GROUP = "refdata";
	
	private static final String FIELDS_CONTAINER_XPATH = "field.containers.xpath";
	private static final String FIELD_XPATH = "xpath"; 
	private static final String IS_REQUIRED_XPATH = "is.required.xpath";
	private static final String IS_DISABLED_XPATH = "is.disabled.xpath";
	
	private Properties properties;
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		properties = ((WebConfiguration) WebConfiguration.getInstance()).getFormParserProperties();
		if (properties == null)
			throw new ScriptExecuteException("Configuration for the form parser wasn't loaded.");
		
		String group = params.get(GROUP_PARAM);
		if (group == null)
		{
			group = DEFAULT_GROUP;
			logger.info(format("Parameter #%s wasn't found. Value %s will be used by default.", GROUP_PARAM, DEFAULT_GROUP));
		}
		
		WebElement formContainer = getFormContainer(webDriver, webLocator);
		
		List<WebElement> fieldContainers = findFieldContainers(formContainer, group);
		if (fieldContainers.isEmpty())
			throw new ScriptExecuteException("Unable to find fields in the specified form container.");
		
		List<FormFieldDesc> fields = findFields(fieldContainers, group);
		if (fields.isEmpty())
			throw new ScriptExecuteException("Unable to find fields in the specified fields containers.");
		return serializeFields(fields);
	}
	
	private WebElement getFormContainer(WebDriver webDriver, By webLocator) throws ScriptExecuteException
	{
		try
		{
			return findElement(webDriver, webLocator);
		}
		catch (NoSuchElementException e)
		{
			throw new ScriptExecuteException("Unable to find container element for fields of the form.", e);
		}
	}
	
	private List<WebElement> findFieldContainers(WebElement formContainer, String group)
	{
		String xpath = properties.getProperty(group + '.' + FIELDS_CONTAINER_XPATH);
		if (logger.isInfoEnabled())
			logger.info(format("Try to find field containers by xpath '%s'...", xpath));		
		List<WebElement> containers = formContainer.findElements(By.xpath(xpath));
		if (logger.isInfoEnabled())
			logger.info(format("%d field containers found.", containers.size()));
		return containers;
	}
	
	private List<FormFieldDesc> findFields(List<WebElement> fieldContainers, String group)
	{
		List<FormFieldDesc> result = new ArrayList<FormFieldDesc>(fieldContainers.size());
		for (WebElement container : fieldContainers)
		{
			FormFieldDesc fieldDesc = findField(container, group);
			if (fieldDesc != null)
				result.add(fieldDesc);
		}
		return result;
	}
	
	private FormFieldDesc findField(WebElement container, String group)
	{
		FormFieldDesc fieldDesc;
		if ((fieldDesc = findFieldWithType(container, group, FormFieldType.DROPDOWN)) != null)
			return fieldDesc;
		else if ((fieldDesc = findFieldWithType(container, group, FormFieldType.DATE)) != null)
			return fieldDesc;
		else if ((fieldDesc = findFieldWithType(container, group, FormFieldType.TIME)) != null)
			return fieldDesc;
		else 
			return findFieldWithType(container, group, FormFieldType.TEXT);
	}
	
	private FormFieldDesc findFieldWithType(WebElement container, String group, FormFieldType type)
	{
		String xpath = properties.getProperty(format("%s.%s.%s", group, type.value(), FIELD_XPATH));	
		WebElement element = findElementOrNull(container, xpath);
		if (element == null)
			return null;
		
		FormFieldDesc fieldDesc = new FormFieldDesc();
		fieldDesc.setId(element.getAttribute("id"));
		fieldDesc.setType(type);
		fieldDesc.setRequired(isRequired(element, group, type));
		boolean isEnabled = type == FormFieldType.DATE || type == FormFieldType.TIME || !isDisabled(element, group, type);
		fieldDesc.setEnabled(isEnabled);
		
		return fieldDesc;
	}
	
	private WebElement findElementOrNull(WebElement container, String xpath)
	{
		List<WebElement> list = container.findElements(By.xpath(xpath));
		return list.isEmpty() ? null : list.get(0);
	}
	
	private boolean isRequired(WebElement element, String group, FormFieldType type)
	{
		String xpath = properties.getProperty(format("%s.%s.%s", group, type.value(), IS_REQUIRED_XPATH));
		WebElement e = findElementOrNull(element, xpath);
		return e != null;
	}
	
	private boolean isDisabled(WebElement element, String group, FormFieldType type)
	{
		String xpath = properties.getProperty(format("%s.%s.%s", group, type.value(), IS_DISABLED_XPATH));
		WebElement e = findElementOrNull(element, xpath);
		return e != null;
	}
	
	private String serializeFields(List<FormFieldDesc> fieldDescs) throws ScriptExecuteException
	{
		FormDesc formDesc = new FormDesc();
		formDesc.getFields().addAll(fieldDescs);
		try
		{
			Marshaller m = JAXBContext.newInstance(FormDesc.class).createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter writer = new StringWriter();
			ObjectFactory of = new ObjectFactory();
			m.marshal(of.createForm(formDesc), writer);
			return writer.toString();
		}
		catch (JAXBException e)
		{
			throw new ScriptExecuteException("Unable to serialize action result", e);
		}
	}

	@Override
	public boolean isNeedLocator()
	{
		return true;
	}

	@Override
	public boolean isCanWait()
	{
		return true;
	}
}
