package com.genevieveluyt.multiplayercardgames;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

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
public class CrazyEightsGame extends Game {

	private static final int STARTING_HAND = 8;

	Activity activity;

    // Layouts
	LinearLayout gameLayout;
	LinearLayout handLayout;
	TextView numCardsView;

	// Game variables
	Hand[] hands;    // Player ID, Hand of cards
	int currParticipantIndex;		// index of current player in participantIds and playerNames
	ArrayList<String> participantIds;
	ArrayList<String> playerNames;
	int numPlayers;
	Hand currHand;
	Deck drawDeck;
	Deck playDeck;
	AlertDialog chooseSuitDialog;
	AlertDialog mustPlaySuitDialog;
	PopupMenu overflowMenu;
	GameCallbacks mCallbacks;

	// Gameplay variables
	boolean hasPlayed; 			// current player has placed a card on the play deck
	int chosenSuit;				// suit chosen after playing an 8
	int mustPlaySuit;			// suit chosen by previous player after playing an 8
	String hint;

	View.OnClickListener handClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!hasPlayed) currHand.select(new Card(v.getId()));
		}
	};

	public CrazyEightsGame(int currParticipantIndex, ArrayList<String> participantIds, ArrayList<String> playerNames, Activity activity) {
		super();
		this.currParticipantIndex = currParticipantIndex;
		this.participantIds = participantIds;
		this.playerNames = playerNames;
		this.gameLayout = (LinearLayout) activity.findViewById(R.id.gameplay_layout);
		this.handLayout = (LinearLayout) activity.findViewById(R.id.hand_layout);
		this.activity = activity;
		numPlayers = participantIds.size();
		hands = new Hand[numPlayers];
		hasPlayed = false;
		chosenSuit = 0;
		mustPlaySuit = 0;

		initGame();
	}

	public CrazyEightsGame(int currParticipantIndex, ArrayList<String> participantIds, ArrayList<String> playerNames, byte[] data, Activity activity) {
		super();
		this.currParticipantIndex = currParticipantIndex;
		this.participantIds = participantIds;
		this.playerNames = playerNames;
		this.gameLayout = (LinearLayout) activity.findViewById(R.id.gameplay_layout);
		this.handLayout = (LinearLayout) activity.findViewById(R.id.hand_layout);
		this.activity = activity;
		numPlayers = participantIds.size();
		hands = new Hand[numPlayers];
		hasPlayed = false;
		chosenSuit = 0;
		mustPlaySuit = 0;

		// initiate callback object which calls back to GameActivity when turn is ended, cancelled or won
		try {
			mCallbacks = (GameCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement GameCallbacks.");
		}

		try {
			loadData(data);
			activateGUI();
		}
		 catch (Exception e) {
			Log.d(MainActivity.TAG, "Loading data error: " + e);
			mCallbacks.onLoadDataError();
		}
	}

	@Override
	public void initGame() {
		if (MainActivity.DEBUG) System.out.println("CrazyEightsGame|initGame(): Initializing game");
		drawDeck = new Deck(Deck.STANDARD);
		playDeck = new Deck(Deck.EMPTY);

		for (int i = 0; i < numPlayers; i++) {
			hands[i] = new Hand(drawDeck, STARTING_HAND);
			if (MainActivity.DEBUG) System.out.println(playerNames.get(i) + " hand: " + hands[i]);
		}

		playDeck.addVirtual(drawDeck.drawVirtual());
	}

	/* Data format:
		draw deck data | play deck data | chosen suit | hand data
	 */
	@Override
	public byte[] saveData() {
		if (MainActivity.DEBUG) System.out.println("CrazyEightsGame|loadData(byte[]): Saving game data");
        StringBuilder dataStr = new StringBuilder();
		dataStr.append(drawDeck.getData()).append(separator)
        .append(playDeck.getData()).append(separator);
        for (int i = 0; i < numPlayers; i++) {
            dataStr.append(hands[i].getData())
            .append(separator);
        }
		dataStr.append(chosenSuit);

        return dataStr.toString().getBytes(Charset.forName("UTF-8"));
	}

	@Override
	public void loadData(byte[] data) {
		if (MainActivity.DEBUG) System.out.println("CrazyEightsGame|loadData(byte[]): Loading game data");

		String dataStr = new String(data, Charset.forName("UTF-8"));
		String[] dataArr = dataStr.split(String.valueOf(separator));

		drawDeck = new Deck(dataArr[0], false, (ImageView) gameLayout.findViewById(R.id.drawdeck_view));
		playDeck = new Deck(dataArr[1], true, (ImageView) gameLayout.findViewById(R.id.playdeck_view));

		for (int i = 2; i < 2 + numPlayers; i++) {
			int j = i-2;
			if (j == currParticipantIndex) {
				currHand = new Hand(dataArr[i], handLayout, handClickListener);
				hands[j] = currHand;
			} else
				hands[j] = new Hand(dataArr[i]);

			if (MainActivity.DEBUG)
				System.out.println(playerNames.get(j) + " hand: " + hands[j]);
		}
		mustPlaySuit = Integer.parseInt(dataArr[2 + numPlayers]);
	}

	private void activateGUI() {

		// Set game name at top
		((TextView) activity.findViewById(R.id.game_title)).setText(getGameName());

		// logic for when draw deck or play deck is clicked
		View.OnClickListener gameClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.drawdeck_view:
						currHand.draw(drawDeck);
						numCardsView.setText(Integer.toString(currHand.size()));
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
							numCardsView.setText(Integer.toString(currHand.size()));
							hasPlayed = true;
							if (currHand.isEmpty()) {
								Game.showYouWonDialog(activity, mCallbacks);
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

		// logic for when menu options are clicked (hint, cancel, more options)
		View.OnClickListener menuClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()){
					case R.id.hint_button:
						if (!hasPlayed && mustPlaySuit != 0)
							mustPlaySuitDialog.show();
						else
							showHintDialog(activity, hint, Game.CRAZY_EIGHTS);
						break;
					case R.id.end_turn_button:
						if (hasPlayed) {
							if (playDeck.peek().getRank() == 8 && chosenSuit == 0)
								chooseSuitDialog.show();
							else
								mCallbacks.onTurnEnded();
						}
						break;
					case R.id.overflow_button:
						overflowMenu.show();
				}
			}
		};

		// logic for options in overflow menu
		PopupMenu.OnMenuItemClickListener moreOptionsClickListener = new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.cancel_game:
						showCancelDialog(activity, mCallbacks);
						return true;
					default:
						return false;
				}
			}
		};

		// set click listeners for game elements
		gameLayout.findViewById(R.id.drawdeck_view).setOnClickListener(gameClickListener);
		gameLayout.findViewById(R.id.playdeck_view).setOnClickListener(gameClickListener);
		gameLayout.findViewById(R.id.hint_button).setOnClickListener(menuClickListener);
		gameLayout.findViewById(R.id.end_turn_button).setOnClickListener(menuClickListener);
		gameLayout.findViewById(R.id.overflow_button).setOnClickListener(menuClickListener);

		// make dialog to be used for choosing a suit after an 8 is played
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
		builder.setTitle(R.string.choose_suit)
				.setAdapter(new ArrayAdapterWithIcon(activity, R.array.suits_array, R.array.suits_icons_array),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								chosenSuit = which+1;
								if (MainActivity.DEBUG)
									System.out.println("CrazyEightsGame|activateGUI(): chose suit " + Card.suitToString(chosenSuit));
							}
				});
		chooseSuitDialog = builder.create();

		// initiate overflow popup menu
		overflowMenu = new PopupMenu(activity, activity.findViewById(R.id.overflow_button));
		MenuInflater menuInflater = overflowMenu.getMenuInflater();
		menuInflater.inflate(R.menu.crazy_eights_menu, overflowMenu.getMenu());
		overflowMenu.setOnMenuItemClickListener(moreOptionsClickListener);

		// populate opponents

		LinearLayout oppLayout = (LinearLayout) activity.findViewById(R.id.opponent_layout);
		LayoutInflater layoutInflater = activity.getLayoutInflater();

		for (int i = currParticipantIndex; i < currParticipantIndex + numPlayers; i++) {
			View oppView = layoutInflater.inflate(R.layout.opponent_layout, oppLayout, false);
			String playerName = playerNames.get(i%numPlayers);
			((TextView) oppView.findViewById(R.id.txt_playerName)).setText(playerName);
			((TextView) oppView.findViewById(R.id.txt_numCards)).setText(Integer.toString(hands[i%numPlayers].size()));
			((ImageView) oppView.findViewById(R.id.img_playerTemplate)).setImageResource(getPlayerDrawable(i%numPlayers));
			oppLayout.addView(oppView);
		}

		numCardsView = (TextView) oppLayout.getChildAt(0).findViewById(R.id.txt_numCards);

		// set hint
		if (mustPlaySuit == 0)
			if (canPlay())
				hint = activity.getString(R.string.gameid_1_can_play_hint);
			else
				hint = activity.getString(R.string.gameid_1_cant_play_hint);
		else {
			String suit = activity.getResources().getStringArray(R.array.suits_array)[mustPlaySuit-1];
			TypedArray imgArray = activity.getResources().obtainTypedArray(R.array.suits_icons_array);
			hint = getPrevPlayerName(Game.ROUND_ROBIN, playerNames, currParticipantIndex) + " "
					+ activity.getString(R.string.gameid_1_8_was_played) + " " + suit + ". "
					+ activity.getString(R.string.Play);

			// make dialog to display suit chosen by previous if they played an 8 and show it

			View dialogView = activity.getLayoutInflater().inflate(R.layout.crazy_eights_dialog, null);
			((TextView) dialogView.findViewById(R.id.suit))
					.setText(suit);
			((ImageView) dialogView.findViewById(R.id.suit_image))
					.setImageResource(imgArray.getResourceId(mustPlaySuit-1, 0));

			imgArray.recycle();

			builder = new android.app.AlertDialog.Builder(activity);
			builder.setView(dialogView)
					.setMessage(hint)
					.setNegativeButton(R.string.game_rules, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							showGameRulesDialog(activity, Game.CRAZY_EIGHTS);
						}
					})
					.setNeutralButton(R.string.ok, null);

			mustPlaySuitDialog = builder.create();
			mustPlaySuitDialog.show();

			// If user has never played this game before, show game rules
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
			boolean learnedCrazyEights = sp.getBoolean(Game.PREF_GAME_LEARNED[Game.CRAZY_EIGHTS], false);
			if (!learnedCrazyEights) {
				sp.edit().putBoolean(Game.PREF_GAME_LEARNED[Game.CRAZY_EIGHTS], true).apply();
				showGameRulesDialog(activity, Game.CRAZY_EIGHTS);
			}
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

	@Override
	public String getGameName() { return getGameName(activity, Game.CRAZY_EIGHTS); }

	@Override
	public String getNextParticipantId() {
		return Game.getNextParticipantId(Game.ROUND_ROBIN, participantIds, currParticipantIndex);
	}
}
