package com.genevieveluyt.multiplayercardgames;

import java.util.LinkedList;

/**
 * Created by Genevieve on 30/08/2015.
 */
public abstract class CardCollection {

	protected LinkedList<Card> collection;

    public CardCollection() {
        collection = new LinkedList<>();
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

	void loadData (String data) {
		for (int i = 0; i < data.length(); i += 3) {
			collection.addLast(new Card(Integer.parseInt(data.substring(i, i + 3))));
		}
		// TODO delete
		if (collection.size() == 1) System.out.println("playdeck: " + collection.getFirst());
	}

	String getData() {
		StringBuilder data = new StringBuilder();
        for (Card c : collection) {
            data.append(c.getId());
        }
		return data.toString();
	}

	public Card add(Card card) { collection.addFirst(card); return card; }

	public Card remove(Card card) { collection.remove(card); return card; }

	public String toString() {
		StringBuilder str = new StringBuilder();
		for (Card card : collection)
			str.append(card + ", ");
		str.setLength(str.length()-2);

		return str.toString();
	}
}
