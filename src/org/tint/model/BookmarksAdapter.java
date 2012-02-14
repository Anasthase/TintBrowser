package org.tint.model;

import org.tint.R;
import org.tint.providers.BookmarksProvider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class BookmarksAdapter extends SimpleCursorAdapter {
	
	private int mCaptureWidth;
	private int mCaptureHeight;
	
	private int mDefaultThumbnailId;
	
	public BookmarksAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int[] dimensions, int defaultThumbnailId) {
		super(context, layout, c, from, to);

		mCaptureWidth = dimensions[0];
		mCaptureHeight = dimensions[1];
		
		mDefaultThumbnailId = defaultThumbnailId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View superView = super.getView(position, convertView, parent);
		
		ImageView thumbnailView = (ImageView) superView.findViewById(R.id.BookmarkRow_Thumbnail);
		
		byte[] thumbnail = getCursor().getBlob(getCursor().getColumnIndex(BookmarksProvider.Columns.THUMBNAIL));
		if (thumbnail != null) {
			BitmapDrawable icon = new BitmapDrawable(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
			
			Bitmap bm = Bitmap.createBitmap(mCaptureWidth, mCaptureHeight, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bm);
			
			icon.setBounds(0, 0, mCaptureWidth, mCaptureHeight);
			icon.draw(canvas);
			
			thumbnailView.setImageBitmap(bm);
		} else {
			thumbnailView.setImageResource(mDefaultThumbnailId);
		}
		
		return superView;
	}

}
