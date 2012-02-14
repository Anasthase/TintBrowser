package org.tint.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.tint.R;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.preferences.IHistoryBookmaksImportListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

public class HistoryBookmarksImportTask extends AsyncTask<String, Integer, String> {
	
	private Context mContext;
	private IHistoryBookmaksImportListener mListener;
	
	public HistoryBookmarksImportTask(Context context, IHistoryBookmaksImportListener listener) {
		mContext = context;
		mListener = listener;
	}

	@Override
	protected String doInBackground(String... params) {
		
		publishProgress(0, 0, 0);
		
		File file = new File(Environment.getExternalStorageDirectory(), params[0]);
		
		if ((file != null) &&
				(file.exists()) &&
				(file.canRead())) {
			
			try {
				
				publishProgress(1, 0, 0);
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();				
				DocumentBuilder builder;

				builder = factory.newDocumentBuilder();
				Document document = builder.parse(file);

				Element root = document.getDocumentElement();
				
				if ((root != null) &&
						(root.getNodeName().equals("itemlist"))) {
					
					NodeList itemsList = root.getElementsByTagName("item");
					
					int progress = 0;
					int total = itemsList.getLength();
					
					Node item;
					NodeList record;
					Node dataItem;
					
					for (int i = 0; i < itemsList.getLength(); i++) {
						
						publishProgress(2, progress, total);
						
						item = itemsList.item(i);
						
						if (item != null) {
							record = item.getChildNodes();
							
							String title = null;
							String url = null;
							int visits = 0;
							long visitedDate = -1;
							long creationDate = -1;
							int bookmark = 0;
							
							for (int j = 0; j < record.getLength(); j++) {
								dataItem = record.item(j);																
								
								if ((dataItem != null) &&
										(dataItem.getNodeName() != null)) {
									
									if (dataItem.getNodeName().equals("title")) {
										title = URLDecoder.decode(getNodeContent(dataItem));										
									} else if (dataItem.getNodeName().equals("url")) {
										url = URLDecoder.decode(getNodeContent(dataItem));
									} else if (dataItem.getNodeName().equals("visits")) {
										try {
											visits = Integer.parseInt(getNodeContent(dataItem));
										} catch (Exception e) {
											visits = 0;
										}
									} else if (dataItem.getNodeName().equals("visiteddate")) {
										try {
											visitedDate = Long.parseLong(getNodeContent(dataItem));
										} catch (Exception e) {
											visitedDate = -1;
										}
									} else if (dataItem.getNodeName().equals("creationdate")) {
										try {
											creationDate = Long.parseLong(getNodeContent(dataItem));
										} catch (Exception e) {
											creationDate = -1;
										}
									} else if (dataItem.getNodeName().equals("bookmark")) {
										try {
											bookmark = Integer.parseInt(getNodeContent(dataItem));
										} catch (Exception e) {
											bookmark = 0;
										}
									}
								}								
							}
						
							BookmarksWrapper.insertRawRecord(mContext.getContentResolver(),
									title,
									url,
									visits,
									visitedDate,
									creationDate,
									bookmark);
						}
												
						progress++;						
					}
				}
			
			} catch (ParserConfigurationException e) {
				return e.getMessage();
			} catch (SAXException e) {
				return e.getMessage();
			} catch (IOException e) {
				return e.getMessage();
			}
			
		} else {
			return mContext.getString(R.string.HistoryBookmarksImportFileUnavailable);
		}
		
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		mListener.onImportProgress(values[0], values[1], values[2]);
	}
	
	@Override
	protected void onPostExecute(String result) {
		mListener.onImportDone(result);
	}
	
	/**
	 * Get the content of a node, why Android does not include Node.getTextContent() ?
	 * @param node The node.
	 * @return The node content.
	 */
	private String getNodeContent(Node node) {
		StringBuffer buffer = new StringBuffer();
		NodeList childList = node.getChildNodes();
		for (int i = 0; i < childList.getLength(); i++) {
		    Node child = childList.item(i);
		    if (child.getNodeType() != Node.TEXT_NODE) {
		        continue; // skip non-text nodes
		    }
		    buffer.append(child.getNodeValue());
		}

		return buffer.toString(); 
	}

}
