package com.chinappa.search.engine.util;

import java.util.ArrayList;

public class LevenshteinDistanceUtil {
	private static int fetchLevenshteinDistance(String sfirstString,
			String secondString) {
		// degenerate cases
		if (sfirstString == secondString)
			return 0;
		if (sfirstString.length() == 0)
			return secondString.length();
		if (secondString.length() == 0)
			return sfirstString.length();

		int[] m = new int[secondString.length() + 1];
		int[] n = new int[secondString.length() + 1];

		for (int i = 0; i < m.length; i++)
			m[i] = i;

		for (int i = 0; i < sfirstString.length(); i++) {
			n[0] = i + 1;

			// use formula to fill in the rest of the row
			for (int j = 0; j < secondString.length(); j++) {
				int cost = (sfirstString.charAt(i) == secondString.charAt(j)) ? 0
						: 1;
				n[j + 1] = Math.min(n[j] + 1,
						Math.min(m[j + 1] + 1, m[j] + cost));
			}

			for (int j = 0; j < m.length; j++)
				m[j] = n[j];
		}

		return n[secondString.length()];
	}

	public static String getTopMatch(String queryTerm,
			ArrayList<String> dictionary) {
		
		int minimumDistance = Integer.MAX_VALUE;
		String topMatch = null;
		for(String word: dictionary){
			int distance = fetchLevenshteinDistance(queryTerm, word);
			if(distance<minimumDistance){
				minimumDistance = distance;
				topMatch = word;
			}
		}
		if(minimumDistance==0){
			topMatch = null;
		}
		return topMatch;
	}
}
