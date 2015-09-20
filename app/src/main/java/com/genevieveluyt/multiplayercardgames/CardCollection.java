package com.genevieveluyt.multiplayercardgames;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Genevieve on 30/08/2015.
 */
public abstract class CardCollection {

	protected ArrayList<Card> collection;

    public CardCollection() {
        collection = new ArrayList<Card>();
    }

    CardCollection(String data) {
        for (int i = 0; i < data.length(); i += 3) {
            collection.add(new Card(Integer.parseInt(data.substring(i, i+3))));
        }
    }

	public boolean contains(Card card) {
		return collection.contains(card);
	}

	public int size() {
		return collection.size();
	}

	public boolean isEmpty() {
		return collection.isEmpty();
	}

	String getData() {
		StringBuilder data = new StringBuilder();
        for (Card c : collection) {
            data.append(c.getId());
        }
		return data.toString();
	}

	public Card add(Card card) { collection.add(card); return card; }

	public Card remove(Card card) { collection.remove(card); return card; }
}
