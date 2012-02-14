package org.tint.addons.framework;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class AddonResponse implements Parcelable {
	
	public static final Parcelable.Creator<AddonResponse> CREATOR = new Parcelable.Creator<AddonResponse>() {

		@Override
		public AddonResponse createFromParcel(Parcel in) {
			return new AddonResponse(in);
		}

		@Override
		public AddonResponse[] newArray(int size) {
			return new AddonResponse[size];
		}
	};
	
	private List<AddonAction> mActions;
	
	public AddonResponse() {
		mActions = new ArrayList<AddonAction>();
	}
	
	public AddonResponse(Parcel in) {
		mActions = in.createTypedArrayList(AddonAction.CREATOR);
	}
	
	public List<AddonAction> getActions() {
		return mActions;
	}
	
	public void addAction(AddonAction action) {
		mActions.add(action);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mActions);
	}

}
