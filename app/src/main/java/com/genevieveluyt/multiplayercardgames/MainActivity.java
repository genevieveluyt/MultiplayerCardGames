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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.util.ArrayList;

public class MainActivity extends Activity
		implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	public static final String TAG = "CardGameActivity";

	// Prints out messages to System.out for debugging
	public static final boolean DEBUG = true;

	// Client used to interact with Google APIs
	static GoogleApiClient mGoogleApiClient;

	// Are we currently resolving a connection failure?
	private boolean mResolvingConnectionFailure = false;

	// Has the user clicked the sign-in button?
	private boolean mSignInClicked = false;

	// Automatically start the sign-in flow when the Activity starts
	private boolean mAutoStartSignInFlow = true;

	// For our intents
	private static final int RC_SIGN_IN = 9001;
	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_LOOK_AT_MATCHES = 10001;
	final static int RC_CHOOSE_GAME = 10002;
	final static int RC_PLAY_GAME = 10003;

	// How long to show toasts.
	final static int TOAST_DELAY = Toast.LENGTH_SHORT;

	// This is the current match we're in; null if not loaded
	public TurnBasedMatch mMatch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the Google API Client with access to Plus and Games
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Games.API)
				.addScope(Games.SCOPE_GAMES)
				.build();
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateMenuUI();
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
			TurnBasedMatch mTurnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

			if (mTurnBasedMatch != null) {
				if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
					Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
				}

				updateMatch(mTurnBasedMatch);
				return;
			}
		}

		updateMenuUI();
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(TAG, "onConnectionSuspended():  Trying to reconnect.");
		mGoogleApiClient.connect();
		updateMenuUI();
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

		updateMenuUI();
	}

	/******************************** Home Menu Options *******************************************/

	// Open Choose Game UI
	public void onNewGameClicked(View view) {
		Intent intent = new Intent(this, ChooseGameActivity.class);
		startActivityForResult(intent, RC_CHOOSE_GAME);
	}

	// Open Current Matches UI
	public void onCheckGamesClicked(View view) {
		Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
		startActivityForResult(intent, RC_LOOK_AT_MATCHES);
	}

	// Sign in or out
	public void onSignInOutClicked(View view) {
		Button signInOutBtn = (Button) view;

		// If sign-in clicked
		if (signInOutBtn.getText().equals(getString(R.string.sign_in))) {
			mSignInClicked = true;
			mGoogleApiClient.connect();
		}
		// if sign-out clicked
		else {
			mSignInClicked = false;
			Games.signOut(mGoogleApiClient);
			if (mGoogleApiClient.isConnected()) {
				mGoogleApiClient.disconnect();
			}
			updateMenuUI();
		}
	}

	/*********************************************************************************************/

	// Update the visibility based on what state we're in.
	public void updateMenuUI() {
		boolean isSignedIn = (mGoogleApiClient != null) && (mGoogleApiClient.isConnected());
		Button signInOutBtn = (Button) findViewById(R.id.sign_in_out_button);
		Button newGameBtn = (Button) findViewById(R.id.newGameButton);
		Button checkGamesBtn = (Button) findViewById(R.id.checkGamesButton);

		if (!isSignedIn) {
			signInOutBtn.setText(R.string.sign_in);
			newGameBtn.setClickable(false);
			newGameBtn.setBackgroundResource(R.color.grey);
			checkGamesBtn.setClickable(false);
			checkGamesBtn.setBackgroundResource(R.color.grey);

			return;
		}

		signInOutBtn.setText(R.string.sign_out);
		newGameBtn.setClickable(true);
		newGameBtn.setBackgroundResource(R.color.new_game);
		checkGamesBtn.setClickable(true);
		checkGamesBtn.setBackgroundResource(R.color.check_games);
	}

	@Override
	public void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);

		switch (request) {
			case RC_SIGN_IN:
				mSignInClicked = false;
				mResolvingConnectionFailure = false;
				if (response == Activity.RESULT_OK)
					mGoogleApiClient.connect();
				else
					BaseGameUtils.showActivityResultError(this, request, response, R.string.signin_other_error);
				break;
			case RC_CHOOSE_GAME:
				if (response != Activity.RESULT_OK) {
					// user canceled
					return;
				}

				// get the invitee list
				final ArrayList<String> invitees = data
						.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

				TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
						.addInvitedPlayers(invitees)
						.setVariant(data.getIntExtra(ChooseGameActivity.EXTRA_GAME_VARIANT, 0))
						.build();

				// Start the match
				Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(
						new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
							@Override
							public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
								processResult(result);
							}
						});
				showSpinner();
				break;
			case RC_PLAY_GAME:
				if (response != Activity.RESULT_OK) {
					return;
				}

				processGameResponse(data);
				break;
			case RC_LOOK_AT_MATCHES:
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
		}
	}

	public void processGameResponse(Intent data) {
		int action = data.getIntExtra(GameActivity.EXTRA_ACTION, 0);
		String nextParticipantId;

		switch (action) {
			case GameActivity.END_TURN:
				nextParticipantId = data.getStringExtra(GameActivity.EXTRA_NEXT_PARTICIPANT);
				Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
						data.getByteArrayExtra(GameActivity.EXTRA_DATA), nextParticipantId).setResultCallback(
						new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
							@Override
							public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
								processResult(result);
							}
						});
				break;
			case GameActivity.GAME_WON:
				Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId())
						.setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
							@Override
							public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
								processResult(result);
							}
						});
				break;
			case GameActivity.CANCEL:
				Games.TurnBasedMultiplayer.cancelMatch(mGoogleApiClient, mMatch.getMatchId())
						.setResultCallback(new ResultCallback<TurnBasedMultiplayer.CancelMatchResult>() {
							@Override
							public void onResult(TurnBasedMultiplayer.CancelMatchResult result) {
								processResult(result);
							}
						});
				break;
			case GameActivity.UNKNOWN_GAME_ERROR:
				showUnknownGameErrorDialog();
				break;
			case GameActivity.LOAD_DATA_ERROR:
				showLoadErrorDialog();
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

		ArrayList<String> playerNames = new ArrayList<>();
		for (String id : mMatch.getParticipantIds())
			playerNames.add(mMatch.getParticipant(id).getDisplayName());

		Game mTurnData = null;

		switch (mMatch.getVariant()) {
			case Game.CRAZY_EIGHTS:
				mTurnData = new CrazyEightsGame(playerNames);
				break;
			default:
				BaseGameUtils.showAlert(this, R.string.game_not_found, R.string.found_bug_msg);
		}

		showSpinner();

		String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
		String myParticipantId = mMatch.getParticipantId(playerId);

		Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
				mTurnData.saveData(), myParticipantId).setResultCallback(
				new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
					@Override
					public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
						processResult(result);
					}
				});
	}

	// This is the main function that gets called when players choose a match
	// from the inbox, or else create a match and want to start it.
	public void updateMatch(TurnBasedMatch match) {
		mMatch = match;

		int status = match.getStatus();
		int turnStatus = match.getTurnStatus();

		switch (status) {
			case TurnBasedMatch.MATCH_STATUS_CANCELED:
				BaseGameUtils.showAlert(this, "Canceled!", "This game was canceled!");
				return;
			case TurnBasedMatch.MATCH_STATUS_EXPIRED:
				BaseGameUtils.showAlert(this, "Expired!", "This game is expired.");
				return;
			case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
				BaseGameUtils.showAlert(this, "Waiting for auto-match...",
						"We're still waiting for an automatch partner.");
				return;
			case TurnBasedMatch.MATCH_STATUS_COMPLETE:
				BaseGameUtils.showAlert(this, "Complete!",
						"This game is over");
				return;
		}

		// OK, it's active. Check on turn status.
		switch (turnStatus) {
			case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
				playTurn();
				return;
			case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
				// Should return results.
				BaseGameUtils.showAlert(this, "Alas...", "It's not your turn.");
				break;
			case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
				BaseGameUtils.showAlert(this, "Good inititative!",
						"Still waiting for invitations.\n\nBe patient!");
		}
	}

	public void playTurn() {
		String participantId = mMatch.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
		ArrayList<String> participantIds = mMatch.getParticipantIds();

		ArrayList<String> playerNames = new ArrayList<>();
		for (String id : mMatch.getParticipantIds())
			playerNames.add(mMatch.getParticipant(id).getDisplayName());

		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtra(GameActivity.EXTRA_CURR_PARTICIPANT_INDEX, participantIds.indexOf(participantId))
				.putExtra(GameActivity.EXTRA_PARTICIPANT_IDS, mMatch.getParticipantIds())
				.putExtra(GameActivity.EXTRA_PLAYER_NAMES, playerNames)
				.putExtra(GameActivity.EXTRA_DATA, mMatch.getData())
				.putExtra(GameActivity.EXTRA_GAME_VARIANT, mMatch.getVariant());
		startActivityForResult(intent, RC_PLAY_GAME);
	}

	/*********************** implement game result callbacks **************************************/

	private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
		dismissSpinner();
		checkStatusCode(result.getStatus().getStatusCode());
	}

	private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
		TurnBasedMatch match = result.getMatch();
		dismissSpinner();

		if (!checkStatusCode(result.getStatus().getStatusCode())) {
			return;
		}

		if (match.getData() != null) {
			// This is a game that has already started, so I'll just start
			updateMatch(match);
			return;
		}

		startMatch(match);
	}


	private void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
		TurnBasedMatch match = result.getMatch();
		dismissSpinner();
		if (!checkStatusCode(result.getStatus().getStatusCode())) {
			return;
		}
		if (match.canRematch()) {
			showRematchDialog();
		}

		if (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN)
			updateMatch(match);
	}

	/*********************************************************************************************/


	// Returns false if something went wrong, probably. This should handle
	// more cases, and probably report more accurate results.
	private boolean checkStatusCode(int statusCode) {
		switch (statusCode) {
			case GamesStatusCodes.STATUS_OK:
				return true;
			case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
				return true;
			case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
				showWarning(R.string.status_multiplayer_error_not_trusted_tester);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
				showWarning(R.string.match_error_already_rematched);
				break;
			case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
				showWarning(R.string.network_error_operation_failed);
				break;
			case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
				showWarning(R.string.client_reconnect_required);
				break;
			case GamesStatusCodes.STATUS_INTERNAL_ERROR:
				showWarning(R.string.internal_error);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
				showWarning(R.string.match_error_inactive_match);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
				showWarning(R.string.match_error_locally_modified);
				break;
			default:
				showWarning(R.string.unexpected_status);
				Log.d(TAG, "Did not have warning or string to deal with: "
						+ statusCode);
		}

		return false;
	}

	// intent to open app details in Play Store
	public static Intent getPlayStoreIntent() {
		return new Intent(Intent.ACTION_VIEW , Uri.parse("market://details?id=com.genevieveluyt.multiplayercardgames"));
	}

	/********************************** Dialogs ***************************************************/

	public void showSpinner() {
		findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
	}

	public void dismissSpinner() {
		findViewById(R.id.progressLayout).setVisibility(View.GONE);
	}

	public void showRematchDialog() {
		(new AlertDialog.Builder(this))
				.setMessage("Do you want a rematch?")
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								showSpinner();
								Games.TurnBasedMultiplayer.rematch(mGoogleApiClient, mMatch.getMatchId()).setResultCallback(
										new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
											@Override
											public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
												processResult(result);
											}
										});
								mMatch = null;
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						})
				.show();
	}

	public void showWarning(int stringId) {
		BaseGameUtils.showAlert(this, R.string.warning, stringId);
	}

	public void showLoadErrorDialog() {
		(new AlertDialog.Builder(this))
				.setMessage(R.string.load_data_error)
				.setNegativeButton(R.string.no, null)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(getPlayStoreIntent());
					}
				})
				.show();
	}

	public void showUnknownGameErrorDialog() {
		(new AlertDialog.Builder(this))
				.setMessage(R.string.unknown_game_error)
				.setNegativeButton(R.string.no, null)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(MainActivity.getPlayStoreIntent());
					}
				})
				.show();
	}
}