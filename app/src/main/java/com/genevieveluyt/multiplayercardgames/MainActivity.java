package com.genevieveluyt.multiplayercardgames;

/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;

public class MainActivity extends Activity
		implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	public static final String TAG = "CardGameActivity";

	// Prints out messages to System.out for debugging
	public static final boolean DEBUG = true;

	// Client used to interact with Google APIs
	private GoogleApiClient mGoogleApiClient;

	// Are we currently resolving a connection failure?
	private boolean mResolvingConnectionFailure = false;

	// Has the user clicked the sign-in button?
	private boolean mSignInClicked = false;

	// Automatically start the sign-in flow when the Activity starts
	private boolean mAutoStartSignInFlow = true;

	// Current turn-based match
	private TurnBasedMatch mTurnBasedMatch;

	private AlertDialog mAlertDialog;

	// For our intents
	private static final int RC_SIGN_IN = 9001;
	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_LOOK_AT_MATCHES = 10001;
	private static final int RC_CHOOSE_GAME = 10002;

	// How long to show toasts.
	final static int TOAST_DELAY = Toast.LENGTH_SHORT;

	// Should I be showing the turn API?
	public boolean isDoingTurn = false;

	// This is the current match we're in; null if not loaded
	public TurnBasedMatch mMatch;

	// This is the current match data after being unpersisted.
	// Do not retain references to match data once you have
	// taken an action on the match, such as takeTurn()
	public GameBoard mTurnData;

	public int mGameType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the Google API Client with access to Plus and Games
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API)
				.addApi(Games.API)
				.addScope(Games.SCOPE_GAMES)
				.build();

		//mTurnData = new CrazyEightsGameBoard("playerId", new ArrayList<String>(){ {add("playerId");} }, null, this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mGoogleApiClient.isConnected()) {
			Log.d(TAG, "onStart(): Connecting to Google APIs");
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy(): Disconnecting from Google APIs");
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	/********************* implement GoogleApiClient.ConnectionCallbacks **************************/

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "onConnected(): Connection successful");

		// Retrieve the TurnBasedMatch from the connectionHint
		if (connectionHint != null) {
			mTurnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

			if (mTurnBasedMatch != null) {
				if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
					Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
				}

				updateMatch(mTurnBasedMatch);
				return;
			}
		}

		setViewVisibility();
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(TAG, "onConnectionSuspended():  Trying to reconnect.");
		mGoogleApiClient.connect();
		setViewVisibility();
	}

	/***************** implement GoogleApiClient.OnConnectionFailedListener ***********************/

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed(): attempting to resolve");
		if (mResolvingConnectionFailure) {
			// Already resolving
			Log.d(TAG, "onConnectionFailed(): ignoring connection failure, already resolving.");
			return;
		}

		// Launch the sign-in flow if the button was clicked or if auto sign-in is enabled
		if (mSignInClicked || mAutoStartSignInFlow) {
			mAutoStartSignInFlow = false;
			mSignInClicked = false;

			mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this,
					mGoogleApiClient, connectionResult, RC_SIGN_IN,
					getString(R.string.signin_other_error));
		}

		setViewVisibility();
	}

	/******************************** Home Menu Options *******************************************/

	// Open the create-game UI. You will get back an onActivityResult
	// and figure out what to do.
	public void onNewGameClicked(View view) {
		/*Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient,
				1, 7, false);
		startActivityForResult(intent, RC_SELECT_PLAYERS);*/
		Intent intent = new Intent(this, ChooseCardGame.class);
		startActivityForResult(intent, RC_CHOOSE_GAME);
	}

	// Displays your inbox. You will get back onActivityResult where
	// you will need to figure out what you clicked on.
	public void onCheckGamesClicked(View view) {
		Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
		startActivityForResult(intent, RC_LOOK_AT_MATCHES);
	}

	public void onSignInOutClicked(View view) {
		Button signInOutBtn = (Button) view;

		// If sign-in clicked
		if (signInOutBtn.getText().equals(getString(R.string.sign_in))) {
			mSignInClicked = true;
			mTurnBasedMatch = null;
			mGoogleApiClient.connect();
		}
		// if sign-out clicked
		else {
			mSignInClicked = false;
			Games.signOut(mGoogleApiClient);
			if (mGoogleApiClient.isConnected()) {
				mGoogleApiClient.disconnect();
			}
			setViewVisibility();
		}
	}

	/********************************** In-game Menu **********************************************/

	// Cancel the game. Should possibly wait until the game is canceled before
	// giving up on the view.
	public void onCancelClicked(View view) {
		showSpinner();
		Games.TurnBasedMultiplayer.cancelMatch(mGoogleApiClient, mMatch.getMatchId())
				.setResultCallback(new ResultCallback<TurnBasedMultiplayer.CancelMatchResult>() {
					@Override
					public void onResult(TurnBasedMultiplayer.CancelMatchResult result) {
						processResult(result);
					}
				});
		isDoingTurn = false;
		setViewVisibility();
	}

	// Leave the game during your turn. Note that there is a separate
	// Games.TurnBasedMultiplayer.leaveMatch() if you want to leave NOT on your turn.
	public void onLeaveClicked(View view) {
		showSpinner();
		String nextParticipantId = getNextParticipantId();

		Games.TurnBasedMultiplayer.leaveMatchDuringTurn(mGoogleApiClient, mMatch.getMatchId(),
				nextParticipantId).setResultCallback(
				new ResultCallback<TurnBasedMultiplayer.LeaveMatchResult>() {
					@Override
					public void onResult(TurnBasedMultiplayer.LeaveMatchResult result) {
						processResult(result);
					}
				});
		setViewVisibility();
	}

	// End turn
	public void onFinishClicked(View view) {
		// TODO move to respective game board
		CrazyEightsGameBoard game = (CrazyEightsGameBoard) mTurnData;

		// if player's hand or draw deck is empty, finish game
		if (game.currHand.isEmpty() || game.drawDeck.isEmpty()){
			showSpinner();
			Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId())
					.setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
						@Override
						public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
							processResult(result);
						}
					});

			isDoingTurn = false;
			setViewVisibility();
			return;
		}

		showSpinner();

		String nextParticipantId = getNextParticipantId();

		showSpinner();

		Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
				mTurnData.saveData(), nextParticipantId).setResultCallback(
				new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
					@Override
					public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
						processResult(result);
					}
				});

		mTurnData = null;
	}

	/*********************************************************************************************/

	// Update the visibility based on what state we're in.
	public void setViewVisibility() {
		boolean isSignedIn = (mGoogleApiClient != null) && (mGoogleApiClient.isConnected());
		Button signInOutBtn = (Button) findViewById(R.id.sign_in_out_button);
		Button newGameBtn = (Button) findViewById(R.id.newGameButton);
		Button checkGamesBtn = (Button) findViewById(R.id.checkGamesButton);

		if (!isSignedIn) {
			// Show home page with New Game and Check Games disabled
			findViewById(R.id.menu_layout).setVisibility(View.VISIBLE);
			signInOutBtn.setText(R.string.sign_in);
			newGameBtn.setClickable(false);
			newGameBtn.setBackgroundResource(R.color.grey);
			checkGamesBtn.setClickable(false);
			checkGamesBtn.setBackgroundResource(R.color.grey);
			findViewById(R.id.gameplay_layout).setVisibility(View.GONE);

			if (mAlertDialog != null) {
				mAlertDialog.dismiss();
			}
			return;
		}

		if (isDoingTurn) {
			findViewById(R.id.menu_layout).setVisibility(View.GONE);
			findViewById(R.id.gameplay_layout).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.menu_layout).setVisibility(View.VISIBLE);
			signInOutBtn.setText(R.string.sign_out);
			newGameBtn.setClickable(true);
			newGameBtn.setBackgroundResource(R.color.new_game);
			checkGamesBtn.setClickable(true);
			checkGamesBtn.setBackgroundResource(R.color.check_games);
			findViewById(R.id.gameplay_layout).setVisibility(View.GONE);
		}
	}

	// Helpful dialogs

	public void showSpinner() {
		findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
	}

	public void dismissSpinner() {
		findViewById(R.id.progressLayout).setVisibility(View.GONE);
	}

	// Generic warning/info dialog
	public void showWarning(String title, String message) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle(title).setMessage(message);

		// set dialog message
		alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
					}
				});

		// create alert dialog
		mAlertDialog = alertDialogBuilder.create();

		// show it
		mAlertDialog.show();
	}

	// Rematch dialog
	public void askForRematch() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setMessage("Do you want a rematch?");

		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Sure, rematch!",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								rematch();
							}
						})
				.setNegativeButton("No.",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						});

		alertDialogBuilder.show();
	}

	// This function is what gets called when you return from either the Play
	// Games built-in inbox, or else the create game built-in interface.
	@Override
	public void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
		if (request == RC_CHOOSE_GAME) {
			mGoogleApiClient.connect();
			if (response == Activity.RESULT_OK) {
				mGameType = data.getIntExtra(ChooseCardGame.GAME_TYPE_EXTRA, 0);
				Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient,
						1, 7, false);
				startActivityForResult(intent, RC_SELECT_PLAYERS);
			}
		} else if (request == RC_SIGN_IN) {
			mSignInClicked = false;
			mResolvingConnectionFailure = false;
			if (response == Activity.RESULT_OK) {
				mGoogleApiClient.connect();
			} else {
				BaseGameUtils.showActivityResultError(this, request, response, R.string.signin_other_error);
			}
		} else if (request == RC_LOOK_AT_MATCHES) {
			// Returning from the 'Select Match' dialog

			if (response != Activity.RESULT_OK) {
				// user canceled
				return;
			}

			TurnBasedMatch match = data
					.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

			if (match != null) {
				updateMatch(match);
			}

			Log.d(TAG, "Match = " + match);
		} else if (request == RC_SELECT_PLAYERS) {
			// Returned from 'Select players to Invite' dialog

			if (response != Activity.RESULT_OK) {
				// user canceled
				return;
			}

			// get the invitee list
			final ArrayList<String> invitees = data
					.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);


			TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
					.addInvitedPlayers(invitees).build();

			// Start the match
			Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(
					new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
						@Override
						public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
							processResult(result);
						}
					});
			showSpinner();
		}
	}

	// startMatch() happens in response to the createTurnBasedMatch()
	// above. This is only called on success, so we should have a
	// valid match object. We're taking this opportunity to setup the
	// game, saving our initial state. Calling takeTurn() will
	// callback to OnTurnBasedMatchUpdated(), which will show the game
	// UI.
	public void startMatch(TurnBasedMatch match) {

		mMatch = match;

		String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
		String myParticipantId = mMatch.getParticipantId(playerId);

		mTurnData = new CrazyEightsGameBoard(myParticipantId, match.getParticipantIds(), null, this);

		showSpinner();

		Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(),
				mTurnData.saveData(), myParticipantId).setResultCallback(
				new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
					@Override
					public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
						processResult(result);
					}
				});
	}

	// If you choose to rematch, then call it and wait for a response.
	public void rematch() {
		showSpinner();
		Games.TurnBasedMultiplayer.rematch(mGoogleApiClient, mMatch.getMatchId()).setResultCallback(
				new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
					@Override
					public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
						processResult(result);
					}
				});
		mMatch = null;
		isDoingTurn = false;
	}

	/**
	 * Get the next participant. In this function, we assume that we are
	 * round-robin, with all known players going before all automatch players.
	 * This is not a requirement; players can go in any order. However, you can
	 * take turns in any order.
	 *
	 * @return participantId of next player, or null if automatching
	 */
	public String getNextParticipantId() {

		String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
		String myParticipantId = mMatch.getParticipantId(playerId);

		ArrayList<String> participantIds = mMatch.getParticipantIds();

		return participantIds.get((participantIds.indexOf(myParticipantId) + 1) % participantIds.size());
	}

	// This is the main function that gets called when players choose a match
	// from the inbox, or else create a match and want to start it.
	public void updateMatch(TurnBasedMatch match) {
		mMatch = match;

		int status = match.getStatus();
		int turnStatus = match.getTurnStatus();

		switch (status) {
			case TurnBasedMatch.MATCH_STATUS_CANCELED:
				showWarning("Canceled!", "This game was canceled!");
				return;
			case TurnBasedMatch.MATCH_STATUS_EXPIRED:
				showWarning("Expired!", "This game is expired.  So sad!");
				return;
			case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
				showWarning("Waiting for auto-match...",
						"We're still waiting for an automatch partner.");
				return;
			case TurnBasedMatch.MATCH_STATUS_COMPLETE:
				if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
					showWarning(
							"Complete!",
							"This game is over; someone finished it, and so did you!  There is nothing to be done.");
					break;
				}

				// Note that in this state, you must still call "Finish" yourself,
				// so we allow this to continue.
				showWarning("Complete!",
						"This game is over; someone finished it!  You can only finish it now.");
		}

		// OK, it's active. Check on turn status.
		switch (turnStatus) {
			case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
				String participantId = mMatch.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
				mTurnData = new CrazyEightsGameBoard(participantId, mMatch.getParticipantIds(), mMatch.getData(), this);
				isDoingTurn = true;
				setViewVisibility();
				return;
			case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
				// Should return results.
				showWarning("Alas...", "It's not your turn.");
				break;
			case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
				showWarning("Good inititative!",
						"Still waiting for invitations.\n\nBe patient!");
		}

		mTurnData = null;

		setViewVisibility();
	}

	private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
		dismissSpinner();

		if (!checkStatusCode(null, result.getStatus().getStatusCode())) {
			return;
		}

		isDoingTurn = false;

		//showWarning("Match",
		//		"This match is canceled.  All other players will have their game ended.");
	}

	private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
		TurnBasedMatch match = result.getMatch();
		dismissSpinner();

		if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
			return;
		}

		if (match.getData() != null) {
			// This is a game that has already started, so I'll just start
			updateMatch(match);
			return;
		}

		startMatch(match);
	}


	private void processResult(TurnBasedMultiplayer.LeaveMatchResult result) {
		TurnBasedMatch match = result.getMatch();
		dismissSpinner();
		if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
			return;
		}
		//isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);
		isDoingTurn = false;
		showWarning("Left", "You've left this match.");

		setViewVisibility();
	}


	public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
		TurnBasedMatch match = result.getMatch();
		dismissSpinner();
		if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
			return;
		}
		if (match.canRematch()) {
			askForRematch();
		}

		isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

		if (isDoingTurn) {
			updateMatch(match);
			return;
		}

		setViewVisibility();
	}

	public void showErrorMessage(TurnBasedMatch match, int statusCode,
	                             int stringId) {

		showWarning("Warning", getResources().getString(stringId));
	}

	// Returns false if something went wrong, probably. This should handle
	// more cases, and probably report more accurate results.
	private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
		switch (statusCode) {
			case GamesStatusCodes.STATUS_OK:
				return true;
			case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
				// This is OK; the action is stored by Google Play Services and will
				// be dealt with later.
				Toast.makeText(
						this,
						"Stored action for later.  (Please remove this toast before release.)",
						TOAST_DELAY).show();
				// NOTE: This toast is for informative reasons only; please remove
				// it from your final application.
				return true;
			case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
				showErrorMessage(match, statusCode,
						R.string.status_multiplayer_error_not_trusted_tester);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
				showErrorMessage(match, statusCode,
						R.string.match_error_already_rematched);
				break;
			case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
				showErrorMessage(match, statusCode,
						R.string.network_error_operation_failed);
				break;
			case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
				showErrorMessage(match, statusCode,
						R.string.client_reconnect_required);
				break;
			case GamesStatusCodes.STATUS_INTERNAL_ERROR:
				showErrorMessage(match, statusCode, R.string.internal_error);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
				showErrorMessage(match, statusCode,
						R.string.match_error_inactive_match);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
				showErrorMessage(match, statusCode,
						R.string.match_error_locally_modified);
				break;
			default:
				showErrorMessage(match, statusCode, R.string.unexpected_status);
				Log.d(TAG, "Did not have warning or string to deal with: "
						+ statusCode);
		}

		return false;
	}
}