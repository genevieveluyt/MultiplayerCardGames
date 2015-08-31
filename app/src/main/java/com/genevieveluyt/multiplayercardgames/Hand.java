package com.genevieveluyt.multiplayercardgames;

import java.util.ArrayList;

/**
 * Created by Genevieve on 30/08/2015.
 */
public class Hand extends CardCollection {

	ArrayList<Card> hand = (ArrayList<Card>) collection;

	public Hand() {
		hand = new ArrayList<Card>();
	}

	public void draw(Deck deck) {
		hand.add(deck.draw());
	}

	// return false if card not in hand
	public boolean play(Card card, Deck deck) {
		if (!hand.contains(card))
			return false;
		deck.play(card);
		hand.remove(card);
		return true;
	}

	public boolean giveTo(Card card, Hand oppHand) {
		if (!hand.contains(card))
			return false;
		oppHand.receive(card);
		play(card);
		return true;
	}

	public boolean takeFrom(Card card, Hand oppHand) {
		if (!oppHand.contains(card))
			return false;
		oppHand.play(card);
		receive(card);
		return true;
	}

	public void receive(Card card) {
		hand.add(card);
	}

	// return false if card not in hand
	public boolean play(Card card) {
		if (!hand.contains(card))
			return false;
		hand.remove(card);
		return true;
	}
}
