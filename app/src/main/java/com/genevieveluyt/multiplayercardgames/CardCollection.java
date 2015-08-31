package com.genevieveluyt.multiplayercardgames;

import java.util.List;

/**
 * Created by Genevieve on 30/08/2015.
 */
public abstract class CardCollection {
	// Deck types
	public static final int STANDARD = 0;   // standard 52-card collection
	public static final int EMPTY = 1;      // can be used as a playing or discard pile
	public static final int JOKER = 2;      // standard 52-card collection with 2 jokers

	protected List<Card> collection;

	public boolean contains(Card card) {
		return collection.contains(card);
	}

	public int size() {
		return collection.size();
	}

	public boolean isEmpty() {
		return collection.isEmpty();
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		for (Card c : collection) {
			str.append(c.toString());
			str.append("\n");
		}
		return str.toString();
	}
}
