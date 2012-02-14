package org.tint.addons.framework;

import org.tint.addons.framework.AddonResponse;

interface IAddon {
	void onBind();
	void onUnbind();

	String getName();
	String getShortDescription();
	String getDescription();
	String getEMail();
	String getWebsite();
	
	int getCallbacks();
	
	AddonResponse onPageStarted(String url);
	AddonResponse onPageFinished(String url);
	
	String getContributedMainMenuItem();
	AddonResponse onContributedMainMenuItemSelected(String currentTitle, String currentUrl);
	
	String getContributedLinkContextMenuItem();
	AddonResponse onContributedLinkContextMenuItemSelected(int hitTestResult, String url);
	
	String getContributedHistoryBookmarksMenuItem();
	AddonResponse onContributedHistoryBookmarksMenuItemSelected();
	
	String getContributedBookmarkContextMenuItem();
	AddonResponse onContributedBookmarkContextMenuItemSelected(String title, String url);
	
	String getContributedHistoryContextMenuItem();
	AddonResponse onContributedHistoryContextMenuItemSelected(String title, String url);
	
	AddonResponse onUserAnswerQuestion(String questionId, boolean positiveAnswer);
	
	void showAddonPreferenceActivity();	
}