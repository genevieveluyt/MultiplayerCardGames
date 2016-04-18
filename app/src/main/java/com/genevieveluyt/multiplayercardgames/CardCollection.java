package com.genevieveluyt.multiplayercardgames;

import java.util.LinkedList;

/**
 * Created by Genevieve on 30/08/2015.
 */
public class CardCollection extends LinkedList<Card> {

	void loadData (String data) {
		for (int i = 0; i < data.length(); i += 3) {
			super.addLast(new Card(Integer.parseInt(data.substring(i, i + 3))));
		}
	}

	String getData() {
		StringBuilder data = new StringBuilder();
        for (Card c : this) {
            data.append(c.getId());
        }
		return data.toString();
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		for (Card card : this)
			str.append(card + ", ");
		if (str.length() > 0)
			str.setLength(str.length()-2);

		return str.toString();
	}
}
