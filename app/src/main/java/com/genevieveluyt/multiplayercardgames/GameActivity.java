package com.genevieveluyt.multiplayercardgames;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

public class GameActivity extends Activity {

    public static final String EXTRA_CURR_PARTICIPANT_ID = "currParticipantId";
    public static final String EXTRA_PARTICIPANT_IDS = "participantIds";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_ACTION = "action";

    public static final int LEAVE = 100;
    public static final int END_TURN = 101;
    public static final int GAME_WON = 102;

    public GameBoard mTurnData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        mTurnData = new CrazyEightsGameBoard(intent.getStringExtra(EXTRA_CURR_PARTICIPANT_ID),
                intent.getStringArrayListExtra(EXTRA_PARTICIPANT_IDS),
                intent.getByteArrayExtra(EXTRA_DATA),
                this
        );
    }

    /****************************** In-Game Options **********************************************/

    // Cancel the game.
    public void onCancelClicked(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    // Leave the game during your turn. Note that there is a separate
    // Games.TurnBasedMultiplayer.leaveMatch() if you want to leave NOT on your turn.
    public void onLeaveClicked(View view) {
        setResult(Activity.RESULT_OK, (new Intent()).putExtra(EXTRA_ACTION, LEAVE));


        finish();
    }

    // End turn
    public void onFinishClicked(View view) {
        // TODO move to respective game board
        CrazyEightsGameBoard game = (CrazyEightsGameBoard) mTurnData;

        Intent intent = new Intent();

        // if game not over
        if (!(game.currHand.isEmpty() || game.drawDeck.isEmpty())) {
            intent.putExtra(EXTRA_ACTION, END_TURN);
            intent.putExtra(EXTRA_DATA, mTurnData.saveData());
        } else
            intent.putExtra(EXTRA_ACTION, GAME_WON);

        setResult(Activity.RESULT_OK, intent);

        finish();
    }

    /*********************************************************************************************/


}
