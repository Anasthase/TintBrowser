package org.tint.ui.tabs;

import org.tint.R;
import org.tint.ui.TabletUIManager;
import org.tint.ui.fragments.TabletWebViewFragment;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;

public class WebViewFragmentTabListener implements ActionBar.TabListener {
	
	private TabletUIManager mTabsManager;
	private TabletWebViewFragment mFragment;	

	public WebViewFragmentTabListener(TabletUIManager tabsManager, TabletWebViewFragment fragment) {
		mTabsManager = tabsManager;
		mFragment = fragment;
	}
	
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) { }

	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction ft) {
		if (mFragment.isStartPageShown()) {
			ft.add(R.id.WebViewContainer, mTabsManager.getStartPageFragment(), null);
		} else {
			ft.add(R.id.WebViewContainer, mFragment, null);
		}
		mFragment.onTabSelected(arg0);
		mTabsManager.onTabSelected(arg0);
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction ft) {
		if (mFragment.isStartPageShown()) {
			ft.remove(mTabsManager.getStartPageFragment());
		} else {
			ft.remove(mFragment);
		}		
	}

}
