package nl.helixsoft.higgins;

import nl.helixsoft.util.KeyType;

public enum HiggPrefs implements KeyType 
{
	LAST_USED_LESSONS_DIR (MainFrame.DEFAULT_LESSONS_DIR),
	WIN_X (0),
	WIN_Y (0),
	DEFAULT_BINS (4),
	INTERNATIONAL_INPUT (true),
	;
	
	private String def;
	
	HiggPrefs(Object aDefault)
	{
		def = "" + aDefault;
	}
	
	public String getDefault() 
	{
		return def;
	}
}
