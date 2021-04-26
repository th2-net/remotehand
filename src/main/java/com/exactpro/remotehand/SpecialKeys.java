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

package com.exactpro.remotehand;

/**
 * Additional special keys not defined in org.openqa.selenium.Keys
 * 
 * 12 June 2019
 */
public enum SpecialKeys implements CharSequence
{
	NON_BREAKING_SPACE('\u00a0');
	
	private final char keyCode;

	SpecialKeys(char keyCode)
	{
		this.keyCode = keyCode;
	}

	@Override
	public int length()
	{
		return 1;
	}

	@Override
	public char charAt(int index)
	{
		return index == 0 ? this.keyCode : '\u0000';
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		if ((start == 0) && (end == 1))
			return String.valueOf(keyCode);
		else 
			throw new IndexOutOfBoundsException();
	}

	@Override
	public String toString()
	{
		return String.valueOf(keyCode);
	}
}
