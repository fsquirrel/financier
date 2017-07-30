package com.fsquirrelsoft.financier.context;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fsquirrelsoft.commons.util.Files;
import com.fsquirrelsoft.commons.util.Logger;
import com.fsquirrelsoft.financier.ui.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class ScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar now = Calendar.getInstance();
        if (Constants.BACKUP_JOB.equals(intent.getAction())) {
            Contexts ctxs = Contexts.instance();
            try {
                SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
                String prefBackupDir = prefs.getString(Constants.BACKUP_DIR, ctxs.getSdFolder().getAbsolutePath());
                Logger.d("BackupFolder: " + prefBackupDir);
                File backupFolder = new File(prefBackupDir);
                File dbFolder = context.getDatabasePath("fsf.db").getParentFile();
                File prefFolder = new File(context.getFilesDir().getParent(), "shared_prefs");
                int count = 0;
                Logger.d("DBFolder: " + dbFolder.getAbsolutePath());
                Logger.d("PrefFolder: " + prefFolder.getAbsolutePath());
                List<File> dbs = Files.copyDatabases(dbFolder, backupFolder, now.getTime());
                File pref = Files.copyPrefFile(prefFolder, backupFolder, now.getTime());
                count = dbs.size() + (pref == null ? 0 : 1);
                if (count > 0) {
                    ctxs.setLastBackup(context, now.getTime());
                }
                Files.removeOldBackups(backupFolder, now);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
