package org.tint.ui.preferences;

public interface IHistoryBookmaksImportListener {
	
	void onImportProgress(int step, int progress, int total);
	
	void onImportDone(String message);

}
