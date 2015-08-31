package com.genevieveluyt.multiplayercardgames;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Genevieve on 30/08/2015.
 */
public abstract class CardCollection {

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

	LinkedList<Byte> getData() {
		LinkedList<Byte> data = new LinkedList<Byte>();
		data.add((Byte)(byte)collection.size());
		for (Card c : collection) {
			data.add((Byte)(byte) c.getSuit()); // REVIEW does this have to be Byte.valueOf( (byte) c.getSuit() );
			data.add((Byte)(byte) c.getRank());
		}
		return data;
	}
}
