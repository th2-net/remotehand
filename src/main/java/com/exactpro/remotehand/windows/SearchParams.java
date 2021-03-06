/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.windows;

public class SearchParams {

	public final String locator;
	public final String matcher;
	public final Integer parsedIndex;
	
	public SearchParams(String locator, String matcher, Integer parsedIndex) {
		this.locator = locator;
		this.matcher = matcher;
		this.parsedIndex = parsedIndex;
	}
	
	public static class HeaderKeys {
		public final String locator;
		public final String matcher;
		public final String index;
		
		public static final HeaderKeys DEFAULT = new HeaderKeys("locator", "matcher", "matcherindex");

		public HeaderKeys(String locator, String matcher, String index) {
			this.locator = locator;
			this.matcher = matcher;
			this.index = index;
		}

		public HeaderKeys(String prefix) {
			this.locator = prefix + DEFAULT.locator;
			this.matcher = prefix + DEFAULT.matcher;
			this.index = prefix + DEFAULT.index;
		}
	}
}
