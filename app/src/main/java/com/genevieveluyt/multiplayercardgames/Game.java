package com.genevieveluyt.multiplayercardgames;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Genevieve on 03/09/2015.
 */
public abstract class Game {

	// Game variants
	public static final int CRAZY_EIGHTS = 1;

	// Use in getting next/previous participant ID
	public static final int ROUND_ROBIN = 50;

	// Separates deck and hand segments in data
    protected static final char separator = '\n';

	public abstract void initBoard();

	public abstract byte[] saveData();

	public abstract void loadData(byte[] data);

	public abstract String getGameName();

	public abstract String getNextParticipantId();

	public static String getGameName(Activity activity, int gameVariant) {
		return activity.getResources().getStringArray(R.array.game_names_array)[gameVariant-1];
	}

	public static String getNextParticipantId(int turnStyle, ArrayList<String> participantIds, int currParticipantIndex) {
		String nextParticipant = null;

		switch (turnStyle) {
			case ROUND_ROBIN:
				nextParticipant = participantIds.get((currParticipantIndex + 1) % participantIds.size());
		}

		return nextParticipant;
	}

	public static String getPreviousParticipantId(int turnStyle, ArrayList<String> participantIds, int currParticipantIndex) {
		String prevParticipant = null;

		switch (turnStyle) {
			case ROUND_ROBIN:
				prevParticipant = participantIds.get((currParticipantIndex + participantIds.size() - 1) % participantIds.size());
		}

		return prevParticipant;
	}

	public static String getNextPlayerName(int turnStyle, ArrayList<String> playerNames, int currPlayerIndex) {
		return getNextParticipantId(turnStyle, playerNames, currPlayerIndex);
	}

	public static String getPrevPlayerName(int turnStyle, ArrayList<String> playerNames, int currPlayerIndex) {
		return getPreviousParticipantId(turnStyle, playerNames, currPlayerIndex);
	}

	public static int getPlayerDrawable(int player) {
		int drawable = 0;
		switch(player) {
			case 0: drawable = R.drawable.player_red; break;
			case 1: drawable = R.drawable.player_indigo; break;
			case 2: drawable = R.drawable.player_orange; break;
			case 3: drawable = R.drawable.player_purple; break;
			case 4: drawable = R.drawable.player_green; break;
			case 5: drawable = R.drawable.player_pink; break;
			case 6: drawable = R.drawable.player_blue; break;
			case 7: drawable = R.drawable.player_yellow; break;
		}
		return drawable;
	}

	public AlertDialog makeYouWonDialog(Activity activity, final GameCallbacks mCallbacks) {
		return (new AlertDialog.Builder(activity))
				.setMessage(R.string.you_won)
				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						mCallbacks.onGameWon();
					}
				})
				.create();
	}

	public static AlertDialog makeCancelDialog(Activity activity, final GameCallbacks mCallbacks) {
		return (new AlertDialog.Builder(activity))
				.setMessage(R.string.confirm_cancel)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mCallbacks.onGameCancelled();
					}
				})
				.setNegativeButton(R.string.no, null)
				.create();
	}

	public interface GameCallbacks {

		void onTurnEnded();

		void onGameCancelled();

		void onGameWon();

		void onLoadDataError();
	}
}
