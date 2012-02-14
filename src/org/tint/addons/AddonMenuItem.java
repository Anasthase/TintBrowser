package org.tint.addons;

public class AddonMenuItem {

	private Addon mAddon;
	private String mMenuItem;	
	
	public AddonMenuItem(Addon addon, String menuItem) {
		mAddon = addon;
		mMenuItem = menuItem;
	}
	
	public Addon getAddon() {
		return mAddon;
	}
	
	public String getMenuItem() {
		return mMenuItem;
	}
	
}
