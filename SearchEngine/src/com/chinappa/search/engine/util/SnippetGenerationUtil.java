package com.chinappa.search.engine.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SnippetGenerationUtil {

	public static int[] fetchMinimumWindow(Map<String, ArrayList<Integer>> termPosMap) {
		ArrayList<String> relevantTermList = new ArrayList<String>();
		for (Iterator<String> mapIterator = termPosMap.keySet().iterator(); mapIterator
				.hasNext();) {
			String key = mapIterator.next();
			if (termPosMap.get(key).size() == 0) {
				mapIterator.remove();
			} else {
				relevantTermList.add(key);
			}
		}

		Integer currentWindowLength = Integer.MAX_VALUE;
		int[] windowTerms = new int[termPosMap.size()];
		for (int j = 0; j < windowTerms.length; j++) {
			if (j == windowTerms.length - 1) {
				windowTerms[j] = Integer.MAX_VALUE;
			} else {
				windowTerms[j] = 0;
			}
		}
		int k = 0;
		while (true) {
			boolean temp = false;
			for (int j = 0; j < termPosMap.size(); j++) {
				ArrayList<Integer> positionVector = termPosMap
						.get(relevantTermList.get(j));
				if (positionVector.size() > 0 && positionVector.size() > k) {
					temp = true;
					int currentValue = windowTerms[j];
					int newValue = positionVector.get(k);
					windowTerms[j] = newValue;
					int tempWindowLength = 0;
					if ((tempWindowLength = fetchMinimumWindowLength(
							windowTerms, currentWindowLength)) == currentWindowLength) {
						windowTerms[j] = currentValue;
					} else {
						windowTerms[j] = newValue;
						currentWindowLength = tempWindowLength;
					}
				}
				k++;
			}
			if (!temp)
				break;
		}
		return windowTerms;
	}

	private static int fetchMinimumWindowLength(int[] windowTerms,
			int currentWindowLength) {

		int minimum = findMinimumElementOfArray(windowTerms);
		int maximum = findMaximumElementOfArray(windowTerms);
		if (maximum - minimum < currentWindowLength) {
			return maximum - minimum;
		} else {
			return currentWindowLength;
		}
	}

	public static int findMaximumElementOfArray(int[] windowTerms) {

		int maximumElement = Integer.MIN_VALUE;
		for (int term : windowTerms) {
			if (term > maximumElement) {
				maximumElement = term;
			}
		}
		return maximumElement;
	}

	public static int findMinimumElementOfArray(int[] windowTerms) {

		int minimumElement = Integer.MAX_VALUE;
		for (int term : windowTerms) {
			if (term < minimumElement) {
				minimumElement = term;
			}
		}
		return minimumElement;
	}

}
