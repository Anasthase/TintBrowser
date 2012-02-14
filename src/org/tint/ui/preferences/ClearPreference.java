package org.tint.ui.preferences;

import org.tint.controllers.Controller;
import org.tint.providers.BookmarksWrapper;
import org.tint.utils.Constants;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebViewDatabase;

public class ClearPreference extends DialogPreference {

	public ClearPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		String key = getKey();
		if (key.equals(Constants.PREFERENCE_CLEAR_COOKIES)) {
			setEnabled(CookieManager.getInstance().hasCookies());
		} else if (key.equals(Constants.PREFERENCE_CLEAR_FORM_DATA)) {
			setEnabled(WebViewDatabase.getInstance(getContext()).hasFormData());
		} else if (key.equals(Constants.PREFERENCE_CLEAR_PASSWORDS)) {
			WebViewDatabase db = WebViewDatabase.getInstance(getContext());			
			setEnabled(db.hasUsernamePassword() || db.hasHttpAuthUsernamePassword());
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
						
		if (positiveResult) {
			String key = getKey();
			
			setEnabled(false);
			
			if (key.equals(Constants.PREFERENCE_CLEAR_CACHE)) {
				Controller.getInstance().getUIManager().clearCache();
			} if (key.equals(Constants.PREFERENCE_CLEAR_HISTORY)) {
				BookmarksWrapper.clearHistoryAndOrBookmarks(Controller.getInstance().getMainActivity().getContentResolver(), true, false);
			} else if (key.equals(Constants.PREFERENCE_CLEAR_COOKIES)) {
				CookieManager.getInstance().removeAllCookie();
			} else if (Constants.PREFERENCE_CLEAR_GEOLOCATION.equals(key)) {
				GeolocationPermissions.getInstance().clearAll();
			} else if (key.equals(Constants.PREFERENCE_CLEAR_FORM_DATA)) {
				Controller.getInstance().getUIManager().clearFormData();
			} else if (key.equals(Constants.PREFERENCE_CLEAR_PASSWORDS)) {
				WebViewDatabase db = WebViewDatabase.getInstance(getContext());
		        db.clearUsernamePassword();
		        db.clearHttpAuthUsernamePassword();
			}
		}
	}

}
