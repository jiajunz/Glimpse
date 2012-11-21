package edu.cmu.glimpse.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EntryEditActivity extends Activity {
    private EditText mEditText;
    private Button mSaveEntryButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_edit);

        mEditText = (EditText) findViewById(R.id.entryEditText);
        mSaveEntryButton = (Button) findViewById(R.id.saveEntryButton);
        mSaveEntryButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String content = mEditText.getText().toString();
                System.out.println(content);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_entry_edit, menu);
        return true;
    }
}
