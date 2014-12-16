package com.ekavali.justwrite;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.SearchManager;
import android.support.v7.widget.SearchView;
import android.content.Context;
import android.content.Intent;
import android.content.CursorLoader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.content.Loader;
import android.app.LoaderManager;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private SimpleCursorAdapter mAdapter;
    private String mCurFilter = null;
    private MyLoaderCallbacks mLoaderCallbacks;
    private View mCurStoryItem = null;

    //Object to handle callbacks from Loader
    private class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>
    {
        private Context context;

        public MyLoaderCallbacks(Context context) {
            this.context = context;
        }

        /**
         * Loader callback: Called when a new loader is created
         */
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {
                    StoryContract.Story._ID,
                    StoryContract.Story.COLUMN_NAME_TITLE,
                    StoryContract.Story.COLUMN_NAME_DESCRIPTION,
                    StoryContract.COLUMN_NAME_MODIFY_DATE};
            String selection = null;
            String[] selectionArgs = null;
            if (mCurFilter != null) {
                //Set selection args
                selection = StoryContract.Story.COLUMN_NAME_TITLE + " LIKE ?";
                selectionArgs = new String[] {"%" + mCurFilter + "%"};
            }
            // Create a loader to get all stories
            return new CursorLoader(context, StoryContract.Story.CONTENT_URI, projection, selection, selectionArgs, StoryContract.DEFAULT_SORT_ORDER);
        }

        /**
         * Loader callback: Called when data has been fetched
         */
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
            Log.v(TAG, "GetItem: " + mAdapter.getViewBinder());
            //mAdapter.swapCursor(data);
        }

        /**
         * Loader callback: Called when loader was reset, making its data unavailable
         */
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }

    }

    //Object that handles query text change events
    private SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        /**
         * SearchView: Called when query changes
         */
        public boolean onQueryTextChange(String newText) {
            if (TextUtils.isEmpty(newText)) return true;
            if (newText.equals(mCurFilter)) return true;
            mCurFilter = newText;
            getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
            Log.v(TAG, mCurFilter);
            return true;
        }

        /**
         * SearchView: Called when search is submitted
         */
        public boolean onQueryTextSubmit(String query) {
            Log.v(TAG, query);
            return true;
        }

    };

    /**
     * Custom view binder for story list
     */
    private SimpleCursorAdapter.ViewBinder mViewBinder = new SimpleCursorAdapter.ViewBinder() {
        /**
         * Provide custom bindings for story list
         * @param view
         * @param cursor
         * @param columnIndex
         * @return
         */
         public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
          if (cursor.getColumnName(columnIndex).equals(StoryContract.COLUMN_NAME_MODIFY_DATE)){
              SimpleDateFormat df = new SimpleDateFormat("d MMM yyyy, h:mm a");
              TextView textView = (TextView) view;
              String date = df.format(new Date(cursor.getLong(columnIndex)));
              textView.setText(date);
              Log.v(TAG, date);
              return true;
          }
          return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);

        mLoaderCallbacks = new MyLoaderCallbacks(this);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {StoryContract.Story.COLUMN_NAME_TITLE,
                StoryContract.Story.COLUMN_NAME_DESCRIPTION, StoryContract.COLUMN_NAME_MODIFY_DATE};
        int[] toViews = {R.id.storyTitle, R.id.storyDesc, R.id.storyModifyDate}; // The TextView in simple_list_item_2

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.story_item, null,
                fromColumns, toViews, 0);
        mAdapter.setViewBinder(mViewBinder);
        ListView storyListView = (ListView) findViewById(R.id.storyList);
        storyListView.setAdapter(mAdapter);
        storyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mCurStoryItem != null) {
                    mCurStoryItem.findViewById(R.id.storyEditBtn).setVisibility(View.GONE);
                }
                view.findViewById(R.id.storyEditBtn).setVisibility(View.VISIBLE);
                mCurStoryItem = view;
                //Toggle detail view
                Log.v(TAG, "id: " + id);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Get the search view
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(mQueryTextListener);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Handle search intent */
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     Handle settings
     */
    private void openSettings() {
        //Add record
        addStory();
    }

    /**
     Add a new story record
     */
    private void addStory() {
        ContentValues values = new ContentValues();
        values.put(StoryContract.Story.COLUMN_NAME_TITLE, "Story1 long long long long long long long story tilte ");
        values.put(StoryContract.Story.COLUMN_NAME_DESCRIPTION, "Story1 Description");
        Uri newRowUri = getContentResolver().insert(StoryContract.Story.CONTENT_URI, values);
        Log.v(TAG, "values: " + values);
        Log.v(TAG, "newRowUri: " + newRowUri);
    }

    /**
     * Called when Edit is clicked on current list item, launch story view
     * @param view
     */
    public void launchStoryActivity(View view) {
        Log.v(TAG, "Edit clicked");

    }
}
