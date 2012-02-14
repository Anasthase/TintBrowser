package org.tint.model;

import java.util.Comparator;

/**
 * Comparator for UrlSuggestionItem.
 */
public class UrlSuggestionItemComparator implements Comparator<UrlSuggestionItem> {

	@Override
	public int compare(UrlSuggestionItem object1, UrlSuggestionItem object2) {
		Float value1 = new Float(object1.getNote());
		Float value2 = new Float(object2.getNote());
		return value2.compareTo(value1);
	}

}
