package edu.cmu.glimpse.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class LoginActivity extends FacebookActivity {

    private static final String TAG = "LoginActivity";

    private ImageView mLoadingImage;
    private AnimationDrawable mLoadingAnimation;
    private ImageView mLogoImageView;
    private LoginButton mLoginButton;
    private GraphUser mUser;
    private boolean mUserRequested = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoadingImage = (ImageView) findViewById(R.id.login_loading_image);
        mLoadingImage.setBackgroundResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mLoadingImage.getBackground();
        mLoadingAnimation.start();

        // if (savedInstanceState != null) {
        // String facebookUserString = savedInstanceState.getString("facebookUser");
        // try {
        // mUser = GraphObject.Factory.create(new JSONObject(facebookUserString), GraphUser.class);
        // } catch (JSONException e) {
        // Log.w(TAG, e.getMessage());
        // }
        // }

        mLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);

        if (isSessionOpen()) {
            if (mUser == null) {
                requestUser();
            }
        } else {
            mLoadingImage.setVisibility(View.INVISIBLE);
            mLoginButton.setVisibility(View.VISIBLE);
        }

        mLogoImageView = (ImageView) findViewById(R.id.logo_image_view);
        mLogoImageView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (mUser != null) {
                    enter();
                } else {
                    Log.w(TAG, "mUser == null !");
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
        mLoadingImage.setVisibility(View.INVISIBLE);
        mLoginButton.setVisibility(View.VISIBLE);
        mLogoImageView.setClickable(isSessionOpen() && mUser != null);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "save instance");
        super.onSaveInstanceState(outState);
        if (mUser != null) {
            outState.putString("facebookUser", mUser.getInnerJSONObject().toString());
        }
    }

    private synchronized void requestUser() {
        if (mUserRequested) {
            return;
        } else {
            mUserRequested = true;
        }
        Log.d(TAG, "request user");
        // make request to the /me API
        Request request = Request.newMeRequest(this.getSession(), new Request.GraphUserCallback() {
            // callback after Graph API response with user object
            public void onCompleted(GraphUser user, Response response) {
                mUser = user;
                if (user != null) {
                    enter();
                }
            }
        });
        Request.executeBatchAsync(request);
    }

    private void gotAccount(AccountManager manager, Account account) {
        Log.d(TAG, "Got google account: " + account.name);
        // manager.getAuthToken(account, "", null, this, new GetAuthTokenCallback(), null);
    }

    private void getGoogleAccount() {
        final AccountManager accountManager = AccountManager.get(getApplicationContext());
        final Account[] accounts = accountManager.getAccountsByType("com.google");

        if (accounts == null || accounts.length == 0) {
            // No login accounts found

            ImageButton googleLoginButton = (ImageButton) findViewById(R.id.google_login_button);
            googleLoginButton.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    // Intent intent = new Intent(LoginActivity.this, GoogleLoginActivity.class);
                    // startActivityForResult(intent, GOOGLE_LOGIN_REQUEST);
                    return;
                }

            });
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

    private void enter() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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
                        calendarIntent.putExtra("facebookUser", mUser.getInnerJSONObject().toString());
                        startActivityForResult(calendarIntent, 0);
                    }
                });
                // mLogoImageView.setAnimation(animation);
                mLogoImageView.startAnimation(animation);
            }

        }.execute();
    }
}