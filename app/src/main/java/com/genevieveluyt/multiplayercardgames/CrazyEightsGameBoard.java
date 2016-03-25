package com.genevieveluyt.multiplayercardgames;

import android.app.Activity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Genevieve on 03/09/2015.
 */
public class CrazyEightsGameBoard extends GameBoard {

	private static final int STARTING_HAND = 8;

    String currPlayerId;
    ArrayList<String> playerIds;
	Hand currHand;
	LinearLayout gameLayout;
	HorizontalScrollView oppLayout;   // TODO move to parent class?

	//HashMap<String, Hand> hands; in parent class
	Deck drawDeck;
	Deck playDeck;

	public CrazyEightsGameBoard(String currPlayerId, ArrayList<String> playerIds, byte[] data, Activity activity) {
		super();
		this.currPlayerId = currPlayerId;
		this.playerIds = playerIds;
		this.gameLayout = (LinearLayout) activity.findViewById(R.id.gameplay_layout);
		this.oppLayout = (HorizontalScrollView) activity.findViewById(R.id.opponent_scroll_layout); // TODO temp
		if (data == null)
			initBoard();
		else
			loadData(data);
	}

	@Override
	public void initBoard() {
		if (MainActivity.DEBUG) System.out.println("CrazyEightsGameBoard|initBoard(): Initializing game board");
		drawDeck = new Deck(Deck.STANDARD);
		playDeck = new Deck(Deck.EMPTY);
		for (String player : playerIds) {
			hands.put(player, new Hand(drawDeck, STARTING_HAND));
			if (MainActivity.DEBUG) System.out.println(player + " hand: " + hands.get(player));
		}
		playDeck.addVirtual(drawDeck.drawVirtual());
		activateGUI();

		// TODO Make this and opponent_layout.xml dynamic

	}

	/* Data format:
		game id | draw deck data | play deck data | playerIds and their hand data
	 */
	@Override
	public byte[] saveData() {
		if (MainActivity.DEBUG) System.out.println("Saving game data");
        StringBuilder dataStr = new StringBuilder();
		dataStr.append(getGameType()).append(separator)
        .append(drawDeck.getData()).append(separator)
        .append(playDeck.getData()).append(separator);
        for (String playerId : playerIds) {
	        dataStr.append(playerId).append(separator)
            .append(hands.get(playerId).getData())
            .append(separator);
        }

        return dataStr.toString().getBytes(Charset.forName("UTF-8"));
	}

	@Override
	public void loadData(byte[] data) {
		if (MainActivity.DEBUG) System.out.println("CrazyEightsGameBoard|loadData(byte[]): Loading game data");
        String dataStr = new String(data, Charset.forName("UTF-8"));
        String[] dataArr = dataStr.split(String.valueOf(separator));
        drawDeck = new Deck(dataArr[1], false, (ImageView) gameLayout.findViewById(R.id.drawdeck_view));
        playDeck = new Deck(dataArr[2], true, (ImageView) gameLayout.findViewById(R.id.playdeck_view));
        for (int i = 3; i < dataArr.length; i+=2) {
	        String playerId = dataArr[i];
	        if (playerId.equals(currPlayerId)) {
		        currHand = new Hand(dataArr[i+1], (LinearLayout) gameLayout.findViewById(R.id.hand_layout));
		        hands.put(playerId, currHand);
	        } else
		        hands.put(playerId, new Hand(dataArr[i+1]));  // hand data starts after the two decks
	        if (MainActivity.DEBUG) System.out.println(playerId + " hand: " + hands.get(playerId));
        }
		activateGUI();
	}

	@Override
	public int getGameType() {
		return GameBoard.CRAZY_EIGHTS;
	}

	public void activateGUI() {
		gameLayout.findViewById(R.id.drawdeck_view).setOnClickListener(deckClickListener);
		gameLayout.findViewById(R.id.playdeck_view).setOnClickListener(deckClickListener);
	}

	View.OnClickListener deckClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()){
				case R.id.drawdeck_view:
					currHand.draw(drawDeck);
					break;
				case R.id.playdeck_view:
					currHand.playSelected(playDeck);
			}
		}
	};
}
