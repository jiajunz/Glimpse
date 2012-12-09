package edu.cmu.glimpse.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import com.facebook.FacebookActivity;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.GraphPlaceListCallback;
import com.facebook.Response;
import com.facebook.model.GraphPlace;

import edu.cmu.glimpse.entry.EntryImage;
import edu.cmu.glimpse.entry.EntryPlace;
import edu.cmu.glimpse.entry.GlimpseEntry;
import edu.cmu.glimpse.modules.GlimpseAccountManager;
import edu.cmu.glimpse.sqlite.GlimpseDataSource;
import edu.cmu.glimpse.widget.ImageAdapter;
import edu.cmu.glimpse.widget.ImageDialogFragment;

@SuppressWarnings("deprecation")
public class EntryEditActivity extends FacebookActivity {
    private static final String TAG = "EntryEditActivity";

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
                imageDialog.setImageBitmap(mImageList.get(position).getImageBitmap());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.post_facebook_status:
                postFacebookStatus();
                break;

            case R.id.upload_facebook_image:
                for (EntryImage img : mImageList) {
                    uploadFacebookImage(img.getImageBitmap());
                }
                break;

            default:
                Log.e(TAG, "Unknown options item selected");
        }
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
        
        GlimpseAccountManager.syncDropbox();
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
            mImageAdapter.addImage(image.getImageBitmap());
            mImageList.add(new EntryImage(image.getImageId(), image.getImageBitmap()));
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

    private void postFacebookStatus() {
        final Request statusRequest = Request.newStatusUpdateRequest(this.getSession(), mEditText.getText()
                .toString(),
                new Request.Callback() {

                    public void onCompleted(Response response) {
                        showResponseToast(response, "Success", "Status post failed");
                    }

                });

        if (mPlace != null && mPlace.getLocation() != null) {
            searchFacebookPlace(new GraphPlaceListCallback() {

                public void onCompleted(List<GraphPlace> places, Response response) {
                    if (places != null && places.size() > 0) {
                        GraphPlace myPlace = places.get(0);
                        Bundle params = new Bundle();
                        params.putString("place", myPlace.getId());
                        params.putString("message", mEditText.getText().toString());
                        JSONObject coordinates = new JSONObject();
                        try {
                            coordinates.put("latitude", myPlace.getLocation().getLatitude());
                            coordinates.put("longitude", myPlace.getLocation().getLongitude());
                            params.putString("coordinates", coordinates.toString());
                            Request checkinRequest = new Request(getSession(), "me/checkins",
                                    params, HttpMethod.POST, new Request.Callback() {

                                        public void onCompleted(Response response) {
                                            showResponseToast(response, "Success", "Checkin failed");
                                        }

                                    });
                            Request.executeBatchAsync(checkinRequest);
                            return;
                        } catch (JSONException e) {
                            Log.e(TAG, "Error create JSONObject coordinates, send message without checkin");
                        }
                    }
                    Request.executeBatchAsync(statusRequest);
                }
            });
        } else {
            Request.executeBatchAsync(statusRequest);
        }
    }

    private void uploadFacebookImage(Bitmap bitmap) {
        final Bundle params = new Bundle();
        params.putParcelable("picture", bitmap);
        if (mEditText.getText().toString().length() > 0) {
            params.putString("name", mEditText.getText().toString());
        }
        if (mPlace != null && mPlace.getLocation() != null) {
            searchFacebookPlace(new GraphPlaceListCallback() {

                public void onCompleted(List<GraphPlace> places, Response response) {
                    if (places != null && places.size() > 0) {
                        GraphPlace myPlace = places.get(0);
                        params.putString("place", myPlace.getId());
                        try {
                            JSONObject coordinates = new JSONObject();
                            coordinates.put("latitude", myPlace.getLocation().getLatitude());
                            coordinates.put("longitude", myPlace.getLocation().getLongitude());
                            params.putString("coordinates", coordinates.toString());
                        } catch (JSONException e) {
                            Log.e(TAG, "Error create JSONObject coordinates, upload picture without checkin");
                        }
                        Request request = new Request(getSession(), "me/photos", params, HttpMethod.POST,
                                new Request.Callback() {

                                    public void onCompleted(Response response) {
                                        showResponseToast(response, "Success", "Upload picture failed");
                                    }

                                });
                        Request.executeBatchAsync(request);
                    }
                }

            });
        } else {
            Request request = new Request(getSession(), "me/photos", params, HttpMethod.POST, new Request.Callback() {

                public void onCompleted(Response response) {
                    showResponseToast(response, "Success", "Upload picture failed");
                }

            });
            Request.executeBatchAsync(request);
        }
    }

    private void searchFacebookPlace(GraphPlaceListCallback graphPlaceListCallback) {
        Request locationRequest = Request.newPlacesSearchRequest(getSession(), mPlace.getLocation(),
                50, 10, mPlace.getName(), graphPlaceListCallback);

        Request.executeBatchAsync(locationRequest);
    }

    private void showResponseToast(Response response, String successMsg, String errMsg) {
        Toast.makeText(this, response.getError() == null ? successMsg : errMsg, Toast.LENGTH_SHORT).show();
    }
}
