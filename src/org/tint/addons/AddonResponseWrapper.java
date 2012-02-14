package org.tint.addons;

import org.tint.addons.framework.AddonResponse;

public class AddonResponseWrapper {

	private Addon mAddon;
	private AddonResponse mResponse;
	
	public AddonResponseWrapper(Addon addon, AddonResponse response) {
		mAddon = addon;
		mResponse = response;
	}
	
	public Addon getAddon() {
		return mAddon;
	}
	
	public AddonResponse getResponse() {
		return mResponse;
	}
	
}
