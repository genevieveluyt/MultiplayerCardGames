package com.genevieveluyt.multiplayercardgames;

/**
 * Created by Genevieve on 30/08/2015.
 */
public class Card {
	public static final int NUM_SUITS = 4;
	public static final int NUM_RANKS = 13; // does NOT include joker

	// Suits
	public static final int NONE = 0;   // Jokers have no suit
	public static final int HEARTS = 1;
	public static final int SPADES = 2;
	public static final int DIAMONDS = 3;
	public static final int CLUBS = 4;

	// Named ranks
	public static final int ACE = 1;
	public static final int JACK = 11;
	public static final int QUEEN = 12;
	public static final int KING = 13;
	public static final int JOKER = 0;

	/*********************************************************************/

	private int suit;
	private int rank;

	public Card(int suit, int rank) {
		this.suit = suit;
		this.rank = rank;
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

	public String toString(){
		if (rank == 0)
			return "Joker";

		StringBuilder str = new StringBuilder();

		switch(rank) {
			case(1): str.append("Ace"); break;
			case(2): str.append("Two"); break;
			case(3): str.append("Three"); break;
			case(4): str.append("Four"); break;
			case(5): str.append("Five"); break;
			case(6): str.append("Six"); break;
			case(7): str.append("Seven"); break;
			case(8): str.append("Eight"); break;
			case(9): str.append("Nine"); break;
			case(10): str.append("Ten"); break;
			case(11): str.append("Jack"); break;
			case(12): str.append("Queen"); break;
			case(13): str.append("King"); break;
		}

		str.append(" of ");

		switch(suit) {
			case(SPADES): str.append("Spades"); break;
			case(HEARTS): str.append("Hearts"); break;
			case(DIAMONDS): str.append("Diamonds"); break;
			case(CLUBS): str.append("Clubs"); break;
		}

		return str.toString();
	}
}
