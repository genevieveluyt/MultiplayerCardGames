package com.genevieveluyt.multiplayercardgames;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Genevieve on 03/09/2015.
 *
 * Crazy Eights Rules
 *
 * Goal: Get rid of all your cards
 * Basic Gameplay: Play a single card with the same rank or suit as the pile or an 8. If the player
 * has no such card, draw cards until they do. A player can choose to draw cards even if they
 * have valid cards in their hand
 *
 * Special cards:
 * 8	-	declare a suit which the next player must play (or another 8)
 *
 */
public class CrazyEightsGameBoard extends GameBoard {

	private static final int STARTING_HAND = 8;

	Activity activity;

    // Layouts
	LinearLayout gameLayout;
	LinearLayout handLayout;
	HorizontalScrollView oppLayout;   // TODO move to parent class?

	// Game variables
	//HashMap<String, Hand> hands; in parent class
	int currParticipantIndex;		// index of current player in participantIds and playerNames
	ArrayList<String> participantIds;
	ArrayList<String> playerNames;
	Hand currHand;
	Deck drawDeck;
	Deck playDeck;
	Dialog chooseSuitDialog;
	Dialog youWonDialog;
	Dialog mustPlaySuitDialog;
	GameCallbacks mCallbacks;

	// Gameplay variables
	boolean hasPlayed; 			// current player has placed a card on the play deck
	int chosenSuit;		// suit chosen after playing an 8
	int mustPlaySuit;			// suit chosen by previous player after playing an 8
	String hint;

	View.OnClickListener handClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!hasPlayed) currHand.select(new Card(v.getId()));
		}
	};

	public CrazyEightsGameBoard(int currParticipantIndex, ArrayList<String> participantIds, ArrayList<String> playerNames, byte[] data, Activity activity) {
		super();
		this.currParticipantIndex = currParticipantIndex;
		this.participantIds = participantIds;
		this.playerNames = playerNames;
		this.gameLayout = (LinearLayout) activity.findViewById(R.id.gameplay_layout);
		this.handLayout = (LinearLayout) activity.findViewById(R.id.hand_layout);
		this.oppLayout = (HorizontalScrollView) activity.findViewById(R.id.opponent_scroll_layout); // TODO temp
		this.activity = activity;
		hasPlayed = false;
		chosenSuit = 0;
		mustPlaySuit = 0;
		if (data == null)
			initBoard();
		else {
			loadData(data);
			activateGUI();
		}
	}

	@Override
	public void initBoard() {
		if (MainActivity.DEBUG) System.out.println("CrazyEightsGameBoard|initBoard(): Initializing game board");
		drawDeck = new Deck(Deck.STANDARD);
		playDeck = new Deck(Deck.EMPTY);
		for (String player : playerNames) {
			hands.put(player, new Hand(drawDeck, STARTING_HAND));
			if (MainActivity.DEBUG) System.out.println(player + " hand: " + hands.get(player));
		}
		playDeck.addVirtual(drawDeck.drawVirtual());
	}

	/* Data format:
		game id | draw deck data | play deck data | chosen suit | number of players | player names and their hand data
	 */
	@Override
	public byte[] saveData() {
		if (MainActivity.DEBUG) System.out.println("CrazyEightsGameBoard|loadData(byte[]): Saving game data");
        StringBuilder dataStr = new StringBuilder();
		dataStr.append(getGameType()).append(separator)
        .append(drawDeck.getData()).append(separator)
        .append(playDeck.getData()).append(separator)
		.append(playerNames.size()).append(separator);
        for (String playerName : playerNames) {
	        dataStr.append(playerName).append(separator)
            .append(hands.get(playerName).getData())
            .append(separator);
        }
		dataStr.append(chosenSuit);

        return dataStr.toString().getBytes(Charset.forName("UTF-8"));
	}

	@Override
	public void loadData(byte[] data) {
		if (MainActivity.DEBUG) System.out.println("CrazyEightsGameBoard|loadData(byte[]): Loading game data");

        String dataStr = new String(data, Charset.forName("UTF-8"));
        String[] dataArr = dataStr.split(String.valueOf(separator));

        drawDeck = new Deck(dataArr[1], false, (ImageView) gameLayout.findViewById(R.id.drawdeck_view));
        playDeck = new Deck(dataArr[2], true, (ImageView) gameLayout.findViewById(R.id.playdeck_view));

		int numPlayers = Integer.parseInt(dataArr[3]);

        for (int i = 4; i < 4 + numPlayers*2; i+=2) {
	        String playerName = dataArr[i];
	        if (playerName.equals(playerNames.get(currParticipantIndex))) {
		        currHand = new Hand(dataArr[i+1], handLayout, handClickListener);
		        hands.put(playerName, currHand);
	        } else
		        hands.put(playerName, new Hand(dataArr[i+1]));
	        if (MainActivity.DEBUG) System.out.println(playerName + " hand: " + hands.get(playerName));
        }
		mustPlaySuit = Integer.parseInt(dataArr[4 + numPlayers*2]);
	}

	@Override
	public int getGameType() {
		return GameBoard.CRAZY_EIGHTS;
	}

	@Override
	public String getGameName() { return getGameName(activity, getGameType()); }

	@Override
	public String getNextParticipantId() {
		return GameBoard.getNextParticipantId(GameBoard.ROUND_ROBIN, participantIds, currParticipantIndex);
	}

	private void activateGUI() {

		((TextView) activity.findViewById(R.id.game_title)).setText(getGameName());

		try {
			mCallbacks = (GameCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement GameCallbacks.");
		}

		View.OnClickListener gameClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.drawdeck_view:
						currHand.draw(drawDeck);
						if (drawDeck.isEmpty()) { // TODO what if all cards are in players' hands?
							// if ran out of cards to draw, reshuffle the play deck and use as draw deck
							Card topCard = playDeck.drawVirtual();
							drawDeck = playDeck;
							drawDeck.reshuffle();
							activity.findViewById(R.id.drawdeck_view).setVisibility(View.VISIBLE);
							playDeck = new Deck(Deck.EMPTY, true, (ImageView) activity.findViewById(R.id.playdeck_view));
							playDeck.add(topCard);
						}
						break;
					case R.id.playdeck_view:
						if (validPlay()) {
							currHand.playSelected(playDeck);
							hasPlayed = true;
							if (currHand.isEmpty()) {
								youWonDialog.show();
							} else if (playDeck.peek().getRank() == 8) {
								hint = activity.getString(R.string.gameid_1_you_played_8_hint);
								chooseSuitDialog.show();
							} else
								hint = activity.getString(R.string.gameid_1_already_played_hint);
						} else if (hasPlayed && playDeck.peek().getRank() == 8)
							chooseSuitDialog.show();
				}
			}
		};

		View.OnClickListener menuClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()){
					case R.id.hint_button:
						if (!hasPlayed && mustPlaySuit != 0)
							mustPlaySuitDialog.show();
						else
							BaseGameUtils.makeSimpleDialog(activity, getGameName(), hint).show();
						break;
					case R.id.cancel_button:
						makeCancelDialog(activity, mCallbacks).show();
						break;
					case R.id.end_turn_button:
						if (hasPlayed) {
							if (playDeck.peek().getRank() == 8 && chosenSuit == 0)
								chooseSuitDialog.show();
							else
								mCallbacks.onTurnEnded();
						}
				}
			}
		};

		gameLayout.findViewById(R.id.drawdeck_view).setOnClickListener(gameClickListener);
		gameLayout.findViewById(R.id.playdeck_view).setOnClickListener(gameClickListener);
		gameLayout.findViewById(R.id.hint_button).setOnClickListener(menuClickListener);
		gameLayout.findViewById(R.id.cancel_button).setOnClickListener(menuClickListener);
		gameLayout.findViewById(R.id.end_turn_button).setOnClickListener(menuClickListener);

		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
		builder.setTitle(R.string.choose_suit)
				.setAdapter(new ArrayAdapterWithIcon(activity, R.array.suits_array, R.array.suits_icons_array),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								chosenSuit = which+1;
								if (MainActivity.DEBUG)
									System.out.println("CrazyEightsGameBoard|activateGUI(): chose suit " + Card.suitToString(chosenSuit));
							}
				});
		chooseSuitDialog = builder.create();
		youWonDialog = makeYouWonDialog(activity, mCallbacks);

		if (mustPlaySuit == 0)
			if (canPlay())
				hint = activity.getString(R.string.gameid_1_can_play_hint);
			else
				hint = activity.getString(R.string.gameid_1_cant_play_hint);
		else {
			String suit = activity.getResources().getStringArray(R.array.suits_array)[mustPlaySuit-1];
			TypedArray imgArray = activity.getResources().obtainTypedArray(R.array.suits_icons_array);
			hint = getNextPlayerName(GameBoard.ROUND_ROBIN, playerNames, currParticipantIndex) + " "
					+ activity.getString(R.string.gameid_1_8_was_played) + " " + suit + ". "
					+ activity.getString(R.string.Play);

			View dialogView = activity.getLayoutInflater().inflate(R.layout.crazy_eights_dialog, null);
			((TextView) dialogView.findViewById(R.id.suit))
					.setText(suit);
			((ImageView) dialogView.findViewById(R.id.suit_image))
					.setImageResource(imgArray.getResourceId(mustPlaySuit-1, 0));

			builder = new android.app.AlertDialog.Builder(activity);
			builder.setView(dialogView)
					.setMessage(hint)
					.setNeutralButton(R.string.ok, null);

			mustPlaySuitDialog = builder.create();
			mustPlaySuitDialog.show();
		}
	}

	// Returns true if there is a valid play without drawing
	private boolean canPlay() {
		for (Card card : currHand) {
			if (validPlay(card))
				return true;
		}
		return false;
	}

	// Returns true if selected cards are a valid play
	private boolean validPlay() {
		ArrayList<Card> selected = currHand.getSelected();

		if (selected.isEmpty())
			return false;

		for (Card card : selected)
			if (!validPlay(card))
				return false;

		return true;
	}

	// Returns true if @param card is a valid play
	private boolean validPlay(Card card) {
		Card target = playDeck.peek();
		// if previous player played an 8
		if (mustPlaySuit != 0) {
			return (card.getSuit() == mustPlaySuit || card.getRank() == 8);
		}
		return (card.getSuit() == target.getSuit() || card.getRank() == target.getRank() || card.getRank() == 8);
	}
}
