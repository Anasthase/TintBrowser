/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tint.addons.framework;

import org.tint.addons.framework.Action;

interface IAddon {
	void onBind();
	void onUnbind();

	String getName();
	String getShortDescription();
	String getDescription();
	String getEMail();
	String getWebsite();
	
	int getCallbacks();
	
	List<Action> onPageStarted(String url);
	List<Action> onPageFinished(String url);
	
	String getContributedMainMenuItem();
	List<Action> onContributedMainMenuItemSelected(String currentTitle, String currentUrl);
	
	String getContributedLinkContextMenuItem(int hitTestResult, String url);
	List<Action> onContributedLinkContextMenuItemSelected(int hitTestResult, String url);
	
	String getContributedHistoryBookmarksMenuItem();
	List<Action> onContributedHistoryBookmarksMenuItemSelected();
	
	String getContributedBookmarkContextMenuItem();
	List<Action> onContributedBookmarkContextMenuItemSelected(String title, String url);
	
	String getContributedHistoryContextMenuItem();
	List<Action> onContributedHistoryContextMenuItemSelected(String title, String url);
	
	List<Action> onUserAnswerQuestion(String questionId, boolean positiveAnswer);
	
	void showAddonPreferenceActivity();	
}