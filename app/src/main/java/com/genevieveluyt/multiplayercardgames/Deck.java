package com.genevieveluyt.multiplayercardgames;

import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Genevieve on 30/08/2015.
 */
public class Deck extends CardCollection {

	// Deck types
	public static final int STANDARD = 0;   // standard 52-card collection
	public static final int EMPTY = 1;      // can be used as a playing or discard pile
	public static final int JOKER = 2;      // standard 52-card collection with 2 jokers

	private ArrayList<Card> deck = collection; // alias
	private ImageView deckView;

	// Option that can be set by using setFrontFacing()
	private boolean frontFacing = true;

	Deck(int type, ImageView deckView) {
		super();
		this.deckView = deckView;

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

    Deck (String data, ImageView deckView) {
        super(data);
	    this.deckView = deckView;
    }

	public void setFrontFacing(boolean val) {
		frontFacing = val;
		if (!frontFacing)
			deckView.setImageResource(R.drawable.card_back);
	}

	public boolean isFrontFacing() { return frontFacing; }

	// Removes the top card from the deck and returns it
	public Card draw() {
		return remove(peek());
	}

	// Returns the top card of the deck without removing it
	public Card peek() {
		return deck.get(0);
	}

	// Do I need this method?? It just calls add()
	public Card play(Card card) {
		return add(card);
	}

	// Do I need this method?? It just calls add()
	public Card discard(Card card) {
		return add(card);
	}

	public void reshuffle() { Collections.shuffle(deck); }

	@Override
	public Card remove(Card card) {
		deck.remove(card);
		if (deck.isEmpty())
			deckView.setVisibility(View.INVISIBLE);
		return card;
	}

	@Override
	public Card add(Card card) {
		deckView.setVisibility(View.VISIBLE);   // In case it was previously empty
		if (isFrontFacing()) {
			deckView.setImageResource(card.getImg());
			if (MainActivity.DEBUG) System.out.println("Putting " + card.toString() + " onto play deck");
		}
		return card;
	}
}
