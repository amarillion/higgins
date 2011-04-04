//    This file is part of Dr. Higgins.
//    Copyright 2003-2011 Martijn van Iersel <amarillion@yahoo.com>
//
//    Dr. Higgins is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Dr. Higgins is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Dr. Higgins.  If not, see <http://www.gnu.org/licenses/>.
package nl.helixsoft.util;

import java.awt.Component;
import javax.swing.JOptionPane;

import nl.helixsoft.higgins.Engine;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/** Utility class to help with launching the browser */
public final class Browser 
{
	public static void launch(Component frame, String uri) 
	{
		BrowserLauncher bl;
		String error = null;
		try {
			bl = new BrowserLauncher(null);
			bl.openURLinBrowser(uri);
		} catch (BrowserLaunchingInitializingException e) {
			error = Engine.res.getString("COULD_NOT_LAUNCH_BROWSER");
			e.printStackTrace();
		} catch (UnsupportedOperatingSystemException e) {
			error = Engine.res.getString("COULD_NOT_LAUNCH_BROWSER");
			e.printStackTrace();
		} catch (BrowserLaunchingExecutionException e) {
			error = Engine.res.getString("COULD_NOT_LAUNCH_BROWSER");
			e.printStackTrace();
		}
		if (error != null)
			JOptionPane.showMessageDialog(frame, error);		
	}	
}
