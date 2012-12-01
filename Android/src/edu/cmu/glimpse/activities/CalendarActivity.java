package edu.cmu.glimpse.activities;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.ListView;
import edu.cmu.glimpse.entry.GlimpseEntryPreview;
import edu.cmu.glimpse.sqlite.GlimpseDataSource;

public class CalendarActivity extends Activity {
    private CalendarView mCalendarView;
    private Button mNewEntryButton;
    private ListView mListView;
    private GlimpseDataSource mDataSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // lock orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_calendar);

        mDataSource = new GlimpseDataSource(this);

        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        // set calendar to square
        // mCalendarView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
        //
        // public void onGlobalLayout() {
        // int width = mCalendarView.getWidth();
        // mCalendarView.setLayoutParams(new RelativeLayout.LayoutParams(width, width * 3 / 4));
        // }
        //
        // });
        mCalendarView.setOnDateChangeListener(new OnDateChangeListener() {

            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Calendar date = Calendar.getInstance();
                date.set(year, month, dayOfMonth);
                updateListView(date);
            }

        });

        mNewEntryButton = (Button) findViewById(R.id.newEntryButton);
        mNewEntryButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO start new activity for new entry creation
                Intent intent = new Intent(CalendarActivity.this, EntryEditActivity.class);
                startActivity(intent);
            }

        });

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO start new activity according to selected item
                GlimpseEntryPreview selected = (GlimpseEntryPreview) mListView.getItemAtPosition(arg2);

                Bundle bundle = new Bundle();
                bundle.putParcelable("selected", selected.getEntry());

                Intent intent = new Intent(CalendarActivity.this, EntryEditActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
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
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(mCalendarView.getDate());
        updateListView(selectedDate);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDataSource.close();
        super.onPause();
    }

    private void updateListView(Calendar date) {
        List<GlimpseEntryPreview> titles = mDataSource.getEntryForOneDay(date);
        ArrayAdapter<GlimpseEntryPreview> listAdapter = new ArrayAdapter<GlimpseEntryPreview>(this,
                R.layout.calendar_entry, titles);
        mListView.setAdapter(listAdapter);
    }
}
