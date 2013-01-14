/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.tint.ui.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.tint.R;
import org.tint.ui.managers.UIFactory;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebStorage;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WebsitesSettingsFragment extends ListFragment {
	
	private static final String EXTRA_SITE = "site";
	
	private SiteAdapter mAdapter = null;
	private Site mSite = null;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.websites_settings_fragment, container, false);
		
		Bundle args = getArguments();
        if (args != null) {
            mSite = (Site) args.getParcelable(EXTRA_SITE);            
        }
        if (mSite == null) {
//            View clear = view.findViewById(R.id.clear_all_button);
//            clear.setVisibility(View.VISIBLE);
//            clear.setOnClickListener(this);
        } else {
        	if (!UIFactory.isTablet(getActivity())) {
        		// The current website is currently shown in tablet-type preferences activity / fragements.
        		getActivity().setTitle(String.format(getString(R.string.WebsitesSettingsSiteTitle), mSite.getPrettyTitle()));
        	}
        }
		
		return view;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mAdapter = new SiteAdapter(getActivity(), R.layout.websites_settings_row);
        
        if (mSite != null) {
            mAdapter.mCurrentSite = mSite;
        }
        
        getListView().setAdapter(mAdapter);
        getListView().setOnItemClickListener(mAdapter);
	}
	
	@Override
    public void onResume() {
        super.onResume();
        mAdapter.askForOrigins();
    }
	
	private void finish() {
        PreferenceActivity activity = (PreferenceActivity) getActivity();
        if (activity != null) {
            activity.finishPreferencePanel(this, 0, null);
        }
    }
	
	class SiteAdapter extends ArrayAdapter<Site> implements AdapterView.OnItemClickListener {
		
		private int mResource;
        private LayoutInflater mInflater;
        private Bitmap mUsageEmptyIcon;
        private Bitmap mUsageLowIcon;
        private Bitmap mUsageHighIcon;
        private Bitmap mLocationAllowedIcon;
        private Bitmap mLocationDisallowedIcon;
        private Site mCurrentSite;
		
		public SiteAdapter(Context context, int rsc) {
            this(context, rsc, null);
        }
		
		public SiteAdapter(Context context, int rsc, Site site) {
            super(context, rsc);
            
            mResource = rsc;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mUsageEmptyIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_list_data_off);
            mUsageLowIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_list_data_small);
            mUsageHighIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_list_data_large);
            mLocationAllowedIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_gps_on_holo_dark);
            mLocationDisallowedIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_gps_denied_holo_dark);
            mCurrentSite = site;
            
            if (mCurrentSite == null) {
                askForOrigins();
            }
        }
		
		/**
         * Adds the specified feature to the site corresponding to supplied
         * origin in the map. Creates the site if it does not already exist.
         */
        private void addFeatureToSite(Map<String, Site> sites, String origin, int feature) {
            Site site = null;
            if (sites.containsKey(origin)) {
                site = (Site) sites.get(origin);
            } else {
                site = new Site(origin);
                sites.put(origin, site);
            }
            site.addFeature(feature);
        }
		
		@SuppressWarnings("rawtypes")
		public void askForOrigins() {
            // Get the list of origins we want to display.
            // All 'HTML 5 modules' (Database, Geolocation etc) form these
            // origin strings using WebCore::SecurityOrigin::toString(), so it's
            // safe to group origins here. Note that WebCore::SecurityOrigin
            // uses 0 (which is not printed) for the port if the port is the
            // default for the protocol. Eg http://www.google.com and
            // http://www.google.com:80 both record a port of 0 and hence
            // toString() == 'http://www.google.com' for both.

            WebStorage.getInstance().getOrigins(new ValueCallback<Map>() {
				@SuppressWarnings("unchecked")
				public void onReceiveValue(Map origins) {
                    Map<String, Site> sites = new HashMap<String, Site>();
                    if (origins != null) {
                        Iterator<String> iter = origins.keySet().iterator();
                        while (iter.hasNext()) {
                            addFeatureToSite(sites, iter.next(), Site.FEATURE_WEB_STORAGE);
                        }
                    }
                    
                    askForGeolocation(sites);
                }
            });
        }
		
		public void askForGeolocation(final Map<String, Site> sites) {
            GeolocationPermissions.getInstance().getOrigins(new ValueCallback<Set<String> >() {
                public void onReceiveValue(Set<String> origins) {
                    if (origins != null) {
                        Iterator<String> iter = origins.iterator();
                        while (iter.hasNext()) {
                            addFeatureToSite(sites, iter.next(), Site.FEATURE_GEOLOCATION);
                        }
                    }
                    
                    //populateIcons(sites);
                    populateOrigins(sites);
                }
            });
        }
		
		public void populateOrigins(Map<String, Site> sites) {
            clear();

            // We can now simply populate our array with Site instances
            Set<Map.Entry<String, Site>> elements = sites.entrySet();
            Iterator<Map.Entry<String, Site>> entryIterator = elements.iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, Site> entry = entryIterator.next();
                Site site = entry.getValue();
                add(site);
            }

            notifyDataSetChanged();
        }
		
		public String sizeValueToString(long bytes) {
            // We display the size in MB, to 1dp, rounding up to the next 0.1MB.
            // bytes should always be greater than zero.
            if (bytes <= 0) {
                Log.e("WebsitesSettingsFragment", "sizeValueToString called with non-positive value: " + bytes);
                return "0";
            }
            float megabytes = (float) bytes / (1024.0F * 1024.0F);
            int truncated = (int) Math.ceil(megabytes * 10.0F);
            float result = (float) (truncated / 10.0F);
            return String.valueOf(result);
        }
		
		public void setIconForUsage(ImageView usageIcon, long usageInBytes) {
            float usageInMegabytes = (float) usageInBytes / (1024.0F * 1024.0F);
            // We set the correct icon:
            // 0 < empty < 0.1MB
            // 0.1MB < low < 5MB
            // 5MB < high
            if (usageInMegabytes <= 0.1) {
                usageIcon.setImageBitmap(mUsageEmptyIcon);
            } else if (usageInMegabytes > 0.1 && usageInMegabytes <= 5) {
                usageIcon.setImageBitmap(mUsageLowIcon);
            } else if (usageInMegabytes > 5) {
                usageIcon.setImageBitmap(mUsageHighIcon);
            }
        }
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			
			final TextView title;
			final TextView subtitle;
			
			final ImageView settingsIcon;
			
			final ImageView usageIcon;
            final ImageView locationIcon;
            final ImageView featureIcon;
			
			if (convertView == null) {
                view = mInflater.inflate(mResource, parent, false);
            } else {
                view = convertView;
            }
			
			title = (TextView) view.findViewById(R.id.WebsitesSettingsTitle);
			subtitle = (TextView) view.findViewById(R.id.WebsitesSettingsSubTitle);
			settingsIcon = (ImageView) view.findViewById(R.id.WebsitesSettingsIcon);
            featureIcon = (ImageView) view.findViewById(R.id.WebsitesSettingsFeatureIcon);
            usageIcon = (ImageView) view.findViewById(R.id.WebsitesSettingsUsageIcon);
            locationIcon = (ImageView) view.findViewById(R.id.WebsitesSettingsLocationIcon);
            
//            featureIcon.setVisibility(View.GONE);
//            usageIcon.setVisibility(View.GONE);
//            locationIcon.setVisibility(View.GONE);
            
            if (mCurrentSite == null) {            	
            	Site site = getItem(position);
            	
            	title.setText(site.getPrettyTitle());
            	
            	String subtitleText = site.getPrettyOrigin();
                if (subtitleText != null) {
                    title.setMaxLines(1);
                    title.setSingleLine(true);
                    subtitle.setVisibility(View.VISIBLE);
                    subtitle.setText(subtitleText);
                } else {
                    subtitle.setVisibility(View.GONE);
                    title.setMaxLines(2);
                    title.setSingleLine(false);
                }
            	
            	usageIcon.setVisibility(View.GONE);
                locationIcon.setVisibility(View.GONE);
                featureIcon.setVisibility(View.GONE);
            	
                // We set the site as the view's tag,
                // so that we can get it in onItemClick()
                view.setTag(site);
                
            	if (site.hasFeature(Site.FEATURE_WEB_STORAGE)) {
                    WebStorage.getInstance().getUsageForOrigin(site.getOrigin(), new ValueCallback<Long>() {
                        public void onReceiveValue(Long value) {
                            if (value != null) {
                                setIconForUsage(usageIcon, value.longValue());
                                usageIcon.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            	
            	if (site.hasFeature(Site.FEATURE_GEOLOCATION)) {
                    locationIcon.setVisibility(View.VISIBLE);
                    GeolocationPermissions.getInstance().getAllowed(site.getOrigin(), new ValueCallback<Boolean>() {
                        public void onReceiveValue(Boolean allowed) {
                            if (allowed != null) {
                                if (allowed.booleanValue()) {
                                    locationIcon.setImageBitmap(mLocationAllowedIcon);
                                } else {
                                    locationIcon.setImageBitmap(mLocationDisallowedIcon);
                                }
                            }
                        }
                    });
                }
            	
            } else {
            	
            	settingsIcon.setVisibility(View.GONE);
            	locationIcon.setVisibility(View.GONE);
                usageIcon.setVisibility(View.GONE);
                featureIcon.setVisibility(View.VISIBLE);
                
                switch (mCurrentSite.getFeatureByIndex(position)) {
                case Site.FEATURE_WEB_STORAGE:
                    WebStorage.getInstance().getUsageForOrigin(mCurrentSite.getOrigin(), new ValueCallback<Long>() {
                        public void onReceiveValue(Long value) {
                            if (value != null) {
                                String usage = String.format(getString(R.string.WebsitesSettingsDataUsage),  sizeValueToString(value.longValue()));
                                title.setText(usage);
                                subtitle.setText(R.string.WebsitesSettingsDataClickToClear);
                                subtitle.setVisibility(View.VISIBLE);
                                setIconForUsage(featureIcon, value.longValue());
                            }
                        }
                    });
                    break;
                case Site.FEATURE_GEOLOCATION:
                	subtitle.setText(R.string.WebsitesSettingsGeolocationClickToClear);
                    GeolocationPermissions.getInstance().getAllowed(mCurrentSite.getOrigin(), new ValueCallback<Boolean>() {
                        public void onReceiveValue(Boolean allowed) {
                            if (allowed != null) {
                                if (allowed.booleanValue()) {
                                    title.setText(R.string.WebsitesSettingsGeolocationAllowed);
                                    featureIcon.setImageBitmap(mLocationAllowedIcon);
                                } else {
                                    title.setText(R.string.WebsitesSettingsGeolocationNotAllowed);
                                    featureIcon.setImageBitmap(mLocationDisallowedIcon);
                                }
                                
                                subtitle.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    break;
            }
            }
			
			return view;
		}

		public void onItemClick(AdapterView<?> parent,
				View view,
				int position,
				long id) {
			if (mCurrentSite != null) {
				switch (mCurrentSite.getFeatureByIndex(position)) {
				case Site.FEATURE_WEB_STORAGE:
					new AlertDialog.Builder(getContext())
					.setTitle(R.string.WebsitesSettingsCleatDataDialogTitle)
					.setMessage(R.string.WebsitesSettingsCleatDataDialogMessage)
					.setPositiveButton(R.string.OK,
							new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dlg, int which) {
							WebStorage.getInstance().deleteOrigin(mCurrentSite.getOrigin());
							// If this site has no more features, then go back to the
							// origins list.
							mCurrentSite.removeFeature(Site.FEATURE_WEB_STORAGE);
							if (mCurrentSite.getFeatureCount() == 0) {
								finish();
							}
							askForOrigins();
							notifyDataSetChanged();
						}})
						.setNegativeButton(R.string.Cancel, null)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show();
					break;
				case Site.FEATURE_GEOLOCATION:
					new AlertDialog.Builder(getContext())
					.setTitle(R.string.WebsitesSettingsGeolocationPageDialogTitle)
					.setMessage(R.string.WebsitesSettingsGeolocationPageDialogMessage)
					.setPositiveButton(R.string.OK,
							new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dlg, int which) {
							GeolocationPermissions.getInstance().clear(mCurrentSite.getOrigin());
							mCurrentSite.removeFeature(Site.FEATURE_GEOLOCATION);
							if (mCurrentSite.getFeatureCount() == 0) {
								finish();
							}
							askForOrigins();
							notifyDataSetChanged();
						}})
						.setNegativeButton(R.string.Cancel, null)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show();
					break;
				}
			} else {
				Site site = (Site) view.getTag();
				PreferenceActivity activity = (PreferenceActivity) getActivity();
				if (activity != null) {
					Bundle args = new Bundle();
					args.putParcelable(EXTRA_SITE, site);
					activity.startPreferencePanel(WebsitesSettingsFragment.class.getName(), args, 0,
							site.getPrettyTitle(), null, 0);
				}
			}
		}
		
		public int getCount() {
            if (mCurrentSite == null) {
                return super.getCount();
            }
            return mCurrentSite.getFeatureCount();
        }
	}
	
	static class Site implements Parcelable {
        private String mOrigin;
        private String mTitle;
        private Bitmap mIcon;
        private int mFeatures;

        // These constants provide the set of features that a site may support
        // They must be consecutive. To add a new feature, add a new FEATURE_XXX
        // variable with value equal to the current value of FEATURE_COUNT, then
        // increment FEATURE_COUNT.
        private final static int FEATURE_WEB_STORAGE = 0;
        private final static int FEATURE_GEOLOCATION = 1;
        // The number of features available.
        private final static int FEATURE_COUNT = 2;

        public Site(String origin) {
            mOrigin = origin;
            mTitle = null;
            mIcon = null;
            mFeatures = 0;
        }

        public void addFeature(int feature) {
            mFeatures |= (1 << feature);
        }

        public void removeFeature(int feature) {
            mFeatures &= ~(1 << feature);
        }

        public boolean hasFeature(int feature) {
            return (mFeatures & (1 << feature)) != 0;
        }

        /**
         * Gets the number of features supported by this site.
         */
        public int getFeatureCount() {
            int count = 0;
            for (int i = 0; i < FEATURE_COUNT; ++i) {
                count += hasFeature(i) ? 1 : 0;
            }
            return count;
        }

        /**
         * Gets the ID of the nth (zero-based) feature supported by this site.
         * The return value is a feature ID - one of the FEATURE_XXX values.
         * This is required to determine which feature is displayed at a given
         * position in the list of features for this site. This is used both
         * when populating the view and when responding to clicks on the list.
         */
        public int getFeatureByIndex(int n) {
            int j = -1;
            for (int i = 0; i < FEATURE_COUNT; ++i) {
                j += hasFeature(i) ? 1 : 0;
                if (j == n) {
                    return i;
                }
            }
            return -1;
        }

        public String getOrigin() {
            return mOrigin;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public void setIcon(Bitmap icon) {
            mIcon = icon;
        }

        public Bitmap getIcon() {
            return mIcon;
        }

        public String getPrettyOrigin() {
            return mTitle == null ? null : hideHttp(mOrigin);
        }

        public String getPrettyTitle() {
            return mTitle == null ? hideHttp(mOrigin) : mTitle;
        }

        private String hideHttp(String str) {
            Uri uri = Uri.parse(str);
            return "http".equals(uri.getScheme()) ?  str.substring(7) : str;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mOrigin);
            dest.writeString(mTitle);
            dest.writeInt(mFeatures);
            dest.writeParcelable(mIcon, flags);
        }

        private Site(Parcel in) {
            mOrigin = in.readString();
            mTitle = in.readString();
            mFeatures = in.readInt();
            mIcon = in.readParcelable(null);
        }

        public static final Parcelable.Creator<Site> CREATOR
                = new Parcelable.Creator<Site>() {
            public Site createFromParcel(Parcel in) {
                return new Site(in);
            }

            public Site[] newArray(int size) {
                return new Site[size];
            }
        };

    }

}
