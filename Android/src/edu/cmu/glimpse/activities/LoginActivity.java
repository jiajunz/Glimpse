package edu.cmu.glimpse.activities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.facebook.FacebookActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import edu.cmu.glimpse.modules.DropboxSyncModule;
import edu.cmu.glimpse.modules.GlimpseAccountManager;
import edu.cmu.glimpse.sqlite.GlimpseSQLiteHelper;

public class LoginActivity extends FacebookActivity {

    private static final String TAG = "LoginActivity";

    private ImageView mLoadingImage;
    private AnimationDrawable mLoadingAnimation;
    private ImageView mLogoImageView;
    private LoginButton mFacebookLoginButton;
    private GraphUser mFacebookUser;
    private ImageButton mDropboxLoginButton;
    private DropboxSyncModule mDropboxSyncModule;
    private boolean mUserRequested = false;
    private boolean mIsReturned = false;
    private boolean mHasEntered = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDropboxSyncModule = new DropboxSyncModule(this);
        GlimpseAccountManager.mDropboxSyncModule = mDropboxSyncModule;

        mLoadingImage = (ImageView) findViewById(R.id.login_loading_image);
        mLoadingImage.setBackgroundResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mLoadingImage.getBackground();

        mFacebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        mFacebookLoginButton.setPublishPermissions(Arrays.asList("publish_actions", "publish_checkins"));

        if (isSessionOpen()) {
            if (mFacebookUser == null) {
                requestUser();
            }
        } else {
            mLoadingImage.setVisibility(View.INVISIBLE);
            mFacebookLoginButton.setVisibility(View.VISIBLE);
        }

        mDropboxLoginButton = (ImageButton) findViewById(R.id.dropbox_login_button);
        mDropboxLoginButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mDropboxSyncModule.authenticate();
            }

        });

        mLogoImageView = (ImageView) findViewById(R.id.logo_image_view);
        mLogoImageView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (mFacebookUser != null && mDropboxSyncModule.isAuthenticated()) {
                    enter();
                } else {
                    Log.w(TAG, "user == null");
                }
            }

        });
        mLogoImageView.setClickable(isSessionOpen());

        // get Google account
        getGoogleAccount();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFacebookUser != null && mDropboxSyncModule.isAuthenticated() && !mIsReturned) {
            enter();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    protected void onSessionStateChange(SessionState state, Exception exception) {
        // user has either logged in or not ...
        if (state.isOpened()) {
            requestUser();
        }

        mLogoImageView.setClickable(state.isOpened());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mIsReturned = true;
        mHasEntered = false;

        mLoadingImage.setVisibility(View.INVISIBLE);
        mFacebookLoginButton.setVisibility(View.VISIBLE);
        mLogoImageView.setClickable(isSessionOpen() && mFacebookUser != null);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private synchronized void requestUser() {
        if (mUserRequested) {
            return;
        }
        mUserRequested = true;
        Log.d(TAG, "request user");
        // make request to the /me API
        Request request = Request.newMeRequest(this.getSession(), new Request.GraphUserCallback() {
            // callback after Graph API response with user object
            public void onCompleted(GraphUser user, Response response) {
                mFacebookUser = user;

                new AsyncTask<String, Void, Bitmap>() {

                    @Override
                    protected Bitmap doInBackground(String... userId) {
                        try {
                            URL image = new URL("http://graph.facebook.com/" + userId[0] + "/picture?type=large");
                            Bitmap profileBitmap = BitmapFactory.decodeStream(image.openConnection().getInputStream());
                            return profileBitmap;
                        } catch (MalformedURLException e) {
                            Log.w(TAG, "Fetch profile image failed");
                        } catch (IOException e) {
                            Log.w(TAG, "Fetch profile image failed");
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap profileBitmap) {
                        if (profileBitmap == null) {
                            return;
                        }
                        ImageView profileImage = (ImageView) findViewById(R.id.user_profile_image);
                        profileImage.setVisibility(View.VISIBLE);
                        profileImage.setImageBitmap(profileBitmap);
                    }

                }.execute(user.getId());

                if (user != null && mDropboxSyncModule.isAuthenticated()) {
                    enter();
                }
            }
        });
        Request.executeBatchAsync(request);
    }

    private void gotAccount(AccountManager manager, Account account) {
        Log.d(TAG, "Got google account: " + account.name);
    }

    private void getGoogleAccount() {
        final AccountManager accountManager = AccountManager.get(getApplicationContext());
        final Account[] accounts = accountManager.getAccountsByType("com.google");

        if (accounts == null || accounts.length == 0) {
            // No login accounts found
            Log.w(TAG, "No Google Accounts found");
        }
        if (accounts.length > 1) {
            // show dialog to choose one account
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a Google account");
            String[] names = new String[accounts.length];
            for (int i = 0; i < accounts.length; i++) {
                names[i] = accounts[i].name;
            }
            builder.setItems(names, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Stuff to do when the account is selected by the user
                    gotAccount(accountManager, accounts[which]);
                }
            }).create().show();
        } else {
            gotAccount(accountManager, accounts[0]);
        }

    }

    private synchronized void enter() {
        if (mHasEntered) {
            return;
        }
        mHasEntered = true;
        mLoadingImage.setVisibility(View.VISIBLE);
        mLoadingAnimation.start();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "start sync");
                mDropboxSyncModule.syncContent(GlimpseSQLiteHelper.DB_NAME, GlimpseSQLiteHelper.DB_PATH);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mLoadingAnimation.stop();
                mLoadingImage.setVisibility(View.GONE);

                TranslateAnimation animation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f,
                        TranslateAnimation.ABSOLUTE, 0f,
                        TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                        TranslateAnimation.RELATIVE_TO_PARENT, -1.0f);
                animation.setDuration(2000);
                animation.setInterpolator(new LinearInterpolator());
                animation.setAnimationListener(new AnimationListener() {

                    public void onAnimationStart(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    public void onAnimationRepeat(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    public void onAnimationEnd(Animation animation) {
                        Intent calendarIntent = new Intent(LoginActivity.this, CalendarActivity.class);
                        calendarIntent.putExtra("facebookUser", mFacebookUser.getInnerJSONObject().toString());
                        startActivityForResult(calendarIntent, 0);
                    }
                });
                mLogoImageView.startAnimation(animation);
            }

        }.execute();
    }
}
