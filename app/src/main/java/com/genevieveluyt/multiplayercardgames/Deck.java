package com.genevieveluyt.multiplayercardgames;

import android.view.View;
import android.widget.ImageView;

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

	private LinkedList<Card> deck = collection; // alias
	private ImageView deckView;

	// If true, will show card contents, otherwise back of card
	private boolean frontFacing;

	// Adds card to deck without affecting UI
	Deck(int type) {
		super();
		initDeck(type);
	}

	Deck(int type, boolean frontFacing, ImageView deckView) {
		super();
		this.frontFacing = frontFacing;
		this.deckView = deckView;

		initDeck(type);
		if (type != EMPTY) {
			if (frontFacing)
				deckView.setImageResource(peek().getImg());
			else
				deckView.setImageResource(R.drawable.card_back);
		}
	}

	// Make a deck from data
    Deck(String data, boolean frontFacing, ImageView deckView) {
        super();
	    this.deckView = deckView;
	    this.frontFacing = frontFacing;
		super.loadData(data);
	    if (frontFacing) {
		    deckView.setImageResource(peek().getImg());
		    deckView.setVisibility(View.VISIBLE);
	    } else
		    deckView.setImageResource(R.drawable.card_back);
    }

	void initDeck(int type) {
		if (type != EMPTY) {
			// Both decks start with a standard deck
			for (int suit = 1; suit <= Card.NUM_SUITS; suit++) {
				for (int rank = 1; rank <= Card.NUM_RANKS; rank++) {
					addVirtual(new Card(suit, rank));
				}
			}

			// Joker decks have 2 jokers
			if (type == JOKER) {
				addVirtual(new Card(Card.NONE, Card.JOKER));
				addVirtual(new Card(Card.NONE, Card.JOKER));
			}

			// Shuffle deck
			Collections.shuffle(deck);
		}
	}

	public boolean isFrontFacing() { return frontFacing; }

	// Removes the top card from the deck and returns it
	public Card draw() {
		return remove(peek());
	}

	public Card drawVirtual() {
		return removeVirtual(peek());
	}

	// Returns the top card of the deck without removing it
	public Card peek() { return deck.peek(); }

	public void reshuffle() { Collections.shuffle(deck); }

	@Override
	public Card remove(Card card) {
		removeVirtual(card);
		if (deck.isEmpty())
			deckView.setVisibility(View.INVISIBLE);
		else if (isFrontFacing())
			deckView.setImageResource(peek().getImg());
		return card;
	}

	public Card removeVirtual(Card card) {
		deck.remove(card);
		return card;
	}

	@Override
	public Card add(Card card) {
		addVirtual(card);
		deckView.setVisibility(View.VISIBLE);   // In case it was previously empty
		if (isFrontFacing()) {
			deckView.setImageResource(card.getImg());
			if (MainActivity.DEBUG) System.out.println("Deck|Card(Card card): Putting " + card.toString() + " onto play deck");
		}
		return card;
	}

	public Card addVirtual(Card card) {
		deck.addFirst(card);
		return card;
	}
}
