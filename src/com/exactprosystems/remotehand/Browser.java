package com.exactprosystems.remotehand;

public enum Browser
{
	FIREFOX("Firefox"), 
	IE("IE"), 
	CHROME("Chrome"), 
	INVALID("");

	private String label;

	private Browser(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	public static Browser valueByLabel(String label)
	{
		if (label == null)
			return INVALID;

		for (Browser b : values())
			if (b.label.equals(label))
				return b;
		return INVALID;
	}
}
