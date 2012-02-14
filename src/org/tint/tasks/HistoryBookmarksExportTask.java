package org.tint.tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.tint.R;
import org.tint.providers.BookmarksProvider;
import org.tint.ui.preferences.IHistoryBookmaksExportListener;
import org.tint.utils.IOUtils;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

public class HistoryBookmarksExportTask extends AsyncTask<Cursor, Integer, String> {

	private Context mContext;
	private IHistoryBookmaksExportListener mListener;
	
	public HistoryBookmarksExportTask(Context context, IHistoryBookmaksExportListener listener) {
		mContext = context;
		mListener = listener;
	}

	@Override
	protected String doInBackground(Cursor... params) {
		
		publishProgress(0, 0, 0);
		
		String cardState = IOUtils.checkCardState(mContext);
		if (cardState != null) {
			return cardState;
		}
		
		try {
			String fileName = mContext.getString(R.string.ApplicationName) + "-" + getNowForFileName() + ".xml";

			File file = new File(Environment.getExternalStorageDirectory(), fileName);		
			FileWriter writer = new FileWriter(file);
			
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writer.write("<itemlist>\n");
			
			Cursor c = params[0];
			if (c.moveToFirst()) {
				
				int titleIndex = c.getColumnIndex(BookmarksProvider.Columns.TITLE);
				int urlIndex = c.getColumnIndex(BookmarksProvider.Columns.URL);
				int creationDateIndex = c.getColumnIndex(BookmarksProvider.Columns.CREATION_DATE);
				int visitedDateIndex = c.getColumnIndex(BookmarksProvider.Columns.VISITED_DATE);
				int visitsIndex = c.getColumnIndex(BookmarksProvider.Columns.VISITS);
				int bookmarkIndex = c.getColumnIndex(BookmarksProvider.Columns.BOOKMARK);
				
				int current = 0;
				int total = c.getCount();
				
				while (!c.isAfterLast()) {
					
					publishProgress(1, current, total);
					
					writer.write("<item>\n");
					
					String title = c.getString(titleIndex);
					writer.write(String.format("<title>%s</title>\n", title != null ? URLEncoder.encode(title) : ""));
					
					String url = c.getString(urlIndex);
					writer.write(String.format("<url>%s</url>\n", url != null ? URLEncoder.encode(url) : ""));
					
					writer.write(String.format("<creationdate>%s</creationdate>\n", c.getLong(creationDateIndex)));
					writer.write(String.format("<visiteddate>%s</visiteddate>\n", c.getLong(visitedDateIndex)));
					writer.write(String.format("<visits>%s</visits>\n", c.getInt(visitsIndex)));					
					writer.write(String.format("<bookmark>%s</bookmark>\n", c.getInt(bookmarkIndex)));
					
					writer.write("</item>\n");
					
					current++;
					c.moveToNext();
				}
				
			}
			
			writer.write("</itemlist>\n");

			writer.flush();
			writer.close();
			
		} catch (IOException e) {			
			e.printStackTrace();
			return e.getMessage();
		}
		
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		mListener.onExportProgress(values[0], values[1], values[2]);
	}
	
	@Override
	protected void onPostExecute(String result) {
		mListener.onExportDone(result);
	}
	
	/**
	 * Get a string representation of the current date / time in a format suitable for a file name.
	 * @return A string representation of the current date / time.
	 */
	private String getNowForFileName() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		
		return sdf.format(c.getTime());
	}

}
