package com.genevieveluyt.multiplayercardgames;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;

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

	public static Dialog makeYouWonDialog(Activity activity, final GameCallbacks mCallbacks) {
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
		builder.setMessage(R.string.you_won)
				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mCallbacks.onGameWon();
					}
				});
		return builder.create();
	}

	public static interface GameCallbacks {

		void onTurnEnded();

		void onGameCancelled();

		void onGameWon();
	}
}
