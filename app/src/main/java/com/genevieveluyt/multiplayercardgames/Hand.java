package com.genevieveluyt.multiplayercardgames;

import android.graphics.Color;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Genevieve on 30/08/2015.
 */
public class Hand {

	private CardCollection hand;
	private ArrayList<Card> selected;
	private final int selectedRaiseAmt = 60;    // How much a selected card will raise above the others, in pixels
	private boolean multiSelect = false;        // Can be set by user
	private boolean selectEnabled = true;

	// XML layouts
	private LinearLayout handLayout;
	private HorizontalScrollView handScrollLayout;

	// Moves cards to the left in hand so they overlap
	private LinearLayout.LayoutParams params =
			new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT) {
				{
					setMargins(-70, 0, 0, 0);
				}
			};


	// Make an empty hand
	public Hand(LinearLayout handLayout) {
		hand = new CardCollection();
		this.handLayout = handLayout;
		this.handScrollLayout = (HorizontalScrollView) handLayout.getParent();
		selected = new ArrayList<>();
	}

	// Make a hand starting with n cards drawn from deck
	public Hand(Deck deck, int n, LinearLayout handLayout) {
		hand = new CardCollection();
		this.handLayout = handLayout;
		this.handScrollLayout = (HorizontalScrollView) handLayout.getParent();
		selected = new ArrayList<>();
		draw(deck, n);
	}

	// Make a hand starting with n cards drawn from deck without adding them to the UI
	public Hand(Deck deck, int n) {
		hand = new CardCollection();
		selected = new ArrayList<>();
		drawVirtual(deck, n);
	}

	// Make a hand by loading data
	Hand(String data, LinearLayout handLayout) {
		hand = new CardCollection();
		this.handLayout = handLayout;
		this.handScrollLayout = (HorizontalScrollView) handLayout.getParent();
		for (int i = 0; i < data.length(); i += 3) {
			add(new Card(Integer.parseInt(data.substring(i, i+3))));
		}
		selected = new ArrayList<>();
	}

	// Make a hand by loading data but without adding the cards to the UI
	Hand(String data) {
		hand = new CardCollection();
		hand.loadData(data);
		selected = new ArrayList<>();
	}

	public void draw(Deck deck) {
		add(deck.draw());
	}

	// draw n cards from deck
	public void draw(Deck deck, int n) {
		for (int i = 0; i < n; i++) {
			draw(deck);
		}
	}

	public void drawVirtual(Deck deck) {
		addVirtual(deck.draw());
	}

	public void drawVirtual (Deck deck, int n) {
		for (int i = 0; i < n; i++) {
			drawVirtual(deck);
		}
	}

	public Card play(Card card, Deck deck) {
		deck.add(card);
		remove(card);

		if (MainActivity.DEBUG) System.out.println("Hand|play(Card, Deck): Played " + card.toString());
		return card;
	}

	public void playSelected(Deck deck) {
		for (Card c : selected)
			play(c, deck);
	}

	public Card giveTo(Card card, Hand oppHand) {
		oppHand.addVirtual(card);
		remove(card);
		return card;
	}

	public Card takeFrom(Card card, Hand oppHand) {
		oppHand.removeVirtual(card);
		add(card);
		return card;
	}

	// Add card to hand and hand UI
	public Card add(Card card) {
		addVirtual(card);

		ImageButton cardView = new ImageButton(handLayout.getContext());
		cardView.setAdjustViewBounds(true);
		cardView.setImageResource(card.getImg());
		cardView.setBackgroundColor(Color.alpha(0));
		cardView.setId(card.getId());
		cardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				select(new Card(v.getId()));
			}
		});
		cardView.setPadding(0, selectedRaiseAmt, 0, 0);
		handLayout.addView(cardView, params);

		// TODO Find a way to scroll all the way to the right so there doesn't have to be
		// right padding in the xml of handLayout (R.id.hand_layout)
		handScrollLayout.smoothScrollBy(handScrollLayout.getMaxScrollAmount(), 0);

		if (MainActivity.DEBUG) System.out.println("Hand|add(Card): Drew " + card.toString());

		return card;
	}

	// Add card to hand without affecting UI
	public void addVirtual(Card card) {
		hand.addLast(card);
	}

	// Remove card from hand and hand UI
	public Card remove(Card card) {
		hand.remove(card);
		if (selected.contains(card)) {
			selected.remove(card);
			deselect(card);
		}
		handLayout.removeView(handLayout.findViewById(card.getId()));
		return card;
	}

	// Remove card from hand without affecting UI
	public void removeVirtual(Card card) {
		hand.remove(card);
	}

	/*
	 * Reselecting a card deselects it
	 * If multiSelect is disabled, selecting another card deselects any previously selected card
	 */
	public void select(Card card) {
		if (selected.contains(card))
			deselect(card);
		else {
			if (!selected.isEmpty() && !multiSelect)
				deselectAll();
			selected.add(card);
			handLayout.findViewById(card.getId()).setPadding(0, 0, 0, selectedRaiseAmt);

			if (MainActivity.DEBUG) System.out.println("Hand|select(Card): Selected " + card.toString());
		}
	}

	public void deselect(Card card) {
		selected.remove(card);
		View cardView = handLayout.findViewById(card.getId());
		cardView.setPadding(0, selectedRaiseAmt, 0, 0);

		if (MainActivity.DEBUG) System.out.println("Hand:deselect(Card): Deselected " + card.toString());
	}

	public void deselectAll() {
		for (Card card : selected)
			deselect(card);
	}

	public ArrayList<Card> getSelected() {
		return selected;
	}

	public void enableMultiSelect() { multiSelect = true; }

	public void disableMultiSelect() {
		if (selected.size() > 1) {
			Card firstSelected = selected.get(0);
			deselectAll();
			select(firstSelected);
		}
	}

	public boolean isMultiSelectEnabled() { return multiSelect; }

	public void enableSelect() { selectEnabled = true; }

	public void disableSelect() { selectEnabled = false; deselectAll(); }

	public boolean isSelectEnabled() { return selectEnabled; }

	public String getData() {
		return hand.getData();
	}

	public void loadData(String data) {
		hand.loadData(data);
	}

	public boolean isEmpty() {
		return hand.isEmpty();
	}

	public String toString() {
		return hand.toString();
	}
}
