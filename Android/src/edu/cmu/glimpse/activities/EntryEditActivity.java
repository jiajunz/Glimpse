package edu.cmu.glimpse.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.Toast;
import edu.cmu.glimpse.entry.EntryImage;
import edu.cmu.glimpse.entry.EntryPlace;
import edu.cmu.glimpse.entry.GlimpseEntry;
import edu.cmu.glimpse.sqlite.GlimpseDataSource;
import edu.cmu.glimpse.widget.ImageAdapter;
import edu.cmu.glimpse.widget.ImageDialogFragment;

@SuppressWarnings("deprecation")
public class EntryEditActivity extends FragmentActivity {

    private List<EntryImage> mImageList;
    private Set<EntryImage> mUpdatedImages;
    private EntryPlace mPlace;
    private boolean mPlaceUpdated = false;
    private long mNextImageId = 1;

    private EditText mEditText;
    private Button mLocationText;
    private Gallery mGallery;
    private ImageAdapter mImageAdapter;
    private ImageButton mNewImageButton;
    private ImageButton mLocationButton;
    private Button mSaveEntryButton;
    private GlimpseDataSource mDataSource;
    private GlimpseEntry mGlimpseEntry;

    private static final int CAMERA_PIC_REQUEST = 18641;
    private static final int LOCATION_REQUEST = 18648;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_edit);

        mUpdatedImages = new HashSet<EntryImage>();
        mImageList = new ArrayList<EntryImage>();
        mDataSource = new GlimpseDataSource(this);
        mDataSource.open();

        mEditText = (EditText) findViewById(R.id.entryEditText);
        mLocationText = (Button) findViewById(R.id.locationText);

        mGallery = (Gallery) findViewById(R.id.entryGallery);
        mGallery.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageDialogFragment imageDialog = new ImageDialogFragment();
                imageDialog.setImageBitmap(mImageList.get(position).getImage());
                imageDialog.show(getSupportFragmentManager(), "Photo");
            }

        });
        mGallery.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
                return true;
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
                startActivityForResult(locationIntent, LOCATION_REQUEST);
            }

        });

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mGlimpseEntry = (GlimpseEntry) bundle.getParcelable("selected");
            mEditText.setText(mGlimpseEntry.getContent());
            if (mGlimpseEntry.getPlace() != null) {
                mPlace = mGlimpseEntry.getPlace();
                mLocationText.setText(mPlace.getName());
                mLocationText.setVisibility(View.VISIBLE);
            }
            loadImages();
        }

        mSaveEntryButton = (Button) findViewById(R.id.saveEntryButton);
        mSaveEntryButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                saveEntry();
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_PIC_REQUEST:
                if (resultCode == RESULT_OK) {
                    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                    mImageAdapter.addImage(thumbnail);
                    System.out.println("New image id: " + mNextImageId);
                    EntryImage newImage = new EntryImage(mNextImageId++, thumbnail);
                    mImageList.add(newImage);
                    mUpdatedImages.add(newImage);
                } else {
                    Toast.makeText(this, "Picture NOT taken", Toast.LENGTH_LONG).show();
                }
                break;

            case LOCATION_REQUEST:
                if (resultCode == RESULT_OK) {
                    EntryPlace newPlace = (EntryPlace) data.getExtras().getParcelable("selected");
                    if (!newPlace.equals(mPlace)) {
                        mPlace = newPlace;
                        mPlaceUpdated = true;
                    }
                    mLocationText.setText(mPlace.getName());
                    mLocationText.setVisibility(View.VISIBLE);
                }
                break;

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

    private void saveEntry() {
        String content = mEditText.getText().toString();
        if (mGlimpseEntry == null) {
            mGlimpseEntry = mDataSource.createEntry(content, mPlace);
            if (!mImageList.isEmpty()) {
                saveAllImages(mGlimpseEntry.getId());
            }
        } else {
            String originalContent = mGlimpseEntry.getContent();
            if (!content.equals(originalContent) || mPlaceUpdated) {
                mDataSource.updateEntry(mGlimpseEntry.getId(), content, mPlace);
                mPlaceUpdated = false;
            }
            for (EntryImage image : mUpdatedImages) {
                mDataSource.insertImage(mGlimpseEntry.getId(), image);
            }
            mUpdatedImages.clear();
        }
    }

    private void saveAllImages(long entryId) {
        for (EntryImage image : mImageList) {
            mDataSource.insertImage(entryId, image);
        }
    }

    private void loadImages() {
        mGlimpseEntry.setImageList(mDataSource.getImagesForEntry(mGlimpseEntry.getId()));
        mNextImageId = mGlimpseEntry.getNextImageId();

        for (EntryImage image : mGlimpseEntry.getImageList()) {
            mImageAdapter.addImage(image.getImage());
            mImageList.add(new EntryImage(image.getImageId(), image.getImage()));
        }
    }

    private void showDeleteDialog(final int imagePosition) {
        new AlertDialog.Builder(this).setTitle("Delete Image?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        mUpdatedImages.remove(mImageList.get(imagePosition));
                        mImageAdapter.deteleImage(imagePosition);
                        if (mGlimpseEntry != null) {
                            mDataSource.deleteImage(mGlimpseEntry.getId(), mImageList.get(imagePosition));
                        }
                        mImageList.remove(imagePosition);
                    }

                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                }).create().show();
    }
}
