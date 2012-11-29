package edu.cmu.glimpse.activities;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.cmu.glimpse.entry.GlimpseEntry;
import edu.cmu.glimpse.sqlite.GlimpseDataSource;

public class EntryEditActivity extends Activity {
    private EditText mEditText;
    private Button mSaveEntryButton;
    private GlimpseDataSource mDataSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_edit);

        mDataSource = new GlimpseDataSource(this);

        mEditText = (EditText) findViewById(R.id.entryEditText);
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mEditText.setText(((GlimpseEntry) bundle.getParcelable("selected")).getContent());
        }

        mSaveEntryButton = (Button) findViewById(R.id.saveEntryButton);
        mSaveEntryButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String content = mEditText.getText().toString();
                if (bundle == null) {
                    mDataSource.createEntry(content);
                } else {
                    GlimpseEntry entry = (GlimpseEntry) bundle.getParcelable("selected");
                    String originalContent = entry.getContent();
                    if (!content.equals(originalContent)) {
                        mDataSource.updateEntry(entry.getId(), content);
                    }
                }
            }

        });

        Button showEntryButton = (Button) findViewById(R.id.showEntryButton);
        showEntryButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                List<GlimpseEntry> list = mDataSource.getAllEntries();
                for (GlimpseEntry entry : list) {
                    Log.d(this.getClass().getName(), entry.getContent());
                }
            }

        });
    }

    @Override
    protected void onResume() {
        mDataSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDataSource.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_entry_edit, menu);
        return true;
    }
}
