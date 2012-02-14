package org.tint.ui.preferences;

public interface IHistoryBookmaksExportListener {
	
	void onExportProgress(int step, int progress, int total);
	
	void onExportDone(String message);

}
