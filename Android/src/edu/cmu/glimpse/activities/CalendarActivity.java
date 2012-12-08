package edu.cmu.glimpse.activities;

import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.ListView;

import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

import edu.cmu.glimpse.entry.GlimpseEntryPreview;
import edu.cmu.glimpse.sqlite.GlimpseDataSource;

public class CalendarActivity extends Activity {
    private static final String TAG = "CalendarActivity";

    private GraphUser mFacebookUser;
    private CalendarView mCalendarView;
    private Button mNewEntryButton;
    private ListView mListView;
    private GlimpseDataSource mDataSource;
    private Calendar mSelectedDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // lock orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_calendar);

        mDataSource = new GlimpseDataSource(this);
        mSelectedDate = Calendar.getInstance();

        String facebookUserString = getIntent().getStringExtra("facebookUser");
        try {
            mFacebookUser = GraphObject.Factory.create(new JSONObject(facebookUserString), GraphUser.class);
        } catch (JSONException e) {
            Log.w(TAG, "Get facebook user failed");
            e.printStackTrace();
        }

        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        mCalendarView.setOnDateChangeListener(new OnDateChangeListener() {

            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                mSelectedDate.set(year, month, dayOfMonth);
                updateListView();
            }

        });

        mNewEntryButton = (Button) findViewById(R.id.newEntryButton);
        mNewEntryButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(CalendarActivity.this, EntryEditActivity.class);
                startActivity(intent);
            }

        });

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                GlimpseEntryPreview selected = (GlimpseEntryPreview) mListView.getItemAtPosition(arg2);
                Bundle bundle = new Bundle();
                bundle.putParcelable("selected", selected.getEntry());
                Intent intent = new Intent(CalendarActivity.this, EntryEditActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }

        });
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
                return true;
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_calendar, menu);
        return true;
    }

    @Override
    protected void onResume() {
        mDataSource.open();
        mSelectedDate.setTimeInMillis(mCalendarView.getDate());
        updateListView();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDataSource.close();
        super.onPause();
    }

    private void updateListView() {
        List<GlimpseEntryPreview> titles = mDataSource.getEntryForOneDay(mSelectedDate);
        ArrayAdapter<GlimpseEntryPreview> listAdapter = new ArrayAdapter<GlimpseEntryPreview>(this,
                R.layout.calendar_entry, titles);
        mListView.setAdapter(listAdapter);
    }

    private void showDeleteDialog(final int entryPosition) {
        new AlertDialog.Builder(this).setTitle("Delete Note?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        GlimpseEntryPreview selected = (GlimpseEntryPreview) mListView.getItemAtPosition(entryPosition);
                        mDataSource.deleteEntry(selected.getId());
                        updateListView();
                    }

                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                }).create().show();
    }
}
