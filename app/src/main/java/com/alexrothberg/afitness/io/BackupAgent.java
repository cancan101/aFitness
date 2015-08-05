package com.alexrothberg.afitness.io;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.alexrothberg.afitness.DbAdapter;

public class BackupAgent extends BackupAgentHelper {
    private static final String TAG = BackupAgent.class.getSimpleName();

    private static final String PREFS_BACKUP_KEY = "prefs";
    private static final String DATA_BACKUP_KEY = "data";
    private static final String DATA_DB = DbAdapter.DATABASE_NAME;

    private Object fileLock = new Object();

    @Override
    public void onCreate() {
        addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, getPackageName()
                + "_preferences"));
        addHelper(DATA_BACKUP_KEY, new DbBackupHelper(this, DATA_DB));
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
        Log.d(TAG, "onBackup");

        synchronized (fileLock) {
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState)
            throws IOException {
        Log.d(TAG, "onRestore appVersionCode = " + appVersionCode);

        synchronized (fileLock) {
            Log.d(TAG, "onRestore in-lock");
            super.onRestore(data, appVersionCode, newState);
        }
    }
}
