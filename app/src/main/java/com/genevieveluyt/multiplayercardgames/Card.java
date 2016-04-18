package com.genevieveluyt.multiplayercardgames;

import android.util.SparseIntArray;

/**
 * Created by Genevieve on 30/08/2015.
 */
public class Card {
	public static final int NUM_SUITS = 4;
	public static final int NUM_RANKS = 13; // does NOT include joker

	// Suits
	public static final int HEARTS = 1;
	public static final int SPADES = 2;
	public static final int DIAMONDS = 3;
	public static final int CLUBS = 4;
	public static final int NONE = 5;   // Jokers have no suit, not 0 b/c the id must be 3 digits

	// Named ranks
	public static final int ACE = 1;
	public static final int JACK = 11;
	public static final int QUEEN = 12;
	public static final int KING = 13;
	public static final int JOKER = 0;

	// Maps card ID to image resource in R.id corresponding to said card
	private static SparseIntArray cardImages = new SparseIntArray(53) {
		{
			//Unnamed init block.
			append(new Card(HEARTS, ACE).getId(), R.drawable.card_hearts_ace);
			append(new Card(HEARTS, 2).getId(), R.drawable.card_hearts_2);
			append(new Card(HEARTS, 3).getId(), R.drawable.card_hearts_3);
			append(new Card(HEARTS, 4).getId(), R.drawable.card_hearts_4);
			append(new Card(HEARTS, 5).getId(), R.drawable.card_hearts_5);
			append(new Card(HEARTS, 6).getId(), R.drawable.card_hearts_6);
			append(new Card(HEARTS, 7).getId(), R.drawable.card_hearts_7);
			append(new Card(HEARTS, 8).getId(), R.drawable.card_hearts_8);
			append(new Card(HEARTS, 9).getId(), R.drawable.card_hearts_9);
			append(new Card(HEARTS, 10).getId(), R.drawable.card_hearts_10);
			append(new Card(HEARTS, JACK).getId(), R.drawable.card_hearts_jack);
			append(new Card(HEARTS, QUEEN).getId(), R.drawable.card_hearts_queen);
			append(new Card(HEARTS, KING).getId(), R.drawable.card_hearts_king);
			append(new Card(SPADES, ACE).getId(), R.drawable.card_spades_ace);
			append(new Card(SPADES, 2).getId(), R.drawable.card_spades_2);
			append(new Card(SPADES, 3).getId(), R.drawable.card_spades_3);
			append(new Card(SPADES, 4).getId(), R.drawable.card_spades_4);
			append(new Card(SPADES, 5).getId(), R.drawable.card_spades_5);
			append(new Card(SPADES, 6).getId(), R.drawable.card_spades_6);
			append(new Card(SPADES, 7).getId(), R.drawable.card_spades_7);
			append(new Card(SPADES, 8).getId(), R.drawable.card_spades_8);
			append(new Card(SPADES, 9).getId(), R.drawable.card_spades_9);
			append(new Card(SPADES, 10).getId(), R.drawable.card_spades_10);
			append(new Card(SPADES, JACK).getId(), R.drawable.card_spades_jack);
			append(new Card(SPADES, QUEEN).getId(), R.drawable.card_spades_queen);
			append(new Card(SPADES, KING).getId(), R.drawable.card_spades_king);
			append(new Card(DIAMONDS, ACE).getId(), R.drawable.card_diamonds_ace);
			append(new Card(DIAMONDS, 2).getId(), R.drawable.card_diamonds_2);
			append(new Card(DIAMONDS, 3).getId(), R.drawable.card_diamonds_3);
			append(new Card(DIAMONDS, 4).getId(), R.drawable.card_diamonds_4);
			append(new Card(DIAMONDS, 5).getId(), R.drawable.card_diamonds_5);
			append(new Card(DIAMONDS, 6).getId(), R.drawable.card_diamonds_6);
			append(new Card(DIAMONDS, 7).getId(), R.drawable.card_diamonds_7);
			append(new Card(DIAMONDS, 8).getId(), R.drawable.card_diamonds_8);
			append(new Card(DIAMONDS, 9).getId(), R.drawable.card_diamonds_9);
			append(new Card(DIAMONDS, 10).getId(), R.drawable.card_diamonds_10);
			append(new Card(DIAMONDS, JACK).getId(), R.drawable.card_diamonds_jack);
			append(new Card(DIAMONDS, QUEEN).getId(), R.drawable.card_diamonds_queen);
			append(new Card(DIAMONDS, KING).getId(), R.drawable.card_diamonds_king);
			append(new Card(CLUBS, ACE).getId(), R.drawable.card_clubs_ace);
			append(new Card(CLUBS, 2).getId(), R.drawable.card_clubs_2);
			append(new Card(CLUBS, 3).getId(), R.drawable.card_clubs_3);
			append(new Card(CLUBS, 4).getId(), R.drawable.card_clubs_4);
			append(new Card(CLUBS, 5).getId(), R.drawable.card_clubs_5);
			append(new Card(CLUBS, 6).getId(), R.drawable.card_clubs_6);
			append(new Card(CLUBS, 7).getId(), R.drawable.card_clubs_7);
			append(new Card(CLUBS, 8).getId(), R.drawable.card_clubs_8);
			append(new Card(CLUBS, 9).getId(), R.drawable.card_clubs_9);
			append(new Card(CLUBS, 10).getId(), R.drawable.card_clubs_10);
			append(new Card(CLUBS, JACK).getId(), R.drawable.card_clubs_jack);
			append(new Card(CLUBS, QUEEN).getId(), R.drawable.card_clubs_queen);
			append(new Card(CLUBS, KING).getId(), R.drawable.card_clubs_king);
			append(new Card(NONE, JOKER).getId(), R.drawable.card_joker);
		}
	};

	/*********************************************************************/

	private int suit;
	private int rank;

	public Card(int suit, int rank) {
		this.suit = suit;
		this.rank = rank;
	}

	public Card(int id) {
		this.suit = id / 100;    // integer division
		this.rank = id - suit * 100;
	}

	public int getSuit() {
		return suit;
	}

	public int getRank() {
		return rank;
	}

	// Compares cards according to rank
	public int compareTo(Card card2) {
		return ((Integer) rank).compareTo((Integer) card2.getRank());
	}

	public String toString() {
		if (rank == 0)
			return "Joker";

		StringBuilder str = new StringBuilder();

		switch (rank) {
			case (1):
				str.append("Ace");
				break;
			case (2):
				str.append("Two");
				break;
			case (3):
				str.append("Three");
				break;
			case (4):
				str.append("Four");
				break;
			case (5):
				str.append("Five");
				break;
			case (6):
				str.append("Six");
				break;
			case (7):
				str.append("Seven");
				break;
			case (8):
				str.append("Eight");
				break;
			case (9):
				str.append("Nine");
				break;
			case (10):
				str.append("Ten");
				break;
			case (11):
				str.append("Jack");
				break;
			case (12):
				str.append("Queen");
				break;
			case (13):
				str.append("King");
				break;
		}

		str.append(" of ");

		switch (suit) {
			case (SPADES):
				str.append("Spades");
				break;
			case (HEARTS):
				str.append("Hearts");
				break;
			case (DIAMONDS):
				str.append("Diamonds");
				break;
			case (CLUBS):
				str.append("Clubs");
				break;
		}

		return str.toString();
	}

	/*
	 * Card ID is a 3-digit number
	 * First digit represents suit
	 * Last two digits represent rank
	 * eg. Ace of Spades is 201
	 */
	int getId() {
		return suit * 100 + rank;
	}

	// R.id of image corresponding to card
	public int getImg() {
		return cardImages.get(getId());
	}

	// R.id of image corresponding to card
	public static int getImg(Card card) {
		return cardImages.get(card.getId());
	}

	public static String suitToString(int suit) {
		switch (suit) {
			case CLUBS: return "Clubs";
			case DIAMONDS: return "Diamonds";
			case HEARTS: return "Hearts";
			case SPADES: return "Spades";
			case NONE: return "None";
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		//return this.rank == ((Card) obj).getRank() && this.suit == ((Card) obj).getSuit();
		return getId() == ((Card) obj).getId();
	}
}
