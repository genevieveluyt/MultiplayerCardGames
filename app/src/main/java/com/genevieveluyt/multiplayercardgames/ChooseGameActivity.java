package com.genevieveluyt.multiplayercardgames;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.games.Games;

public class ChooseGameActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    // Use for getting game type from result (data.getExtra(
    public static final String EXTRA_GAME_VARIANT = "gameVariant";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    static NavigationDrawerFragment mNavigationDrawerFragment;

    int selectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_card_game);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        selectedPosition = position;

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, GameInfoFragment.newInstance(position))
                .commit();
    }

    public void onPlayClicked(View view) {
        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(MainActivity.mGoogleApiClient,
                1, 7, false);
        startActivityForResult(intent, MainActivity.RC_SELECT_PLAYERS);
    }

    public void onOpenDrawerClicked(View view) {
        mNavigationDrawerFragment.openDrawer();
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if (request == MainActivity.RC_SELECT_PLAYERS) {
            // Returned from 'Select players to Invite' dialog

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            setResult(Activity.RESULT_OK, data.putExtra(EXTRA_GAME_VARIANT, selectedPosition+1));
            finish();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class GameInfoFragment extends Fragment {

        private static int position;

        public GameInfoFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static GameInfoFragment newInstance(int position) {
            GameInfoFragment fragment = new GameInfoFragment();
            fragment.position = position;
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_choose_card_game, container, false);
            String [] gameNames = getResources().getStringArray(R.array.game_names_array);
            String [] gameInfo = getResources().getStringArray(R.array.game_info_array);
            ((TextView) rootView.findViewById(R.id.game_title))
                    .setText(gameNames[position]);
            ((TextView) rootView.findViewById(R.id.game_info))
                    .setText(Html.fromHtml(gameInfo[position]));

            // hide Play button if on Other Games page
            Button playButton = (Button) rootView.findViewById(R.id.play_button);
            TextView infoView = (TextView) rootView.findViewById(R.id.game_info);
            TextView moreGamesView = (TextView) rootView.findViewById(R.id.more_games);
            if (position == gameNames.length-1) {
                playButton.setVisibility(View.GONE);
                infoView.setVisibility(View.GONE);
                moreGamesView.setVisibility(View.VISIBLE);
            } else {
                playButton.setVisibility(View.VISIBLE);
                infoView.setVisibility(View.VISIBLE);
                moreGamesView.setVisibility(View.GONE);
            }

            // Enable swiping to switch between game info screens
            final GestureDetector gesture = new GestureDetector(getActivity(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                               float velocityY) {
                            final int numOptions = getResources().getStringArray(R.array.game_names_array).length-1;
                            final int SWIPE_MIN_DISTANCE = 120;
                            final int SWIPE_MAX_OFF_PATH = 250;
                            final int SWIPE_THRESHOLD_VELOCITY = 200;
                            try {
                                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                    return false;
                                // if swipe right to left
                                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                    if (position < numOptions)
                                        mNavigationDrawerFragment.selectItem(position + 1);
                                } // if swipe left to right
                                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                    if (position > 0)
                                        mNavigationDrawerFragment.selectItem(position - 1);
                                }
                            } catch (Exception e) {
                                Log.d(MainActivity.TAG, e.toString());
                            }
                            return super.onFling(e1, e2, velocityX, velocityY);
                        }
                    });

            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gesture.onTouchEvent(event);
                }
            });

            return rootView;
        }
    }

}
