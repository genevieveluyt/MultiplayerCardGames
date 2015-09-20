package com.genevieveluyt.multiplayercardgames;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Genevieve on 30/08/2015.
 */
public class Hand extends CardCollection {

	private ArrayList<Card> hand = collection;  // alias
	private ArrayList<Card> selected;
	private final int selectedRaiseAmt = 60;    // How much a selected card will raise above the others, in pixels
	private boolean multiSelect = false;        // Can be set by user

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
		super();
		this.handLayout = handLayout;
		this.handScrollLayout = (HorizontalScrollView) handLayout.getParent();
		selected = new ArrayList<>();
	}

	// Make a hand starting with n cards drawn from deck
	public Hand(Deck deck, int n, LinearLayout handLayout) {
		super();
		this.handLayout = handLayout;
		this.handScrollLayout = (HorizontalScrollView) handLayout.getParent();
		selected = new ArrayList<>();
		draw(deck, n);
	}

	// Make a hand by loading data
	Hand(String data, LinearLayout handLayout) {
		super(data);
		this.handLayout = handLayout;
		this.handScrollLayout = (HorizontalScrollView) handLayout.getParent();
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

	// return false if card not in hand
	public boolean play(Card card, Deck deck) {
		if (!hand.contains(card))
			return false;
		deck.play(card);
		remove(card);
		return true;
	}

	public void playSelected(Deck deck) {
		for (Card c : selected)
			play(c, deck);
	}

	public boolean giveTo(Card card, Hand oppHand) {
		if (!hand.contains(equals(card)))
			return false;
		oppHand.add(card);
		remove(card);
		return true;
	}

	public boolean takeFrom(Card card, Hand oppHand) {
		if (!oppHand.contains(card))
			return false;
		oppHand.remove(card);
		add(card);
		return true;
	}

	@Override
	public Card add(Card card) {
		hand.add(card);

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

		if (MainActivity.DEBUG) System.out.println("Drew " + card.toString());

		return card;
	}

	@Override
	public Card remove(Card card) {
		hand.remove(card);
		if (selected.contains(card)) {
			selected.remove(card);
			deselect(card);
		}
		handLayout.removeView(handLayout.findViewById(card.getId()));

		if (MainActivity.DEBUG) System.out.println("Played " + card.toString());
		return card;
	}

	/*
	 * Reselecting a card deselects it
	 * If multiSelect is disabled, selecting another card deselects any previously selected card
	 */
	public void select(Card card) {
		if (!selected.contains(card)) {
			if (!selected.isEmpty() && !multiSelect)
				deselect(selected.get(0));
			selected.add(card);
			if (MainActivity.DEBUG) System.out.println("Selected " + card.toString());
			handLayout.findViewById(card.getId()).setPadding(0, 0, 0, selectedRaiseAmt);
		} else
			deselect(card);
	}

	public void deselect(Card card) {
		selected.remove(card);
		if (MainActivity.DEBUG) System.out.println("Deselected " + card.toString());
		View cardView = handLayout.findViewById(card.getId());
		cardView.setPadding(0, selectedRaiseAmt, 0, 0);
	}

	public ArrayList<Card> getSelected() {
		return selected;
	}

	public void allowMultiSelect(boolean bool) {
		multiSelect = bool;
		// if switching from multi select to single select, clear selected cards
		if (!multiSelect && selected.size() > 1) {
			selected.clear();
		}
	}

	public boolean multiSelectAllowed() {
		return multiSelect;
	}
}
