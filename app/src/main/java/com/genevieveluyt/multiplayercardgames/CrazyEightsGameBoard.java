package com.genevieveluyt.multiplayercardgames;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Genevieve on 03/09/2015.
 */
public class CrazyEightsGameBoard extends GameBoard {

	private final int STARTING_HAND = 7;

    String currPlayerId;
    ArrayList<String> playerIds;
	Hand currHand;
	LinearLayout gameLayout;

	//HashMap<String, Hand> hands; in parent class
	Deck drawDeck;
	Deck playDeck;

	public CrazyEightsGameBoard(String currPlayerId, ArrayList<String> playerIds, byte[] data, Activity activity) {
		super();
		this.currPlayerId = currPlayerId;
		this.playerIds = playerIds;
		this.gameLayout = (LinearLayout) activity.findViewById(R.id.gameplay_layout);
		if (data == null)
			initBoard();
		else
			loadData(data);
	}

	@Override
	public void initBoard() {
		if (MainActivity.DEBUG) System.out.println("Initializing game board");
		drawDeck = new Deck(Deck.STANDARD, (ImageView) gameLayout.findViewById(R.id.drawdeck_view));
		drawDeck.setFrontFacing(false);
		playDeck = new Deck(Deck.EMPTY, (ImageView) gameLayout.findViewById(R.id.playdeck_view));
		for (int player = 0; player < playerIds.size(); player++) {
			hands.put(playerIds.get(player), new Hand(drawDeck, STARTING_HAND, (LinearLayout) gameLayout.findViewById(R.id.hand_layout)));
		}
		currHand = hands.get(currPlayerId);
		playDeck.play(drawDeck.draw());
		activateGUI();
	}

	@Override
	public byte[] saveData() {
        StringBuilder dataStr = new StringBuilder();
        dataStr.append(drawDeck.getData()).append(separator);
        dataStr.append(playDeck.getData()).append(separator);
        for (String playerId : playerIds) {
            dataStr.append(hands.get(playerId).getData());
            dataStr.append(separator);
        }

        return dataStr.toString().getBytes(Charset.forName("UTF-8"));
	}

	@Override
	public void loadData(byte[] data) {
        String dataStr = new String(data, Charset.forName("UTF-8"));
        String[] dataArr = dataStr.split(String.valueOf(separator)); // REVIEW does splitting on the last char make an empty string?
        drawDeck = new Deck(dataArr[0], (ImageView) gameLayout.findViewById(R.id.drawdeck_view));
        playDeck = new Deck(dataArr[1], (ImageView) gameLayout.findViewById(R.id.playdeck_view));
        for (int player = 0; player < playerIds.size(); player++) {
            hands.put(playerIds.get(player), new Hand(dataArr[player+2], (LinearLayout) gameLayout.findViewById(R.id.hand_layout)));  // hand data starts after the two decks
        }
		currHand = hands.get(currPlayerId);
		activateGUI();
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
