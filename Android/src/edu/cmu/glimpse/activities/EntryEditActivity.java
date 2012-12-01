package edu.cmu.glimpse.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.Toast;
import edu.cmu.glimpse.entry.EntryImage;
import edu.cmu.glimpse.entry.GlimpseEntry;
import edu.cmu.glimpse.sqlite.GlimpseDataSource;
import edu.cmu.glimpse.widget.ImageAdapter;
import edu.cmu.glimpse.widget.ImageDialogFragment;

@SuppressWarnings("deprecation")
public class EntryEditActivity extends FragmentActivity {

    private List<EntryImage> mImageList;
    private List<Integer> mImageUpdateList;

    private EditText mEditText;
    private Gallery mGallery;
    private ImageAdapter mImageAdapter;
    private ImageButton mNewImageButton;
    private ImageButton mLocationButton;
    private Button mSaveEntryButton;
    private GlimpseDataSource mDataSource;

    private static final int CAMERA_PIC_REQUEST = 1337;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_edit);

        mImageUpdateList = new ArrayList<Integer>();
        mImageList = new ArrayList<EntryImage>();
        mDataSource = new GlimpseDataSource(this);
        mDataSource.open();

        mEditText = (EditText) findViewById(R.id.entryEditText);

        mGallery = (Gallery) findViewById(R.id.entryGallery);
        mGallery.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageDialogFragment imageDialog = new ImageDialogFragment();
                imageDialog.setImageBitmap(mImageList.get(position).getImage());
                imageDialog.show(getSupportFragmentManager(), "123");
            }

        });
        mImageAdapter = new ImageAdapter(this);
        mGallery.setAdapter(mImageAdapter);

        mNewImageButton = (ImageButton) findViewById(R.id.imgButton);
        mNewImageButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }

        });

        mLocationButton = (ImageButton) findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent locationIntent = new Intent(EntryEditActivity.this, LocationActivity.class);
                startActivity(locationIntent);
            }

        });

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            GlimpseEntry entry = (GlimpseEntry) bundle.getParcelable("selected");
            mEditText.setText(entry.getContent());
            loadImages(entry);
        }

        mSaveEntryButton = (Button) findViewById(R.id.saveEntryButton);
        mSaveEntryButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                saveEntry(bundle);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            mImageUpdateList.add(mImageAdapter.getCount());
            mImageAdapter.addImage(thumbnail);
            mImageList.add(new EntryImage(mImageAdapter.getCount(), thumbnail));
        } else {
            Toast.makeText(this, "Picture NOT taken", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataSource.open();
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

    private void saveEntry(Bundle bundle) {
        String content = mEditText.getText().toString();
        if (bundle == null) {
            GlimpseEntry entry = mDataSource.createEntry(content);
            if (!mImageList.isEmpty()) {
                saveImages(entry.getId());
            }
        } else {
            GlimpseEntry entry = (GlimpseEntry) bundle.getParcelable("selected");
            String originalContent = entry.getContent();
            if (!content.equals(originalContent)) {
                mDataSource.updateEntry(entry.getId(), content);
            }
            for (int updateIndex : mImageUpdateList) {
                mDataSource.insertImage(entry.getId(), mImageList.get(updateIndex));
            }
            mImageUpdateList.clear();
        }
    }

    private void saveImages(long entryId) {
        for (EntryImage image : mImageList) {
            mDataSource.insertImage(entryId, image);
        }
    }

    private void loadImages(GlimpseEntry entry) {
        entry.setImageList(mDataSource.getImagesForEntry(entry.getId()));

        for (EntryImage image : entry.getImageList()) {
            mImageAdapter.addImage(image.getImage());
            mImageList.add(new EntryImage(mImageAdapter.getCount(), image.getImage()));
        }
    }
}
