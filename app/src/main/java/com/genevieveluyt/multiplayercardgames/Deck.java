package com.genevieveluyt.multiplayercardgames;

import android.view.View;
import android.widget.ImageView;

import java.util.Collections;

/**
 * Created by Genevieve on 30/08/2015.
 */
public class Deck {

	// Deck types
	public static final int STANDARD = 0;   // standard 52-card collection
	public static final int EMPTY = 1;      // can be used as a playing or discard pile
	public static final int JOKER = 2;      // standard 52-card collection with 2 jokers

	private CardCollection deck;
	private ImageView deckView;

	// If true, will show card contents, otherwise back of card
	private boolean frontFacing;

	// Adds card to deck without affecting UI
	Deck(int type) {
		deck = new CardCollection();
		initDeck(type);
	}

	Deck(int type, boolean frontFacing, ImageView deckView) {
		deck = new CardCollection();
		this.frontFacing = frontFacing;
		this.deckView = deckView;

		initDeck(type);
		if (type != EMPTY) {
			if (frontFacing)
				deckView.setImageResource(peek().getImg());
			else
				deckView.setImageResource(R.drawable.card_back);
			deckView.setVisibility(View.VISIBLE);
		} else deckView.setVisibility(View.INVISIBLE);
	}

	// Make a deck from data
    Deck(String data, boolean frontFacing, ImageView deckView) {
		deck = new CardCollection();
	    this.deckView = deckView;
	    this.frontFacing = frontFacing;
		deck.loadData(data);
	    if (!deck.isEmpty()) {
		    if (frontFacing)
			    deckView.setImageResource(peek().getImg());
		    else
			    deckView.setImageResource(R.drawable.card_back);
		    deckView.setVisibility(View.VISIBLE);
	    } else deckView.setVisibility(View.INVISIBLE);
    }

	void initDeck(int type) {
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

	public String getData() {
		return deck.getData();
	}

	public void loadData(String data) {
		deck.loadData(data);
	}

	public boolean isFrontFacing() { return frontFacing; }

	// Removes the top card from the deck UI and returns it
	public Card draw() {
		return remove(peek());
	}

	// Removes the top card from the deck and returns it
	public Card drawVirtual() {
		Card c = deck.peek();
		deck.remove(c);
		return c;
	}

	// Returns the top card of the deck without removing it
	public Card peek() { return deck.peek();
	}

	public void reshuffle() { Collections.shuffle(deck); }

	public Card remove(Card card) {
		deck.remove(card);
		if (deck.isEmpty())
			deckView.setVisibility(View.INVISIBLE);
		else if (isFrontFacing())
			deckView.setImageResource(peek().getImg());
		return card;
	}

	public void removeVirtual(Card card) {
		deck.remove(card);
	}

	public Card add(Card card) {
		addVirtual(card);
		deckView.setVisibility(View.VISIBLE);   // In case it was previously empty
		if (isFrontFacing()) {
			deckView.setImageResource(card.getImg());
			if (MainActivity.DEBUG) System.out.println("Deck|Card(Card card): Putting " + card.toString() + " onto play deck");
		}
		return card;
	}

	// Add card to deck without affecting UI
	public void addVirtual(Card card) {
		deck.addFirst(card);
	}

	public boolean isEmpty() {
		return deck.isEmpty();
	}

	public String toString() {
		return deck.toString();
	}

	public int size() { return deck.size(); }
}
