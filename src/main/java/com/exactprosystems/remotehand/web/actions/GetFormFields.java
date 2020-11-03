/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.clearth.connectivity.data.rhdata.JsonSerializer;
import com.exactprosystems.clearth.connectivity.data.rhdata.form.FormDesc;
import com.exactprosystems.clearth.connectivity.data.rhdata.form.FormFieldDesc;
import com.exactprosystems.clearth.connectivity.data.rhdata.form.FormFieldType;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.ActionOutputType;
import com.exactprosystems.remotehand.web.WebAction;
import com.exactprosystems.remotehand.web.WebConfiguration;
import com.exactprosystems.remotehand.RhUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
	private static final Logger logger = LoggerFactory.getLogger(GetFormFields.class);
	
	private static final String CHECK_REQUIRED_FLAG_PARAM = "checkRequiredFlag".toLowerCase();
	private static final String CHECK_DISABLED_FLAG_PARAM = "checkDisabledFlag".toLowerCase();
	private static final String GROUP_PARAM = "group";
	private static final String DEFAULT_GROUP = "refdata";
	
	private static final String FIELDS_CONTAINER_XPATH = "field.containers.xpath";
	private static final String FIELD_XPATH = "xpath"; 
	private static final String IS_REQUIRED_XPATH = "is.required.xpath";
	private static final String IS_DISABLED_XPATH = "is.disabled.xpath";
	
	private Properties properties;
	private String group;
	private boolean checkRequiredFlag;
	private boolean checkDisabledFlag;
	
	private final JsonSerializer serializer = new JsonSerializer();
	
	@Override
	public ActionOutputType getOutputType()
	{
		return ActionOutputType.ENCODED_DATA;
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		prepare(params);		
		WebElement formContainer = getFormContainer(webDriver, webLocator);
		
		List<WebElement> fieldContainers = findFieldContainers(formContainer);
		if (fieldContainers.isEmpty())
			throw new ScriptExecuteException("Unable to find fields in the specified form container.");
		
		List<FormFieldDesc> fields = findFields(fieldContainers);
		if (fields.isEmpty())
			throw new ScriptExecuteException("Unable to find fields in the specified fields containers.");
		return serializeFields(fields);
	}
	
	private void prepare(Map<String, String> params) throws ScriptExecuteException
	{
		properties = ((WebConfiguration) WebConfiguration.getInstance()).getFormParserProperties();
		if (properties == null)
			throw new ScriptExecuteException("Configuration for the form parser wasn't loaded.");

		group = params.get(GROUP_PARAM);
		if (group == null)
		{
			group = DEFAULT_GROUP;
			logInfo("Parameter #%s wasn't found. Value %s will be used by default.", GROUP_PARAM, DEFAULT_GROUP);
		}		
		checkRequiredFlag = RhUtils.getBooleanOrDefault(params, CHECK_REQUIRED_FLAG_PARAM, false);
		checkDisabledFlag = RhUtils.getBooleanOrDefault(params, CHECK_DISABLED_FLAG_PARAM, false);
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
	
	private List<WebElement> findFieldContainers(WebElement formContainer)
	{
		String xpath = properties.getProperty(group + '.' + FIELDS_CONTAINER_XPATH);
		logInfo("Try to find field containers by xpath '%s'...", xpath);		
		List<WebElement> containers = formContainer.findElements(By.xpath(xpath));
		logInfo("%d field containers found.", containers.size());
		return containers;
	}
	
	private List<FormFieldDesc> findFields(List<WebElement> fieldContainers)
	{
		List<FormFieldDesc> result = new ArrayList<FormFieldDesc>(fieldContainers.size());
		for (WebElement container : fieldContainers)
		{
			FormFieldDesc fieldDesc = findField(container);
			if (fieldDesc != null)
				result.add(fieldDesc);
		}
		return result;
	}
	
	private FormFieldDesc findField(WebElement container)
	{
		FormFieldDesc fieldDesc;
		if ((fieldDesc = findFieldWithType(container, FormFieldType.DROPDOWN_EXT)) != null)
			return fieldDesc;
		else if ((fieldDesc = findFieldWithType(container, FormFieldType.DROPDOWN)) != null)
			return fieldDesc;
		else if ((fieldDesc = findFieldWithType(container, FormFieldType.DATE)) != null)
			return fieldDesc;
		else if ((fieldDesc = findFieldWithType(container, FormFieldType.TIME)) != null)
			return fieldDesc;
		else 
			return findFieldWithType(container, FormFieldType.TEXT);
	}
	
	private FormFieldDesc findFieldWithType(WebElement container, FormFieldType type)
	{
		String xpath = properties.getProperty(format("%s.%s.%s", group, type.value(), FIELD_XPATH));	
		WebElement element = findElementOrNull(container, xpath);
		if (element == null)
			return null;
		
		FormFieldDesc fieldDesc = new FormFieldDesc();
		fieldDesc.setId(element.getAttribute("id"));
		fieldDesc.setType(type);
		
		if (checkRequiredFlag)
			fieldDesc.setRequired(isRequired(element, type));
		
		if (checkDisabledFlag)
		{
			boolean isEnabled = type == FormFieldType.DATE || type == FormFieldType.TIME || !isDisabled(element, type);
			fieldDesc.setEnabled(isEnabled);
		}		
		return fieldDesc;
	}
	
	private WebElement findElementOrNull(WebElement container, String xpath)
	{
		List<WebElement> list = container.findElements(By.xpath(xpath));
		return list.isEmpty() ? null : list.get(0);
	}
	
	private boolean isRequired(WebElement element, FormFieldType type)
	{
		String xpath = properties.getProperty(format("%s.%s.%s", group, type.value(), IS_REQUIRED_XPATH));
		WebElement e = findElementOrNull(element, xpath);
		return e != null;
	}
	
	private boolean isDisabled(WebElement element, FormFieldType type)
	{
		String xpath = properties.getProperty(format("%s.%s.%s", group, type.value(), IS_DISABLED_XPATH));
		WebElement e = findElementOrNull(element, xpath);
		return e != null;
	}
	
	private String serializeFields(List<FormFieldDesc> fieldDescs) throws ScriptExecuteException
	{
		FormDesc formDesc = new FormDesc();
		formDesc.setFields(fieldDescs);
		try
		{
			return serializer.serialize(formDesc);
		}
		catch (IOException e)
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

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
}
