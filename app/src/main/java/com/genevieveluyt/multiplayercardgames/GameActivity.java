package com.genevieveluyt.multiplayercardgames;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

public class GameActivity extends Activity
    implements Game.GameCallbacks {

    public static final String EXTRA_GAME_VARIANT = "gameVariant";
    public static final String EXTRA_CURR_PARTICIPANT_INDEX = "currParticipantIndex";
    public static final String EXTRA_PARTICIPANT_IDS = "participantIds";
    public static final String EXTRA_PLAYER_NAMES = "playerNames";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_NEXT_PARTICIPANT = "nextParticipant";

    // actions that can be retrieved from extra EXTRA_ACTION
    public static final int LEAVE = 100;
    public static final int END_TURN = 101;
    public static final int GAME_WON = 102;
    public static final int CANCEL = 103;
    public static final int LOAD_DATA_ERROR = 104;
    public static final int NEWER_VERSION_ERROR = 105;
    public static final int OLDER_VERSION_ERROR = 106;
    public static final int UNKNOWN_GAME_ERROR = 107;

    public Game mTurnData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        int gameVariant = intent.getIntExtra(EXTRA_GAME_VARIANT, 0);

        switch (gameVariant) {
            case Game.CRAZY_EIGHTS:
                mTurnData = new CrazyEightsGame(intent.getIntExtra(EXTRA_CURR_PARTICIPANT_INDEX, 0),
                    intent.getStringArrayListExtra(EXTRA_PARTICIPANT_IDS),
                    intent.getStringArrayListExtra(EXTRA_PLAYER_NAMES),
                    intent.getByteArrayExtra(EXTRA_DATA),
                    this
                );
                break;
            default:    // user has an old version of this app in which the specified game does not yet exist
                setResult(RESULT_OK, (new Intent()).putExtra(EXTRA_ACTION, UNKNOWN_GAME_ERROR));
                finish();
        }
    }

    /******************************** Implement Game.GameCallbacks ********************************/

    // End turn
    @Override
    public void onTurnEnded() {
        CrazyEightsGame game = (CrazyEightsGame) mTurnData;

        Intent intent = new Intent();
        intent.putExtra(EXTRA_ACTION, END_TURN)
                .putExtra(EXTRA_NEXT_PARTICIPANT, game.getNextParticipantId())
                .putExtra(EXTRA_DATA, game.saveData());

        setResult(Activity.RESULT_OK, intent);

        finish();
    }

    // Cancel the game.
    @Override
    public void onGameCancelled() {
        setResult(Activity.RESULT_OK, (new Intent()).putExtra(EXTRA_ACTION, CANCEL));
        finish();
    }

    @Override
    public void onGameWon() {
        CrazyEightsGame game = (CrazyEightsGame) mTurnData;

        Intent intent = new Intent();
        intent.putExtra(EXTRA_ACTION, GAME_WON)
                .putExtra(EXTRA_DATA, game.saveData());

        setResult(Activity.RESULT_OK, (intent));

        finish();
    }

    // An exception occurred when trying to load the game data. Likely, players in the game have
    // incompatible versions of the app
    @Override
    public void onLoadError(int errorCode) {
        setResult(RESULT_OK, (new Intent()).putExtra(EXTRA_ACTION, errorCode));
        finish();
    }
}
