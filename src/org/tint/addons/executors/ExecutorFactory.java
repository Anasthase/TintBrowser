/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.tint.addons.executors;

import java.util.HashMap;
import java.util.Map;

import org.tint.addons.framework.AddTabAction;
import org.tint.addons.framework.Action;
import org.tint.addons.framework.AskUserAction;
import org.tint.addons.framework.LoadUrlAction;
import org.tint.addons.framework.ShowDialogAction;
import org.tint.addons.framework.ShowToastAction;

public class ExecutorFactory {
	
	private static Map<String, BaseActionExecutor> sClassMap;
	
	static {
		sClassMap = new HashMap<String, BaseActionExecutor>();
		sClassMap.put(Action.class.getName(), new ActionExecutor());
		sClassMap.put(ShowDialogAction.class.getName(), new ShowDialogExecutor());
		sClassMap.put(LoadUrlAction.class.getName(), new LoadUrlExecutor());
		sClassMap.put(ShowToastAction.class.getName(), new ShowToastExecutor());
		sClassMap.put(AddTabAction.class.getName(), new AddTabExecutor());
		sClassMap.put(AskUserAction.class.getName(), new AskUserExecutor());
	}
	
	public static BaseActionExecutor getExecutor(Action addonAction) {
		return sClassMap.get(addonAction.getClass().getName());
	}

}
