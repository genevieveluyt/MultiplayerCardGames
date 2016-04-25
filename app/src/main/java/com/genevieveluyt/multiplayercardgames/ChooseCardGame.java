package com.genevieveluyt.multiplayercardgames;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseCardGame extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    // Use for getting game type from result (data.getExtra(
    public static final String GAME_TYPE_EXTRA = "gameType";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private static int selectedPosition;

    /**
     * Used to store the last screen title. For use in {restoreActionBar()}.
     */
    //private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_card_game);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        //mTitle = getTitle();

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
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    /*public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }*/

    /*public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }*/

    public void onPlayClicked(View view) {
        setResult(RESULT_OK, new Intent().putExtra(GAME_TYPE_EXTRA, selectedPosition));
        finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        //private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            /*Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_choose_card_game, container, false);
            ((TextView) rootView.findViewById(R.id.game_title))
                    .setText(getResources().getStringArray(R.array.game_names_array)[selectedPosition]);
            ((TextView) rootView.findViewById(R.id.game_info))
                    .setText(Html.fromHtml(
                            getResources().getStringArray(R.array.game_info_array)[selectedPosition]));

            // hide Play button if on Other Games page
            Button playButton = (Button) rootView.findViewById(R.id.play_button);
            TextView infoView = (TextView) rootView.findViewById(R.id.game_info);
            TextView moreGamesView = (TextView) rootView.findViewById(R.id.more_games);
            if (selectedPosition == 1) {
                playButton.setVisibility(View.GONE);
                infoView.setVisibility(View.GONE);
                moreGamesView.setVisibility(View.VISIBLE);
            } else {
                playButton.setVisibility(View.VISIBLE);
                infoView.setVisibility(View.VISIBLE);
                moreGamesView.setVisibility(View.GONE);
            }

            return rootView;
        }

        /*@Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ChooseCardGame) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }*/
    }

}
