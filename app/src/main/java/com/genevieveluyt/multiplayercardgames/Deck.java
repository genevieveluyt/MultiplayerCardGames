package com.genevieveluyt.multiplayercardgames;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Genevieve on 30/08/2015.
 */
public class Deck extends CardCollection {

	// Deck types
	public static final int STANDARD = 0;   // standard 52-card collection
	public static final int EMPTY = 1;      // can be used as a playing or discard pile
	public static final int JOKER = 2;      // standard 52-card collection with 2 jokers

	LinkedList<Card> deck = (LinkedList<Card>) collection;

	Deck(int type) {
		deck = new LinkedList<Card>();

		// Deck type is STANDARD or JOKER
		if (type != EMPTY) {
			// Both decks start with a standard deck
			for (int suit = 1; suit <= Card.NUM_SUITS; suit++) {
				for (int rank = 1; rank <= Card.NUM_RANKS; rank++) {
					deck.add(new Card(suit, rank));
				}
			}

			// Joker decks have 2 jokers
			if (type == JOKER) {
				deck.add(new Card(Card.NONE, Card.JOKER));
				deck.add(new Card(Card.NONE, Card.JOKER));
			}

			// Shuffle deck
			Collections.shuffle(deck);
		}
	}

	public Card draw() {
		return deck.remove(0);
	}

	public Card peek() {
		return deck.get(0);
	}

	public void play(Card card) {
		deck.add(card);
	}

	public void discard(Card card) {
		deck.add(card);
	}

	public void reshuffle() {
		Collections.shuffle(deck);
	}
}
