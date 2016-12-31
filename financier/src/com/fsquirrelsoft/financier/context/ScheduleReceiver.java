package com.fsquirrelsoft.financier.context;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fsquirrelsoft.commons.util.Files;
import com.fsquirrelsoft.financier.ui.Constants;

import java.io.IOException;
import java.util.Calendar;

public class ScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar now = Calendar.getInstance();
        if (Constants.BACKUP_JOB.equals(intent.getAction())) {
            Contexts ctxs = Contexts.instance();
            try {
                int count = 0;
                count += Files.copyDatabases(ctxs.getDbFolder(), ctxs.getSdFolder(), now.getTime());
                count += Files.copyPrefFile(ctxs.getPrefFolder(), ctxs.getSdFolder(), now.getTime());
                if (count > 0) {
                    ctxs.setLastBackup(context, now.getTime());
                }
                Files.removeOldBackups(ctxs.getSdFolder(), now);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
