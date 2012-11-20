package edu.cmu.glimpse.activities;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.ListView;

import com.google.inject.Inject;

import edu.cmu.glimpse.modules.EntryManageModule;

public class CalendarActivity extends Activity {
    private CalendarView mCalendarView;
    private ListView mListView;

    @Inject
    private EntryManageModule mEntryManageModule;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        // set calendar to square
        int width = mCalendarView.getHeight();
        mCalendarView.setLayoutParams(new LayoutParams(width, width));
        mCalendarView.setOnDateChangeListener(new OnDateChangeListener() {

            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                updateListView(year, month, dayOfMonth);
            }

        });

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO start new activity according to selected item

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_calendar, menu);
        return true;
    }

    private void updateListView(int year, int month, int dayOfMonth) {
        List<String> titles = mEntryManageModule.getTitles(year, month, dayOfMonth);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.calendar_entry, titles);
        mListView.setAdapter(listAdapter);
    }
}
