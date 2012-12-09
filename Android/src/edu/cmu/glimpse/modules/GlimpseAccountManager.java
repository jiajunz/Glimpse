package edu.cmu.glimpse.modules;

import com.facebook.model.GraphUser;

import edu.cmu.glimpse.sqlite.GlimpseSQLiteHelper;

public class GlimpseAccountManager {

    public static DropboxSyncModule mDropboxSyncModule;
    public static GraphUser mFacebookUser;

    /**
     * Sync with drop box if possible
     */
    public static void syncDropbox() {
        if (mDropboxSyncModule != null) {
            new Thread() {
                @Override
                public void run() {
                    if (GlimpseAccountManager.mDropboxSyncModule != null) {
                        GlimpseAccountManager.mDropboxSyncModule.syncContent(GlimpseSQLiteHelper.DB_NAME,
                                GlimpseSQLiteHelper.DB_PATH);
                    }
                }
            }.start();
        }
    }
}
