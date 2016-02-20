package com.genevieveluyt.multiplayercardgames;

import java.util.HashMap;

/**
 * Created by Genevieve on 03/09/2015.
 */
public abstract class GameBoard {

	// Game types
	public static final int CRAZY_EIGHTS = 0;

	// Separates deck and hand segments in data
    protected static final char separator = '\n';

	HashMap<String, Hand> hands;     // Player ID, Hand of cards

    public GameBoard() {
        hands = new HashMap<>();
    }

	public abstract void initBoard();

	public abstract byte[] saveData();

	public abstract void loadData(byte[] data);

	public abstract int getGameType();
}
