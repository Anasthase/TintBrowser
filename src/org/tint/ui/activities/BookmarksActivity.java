package org.tint.ui.activities;

import java.util.List;

import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.ui.UIManager;
import org.tint.ui.fragments.BookmarksFragment;
import org.tint.ui.fragments.HistoryFragment;
import org.tint.ui.tabs.GenericTabListener;
import org.tint.utils.Constants;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BookmarksActivity extends Activity {
	
	private UIManager mUIManager;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.BookmarksTitle);
        
        mUIManager = Controller.getInstance().getUIManager();
        
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        Tab tab = actionBar.newTab();
        tab.setText(R.string.BookmarksTabTitle);
        tab.setTabListener(new GenericTabListener<BookmarksFragment>(this, "bookmarks", BookmarksFragment.class));
        actionBar.addTab(tab);
        
        tab = actionBar.newTab();
        tab.setText(R.string.HistoryTabTitle);
        tab.setTabListener(new GenericTabListener<HistoryFragment>(this, "history", HistoryFragment.class));
        actionBar.addTab(tab);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.bookmarks_activity_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.removeGroup(R.id.BookmarksActivity_AddonsMenuGroup);
		
		List<AddonMenuItem> contributedMenuItems = Controller.getInstance().getAddonManager().getContributedHistoryBookmarksMenuItems();
		for (AddonMenuItem item : contributedMenuItems) {
			menu.add(R.id.BookmarksActivity_AddonsMenuGroup, item.getAddon().getMenuId(), 0, item.getMenuItem());
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_CANCELED);
            finish();
			return true;
		case R.id.BookmarksActivity_MenuAddBookmark:
			Intent i = new Intent(this, EditBookmarkActivity.class);
			i.putExtra(Constants.EXTRA_ID, -1);
			startActivity(i);
			
			return true;
			
		default:
			if (Controller.getInstance().getAddonManager().onContributedHistoryBookmarksMenuItemSelected(
					this,
					item.getItemId(),
					mUIManager.getCurrentWebView())) {
				return true;
			} else {
				return super.onContextItemSelected(item);
			}
		}
	}
}
