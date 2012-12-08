package edu.cmu.glimpse.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class DropboxSyncModule {
    private static final String TAG = "GlimpseLog";
    final static private String APP_KEY = "1oxv19hs2pazj53";
    final static private String APP_SECRET = "ybf4jx697ch32yg";
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

    // You don't need to change these, leave them alone.
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    private final DropboxAPI<AndroidAuthSession> mApi;
    private boolean mLoggedIn;
    private final String DATA_DIR = "/Glimpse_Data/";
    private final Context mContext;

    public DropboxSyncModule(Context context) {
        this.mContext = context;
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
    }

    /**
     * sync the File from local filePath to Dropbox Location contentName
     * 
     * @param contentName
     *            the name of file on Dropbox Location
     * @param filePath
     *            the path of Local File
     */
    public void syncContent(String contentName, String filePath) {
        finishAuth();
        File file = new File(filePath);
        Date lastModTime = new Date(file.lastModified());
        try {
            List<Entry> list = mApi.search(DATA_DIR, contentName, 1, false);
            if (list.size() == 0) {
                uploadFile(DATA_DIR + contentName, filePath);
                return;
            }

            Entry entry = mApi.metadata(DATA_DIR + contentName, 1, null, false, null);
            if (entry == null) {
                uploadFile(DATA_DIR + contentName, filePath);
            } else {
                String remoteModTime = entry.modified;
                Date remoteModDate = RESTUtility.parseDate(remoteModTime);

                if (remoteModDate.equals(lastModTime)) {
                    return;
                }

                if (remoteModDate.before(lastModTime)) {
                    Log.i("Gmplimse_Upload remote time : local time", remoteModTime + lastModTime);
                    uploadFile(DATA_DIR + contentName, filePath);
                }
                else {
                    Log.i("Gmplimse_Download remote time : local time", remoteModTime + lastModTime);
                    downloadFile(DATA_DIR + contentName, filePath);
                }
            }

        } catch (DropboxUnlinkedException e) {
            Log.e(TAG, e.toString());
            mApi.getSession().startAuthentication(mContext);

        } catch (DropboxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean isAuthenticated() {
        return mApi.getSession().authenticationSuccessful();
    }

    private void uploadFile(String remotePath, String filePath) {
        Log.d("GlimpseLog", "uploadFile: " + remotePath);
        FileInputStream inputStream = null;
        try {
            File file = new File(filePath);
            inputStream = new FileInputStream(file);
            Entry newEntry = mApi.putFileOverwrite(remotePath, inputStream,
                    file.length(), null);
            Log.i("GlimpseLog", "The uploaded file's rev is: " + newEntry.rev);
        } catch (DropboxUnlinkedException e) {
            // User has unlinked, ask them to link again here.
            Log.e("GlimpseLog", "User has unlinked.");
        } catch (DropboxException e) {
            Log.e("GlimpseLog", "Something went wrong while uploading.");
        } catch (FileNotFoundException e) {
            Log.e("GlimpseLog", "File not found.");
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void downloadFile(String remotePath, String filePath) {
        // Get file.
        FileOutputStream outputStream = null;
        try {
            File file = new File(filePath);
            outputStream = new FileOutputStream(file);
            DropboxFileInfo info = mApi.getFile(remotePath, null, outputStream, null);
            // lastRev = info.
            // Entry entry = info.getMetadata();
            Log.i("GlimpseLog", "The file's rev is: " + info.getMimeType());

            // /path/to/new/file.txt now has stuff in it.
        } catch (DropboxException e) {
            Log.e("GlimpseLog", "Something went wrong while downloading.");
        } catch (FileNotFoundException e) {
            Log.e("GlimpseLog", "File not found.");
        }
        finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Authenticate against the Dropbox
     * 
     */
    public void authenticate() {
        if (!mLoggedIn) {
            mApi.getSession().startAuthentication(mContext);
            mLoggedIn = true;
        }
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    public void logOut() {
        if (mLoggedIn) {
            plogOut();
        }
    }

    private void plogOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();
        // Clear our stored keys
        clearKeys();
        mLoggedIn = false;

    }

    private void finishAuth()
    {

        if (mApi.getSession().authenticationSuccessful()) {
            Log.d(TAG, "auth success");
            try {
                // MANDATORY call to complete auth.
                // Sets the access token on the session
                mApi.getSession().finishAuthentication();

                AccessTokenPair tokens = mApi.getSession().getAccessTokenPair();

                // Provide your own storeKeys to persist the access token pair
                // A typical way to store tokens is using SharedPreferences
                storeKeys(tokens.key, tokens.secret);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    /* Build Androud Session */
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);

            // Provide your own storeKeys to persist the access token pair
            // A typical way to store tokens is using SharedPreferences
            // storeKeys(tokens.key, tokens.secret);
        }

        return session;
    }

    private void clearKeys() {
        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     * 
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys() {
        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Log.i("Glimpse_Error", "No Shared Pref");
        if (prefs != null) {
            String key = prefs.getString(ACCESS_KEY_NAME, null);
            String secret = prefs.getString(ACCESS_SECRET_NAME, null);
            if (key != null && secret != null) {
                String[] ret = new String[2];
                ret[0] = key;
                ret[1] = secret;
                return ret;
            } else {
                return null;
            }
        }
        else {
            return null;
        }
    }

}
