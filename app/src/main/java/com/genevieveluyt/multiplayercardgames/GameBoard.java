package com.genevieveluyt.multiplayercardgames;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Genevieve on 03/09/2015.
 */
public abstract class GameBoard {

	// Separates deck and hand segments in data
    protected final char separator = '\n';

	HashMap<String, Hand> hands;     // Player ID, Hand of cards

    public GameBoard() {
        hands = new HashMap<>();
    }

	public abstract void initBoard();

	public abstract byte[] saveData();

	public abstract void loadData(byte[] data);
}
