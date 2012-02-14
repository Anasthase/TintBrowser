package org.tint.ui.preferences;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.tint.R;
import org.tint.providers.BookmarksWrapper;
import org.tint.tasks.HistoryBookmarksExportTask;
import org.tint.tasks.HistoryBookmarksImportTask;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.IOUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

public class AdvancedPreferencesFragment extends PreferenceFragment implements IHistoryBookmaksExportListener, IHistoryBookmaksImportListener {
	
	private ProgressDialog mProgress;
	
	private HistoryBookmarksImportTask mImportTask;
	private HistoryBookmarksExportTask mExportTask;
	
	private static final AtomicReference<AsyncTask<String, Integer, String>> mImportSyncThread =
		      new AtomicReference<AsyncTask<String, Integer, String>>();
	
	private static final AtomicReference<AsyncTask<Cursor, Integer, String>> mExportSyncThread =
		      new AtomicReference<AsyncTask<Cursor, Integer, String>>();	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_advanced_settings);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		super.onActivityCreated(savedInstanceState);
		
		Preference clearPref = getPreferenceScreen().findPreference("PREFERENCE_CLEAR_HISTORY_BOOKMARKS");
		clearPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    	builder.setIcon(android.R.drawable.ic_dialog_info);
		    	builder.setTitle(getResources().getString(R.string.HistoryBookmarksClearTitle));
		    	builder.setSingleChoiceItems(getActivity().getResources().getStringArray(R.array.ClearHistoryBookmarksChoice), 0, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						switch (which) {
						case 0:
							BookmarksWrapper.clearHistoryAndOrBookmarks(getActivity().getContentResolver(), true, false);
							break;
						
						case 1:
							BookmarksWrapper.clearHistoryAndOrBookmarks(getActivity().getContentResolver(), false, true);
							break;
							
						case 2:
							BookmarksWrapper.clearHistoryAndOrBookmarks(getActivity().getContentResolver(), true, true);
							break;

						default:
							break;
						}
					}
		    		
		    	});
		    	
		    	builder.setCancelable(true);
		    	builder.setNegativeButton(R.string.Cancel, null);
		    	
		    	AlertDialog alert = builder.create();
		    	alert.show();
		    	
				return true;
			}
		});
		
		
		Preference importPref = getPreferenceScreen().findPreference("PREFERENCE_IMPORT_HISTORY_BOOKMARKS");
		importPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				List<String> exportedFiles = IOUtils.getExportedBookmarksFileList();		    	
		    	final String[] choices = exportedFiles.toArray(new String[exportedFiles.size()]);
		    	
		    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    	builder.setIcon(android.R.drawable.ic_dialog_info);
		    	builder.setTitle(getResources().getString(R.string.HistoryBookmarksImportSourceTitle));
		    	builder.setSingleChoiceItems(choices, 0, new OnClickListener() {
		    		
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						
						mImportTask = new HistoryBookmarksImportTask(getActivity(), AdvancedPreferencesFragment.this);
						
						mProgress = ProgressDialog.show(getActivity(),
								getString(R.string.HistoryBookmarksImportTitle),
								getString(R.string.HistoryBookmarksImportInitialMessage),
								true,
								false);
						
						mProgress.show();
						
						boolean retVal = mImportSyncThread.compareAndSet(null, mImportTask);
						if (retVal) {
							mImportTask.execute(choices[which]);
						}						
						
					}    		
		    	});    	
		    	
		    	builder.setCancelable(true);
		    	builder.setNegativeButton(R.string.Cancel, null);
		    	
		    	AlertDialog alert = builder.create();
		    	alert.show();
		    	
				return true;
			}
		});
		
		Preference exportPref = getPreferenceScreen().findPreference("PREFERENCE_EXPORT_HISTORY_BOOKMARKS");
		exportPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				mExportTask = new HistoryBookmarksExportTask(getActivity(), AdvancedPreferencesFragment.this);
				
				mProgress = ProgressDialog.show(getActivity(),
						getString(R.string.HistoryBookmarksExportTitle),
						getString(R.string.HistoryBookmarksExportInitialMessage),
						true,
						false);
				
				mProgress.show();
				
				boolean retVal = mExportSyncThread.compareAndSet(null, mExportTask);
				if (retVal) {
					mExportTask.execute(BookmarksWrapper.getAllHistoryBookmarks(getActivity().getContentResolver()));
				}
				
				return true;
			}
		});
	}

	@Override
	public void onExportProgress(int step, int progress, int total) {
		switch(step) {
		case 0:
			mProgress.setMessage(getString(R.string.HistoryBookmarksExportCheckCardMessage));
			break;
		case 1:
			mProgress.setMessage(String.format(getString(R.string.HistoryBookmarksExportProgressMessage), progress, total));
			break;
		default: break;
		}
	}

	@Override
	public void onExportDone(String message) {
		mExportSyncThread.compareAndSet(mExportTask, null);
		mProgress.dismiss();
		
		if (message != null) {
			ApplicationUtils.showErrorDialog(getActivity(),
					getString(R.string.HistoryBookmarksExportErrorTitle),
					String.format(getString(R.string.HistoryBookmarksExportErrorMessage), message));
		}
	}

	@Override
	public void onImportProgress(int step, int progress, int total) {
		switch(step) {
		case 0:
			mProgress.setMessage(getString(R.string.HistoryBookmarksImportReadingFile));
			break;
		case 1:
			mProgress.setMessage(getString(R.string.HistoryBookmarksImportParsingFile));
			break;
		case 2:			
			mProgress.setMessage(String.format(getString(R.string.HistoryBookmarksImportProgressMessage), progress, total));
			break;
		default: break;
		}
	}

	@Override
	public void onImportDone(String message) {
		mImportSyncThread.compareAndSet(mImportTask, null);
		mProgress.dismiss();
		
		if (message != null) {
			ApplicationUtils.showErrorDialog(getActivity(),
					getString(R.string.HistoryBookmarksImportErrorTitle),
					String.format(getString(R.string.HistoryBookmarksImportErrorMessage), message));
		}
	}

}
