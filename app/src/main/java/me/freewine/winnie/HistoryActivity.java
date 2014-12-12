package me.freewine.winnie;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class HistoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<Cursor> {

        private ListView historyList;
        private HistoryAdapter mAdapter;

        // Identifies a particular Loader being used in this component
        private static final int HISTORY_LOADER = 0;
        private static final String[] PROJECTION =
                {
                        HistoryContract._ID,
                        HistoryContract.COLUMN_NAME_CNT,
                        HistoryContract.COLUMN_NAME_CHANGE,
                        HistoryContract.COLUMN_NAME_TIME,
                };

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_history, container, false);

            historyList = (ListView) rootView.findViewById(R.id.history_lv);

            // Sets the ListView's data adapter
            mAdapter = new HistoryAdapter(getActivity());
            //historyList.setOnItemClickListener(this);
            //historyList.setOnItemLongClickListener(this);
            historyList.setAdapter(mAdapter);

            getLoaderManager().initLoader(HISTORY_LOADER, null, this);

            return rootView;
        }

        /*
         * This callback is invoked when the framework is starting or re-starting the Loader. It
         * returns a CursorLoader object containing the desired query
         */
        @Override
        public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        /*
         * Takes action based on the ID of the Loader that's being created
         */
            switch (loaderID) {
                case HISTORY_LOADER:
                    // Returns a new CursorLoader
                    return new CursorLoader(
                            getActivity(),                                     // Context
                            HistoryContract.HISTORY_TABLE_CONTENTURI,  // Table to query
                            PROJECTION,                                        // Projection to return
                            null,                                              // No selection clause
                            null,                                              // No selection arguments
                            HistoryContract._ID + " DESC"                                               // Default sort order
                    );
                default:
                    // An invalid id was passed in
                    return null;

            }

        }

        /*
         * Invoked when the CursorLoader finishes the query. A reference to the Loader and the
         * returned Cursor are passed in as arguments
         */
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor returnCursor) {

        /*
         *  Changes the adapter's Cursor to be the results of the load. This forces the View to
         *  redraw.
         */

            mAdapter.changeCursor(returnCursor);
        }

        /*
         * Invoked when the CursorLoader is being reset. For example, this is called if the
         * data in the provider changes and the Cursor becomes stale.
         */
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            // Sets the Adapter's backing data to null. This prevents memory leaks.
            mAdapter.changeCursor(null);
        }

        /**
         * Defines a custom View adapter that extends CursorAdapter.
         * 根据收或者发的消息显示不同方向的聊天气泡.
         */
        private class HistoryAdapter extends CursorAdapter {

            /**
             * Simplified constructor that calls the super constructor with the input Context,
             * a null value for Cursor, and no flags
             *
             * @param context A Context for this object
             */
            public HistoryAdapter(Context context) {
                super(context, null, false);
            }

            public class ViewHolder {
                // each data item is just a string in this case
                TextView tv_cnt;
                TextView tv_change;
                TextView tv_time;

                public ViewHolder(View v) {
                    tv_cnt = (TextView) v.findViewById(R.id.tv_cnt);
                    tv_change = (TextView) v.findViewById(R.id.tv_change);
                    tv_time = (TextView) v.findViewById(R.id.tv_time);
                }
            }

            /**
             * Binds a View and a Cursor
             *
             * @param view    An existing View object
             * @param context A Context for the View and Cursor
             * @param cursor  The Cursor to bind to the View, representing one row of the returned query.
             */
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Gets a handle to the View
                ViewHolder vh = (ViewHolder) view.getTag();

                final String cnt = String.valueOf(cursor.getInt(cursor.getColumnIndex(HistoryContract.COLUMN_NAME_CNT)));
                final String change = String.valueOf(cursor.getString(cursor.getColumnIndex(HistoryContract.COLUMN_NAME_CHANGE)));
                final long timeStamp = cursor.getLong(cursor.getColumnIndex(HistoryContract.COLUMN_NAME_TIME));


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStr = sdf.format(new Date(timeStamp));

                vh.tv_cnt.setText("剩余：" + cnt);
                vh.tv_change.setText(change);
                vh.tv_time.setText(timeStr);

            }

            /**
             * Creates a new View that shows the contents of the Cursor
             *
             * @param context   A Context for the View and Cursor
             * @param cursor    The Cursor to display. This is a single row of the returned query
             * @param viewGroup The viewGroup that's the parent of the new View
             * @return the newly-created View
             */
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                View layoutView;

                // Gets a new layout inflater instance
                LayoutInflater inflater = LayoutInflater.from(context);

                layoutView = inflater.inflate(R.layout.history_item, null);
                ViewHolder vh = new ViewHolder(layoutView);

                // Sets the layoutView's tag to be the same as the thumbnail image tag.
                layoutView.setTag(vh);
                return layoutView;
            }

        }
    }
}
