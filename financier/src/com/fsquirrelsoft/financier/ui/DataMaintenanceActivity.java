package com.fsquirrelsoft.financier.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.fsquirrelsoft.commons.util.Files;
import com.fsquirrelsoft.commons.util.Formats;
import com.fsquirrelsoft.commons.util.GUIs;
import com.fsquirrelsoft.commons.util.Logger;
import com.fsquirrelsoft.financier.context.Contexts;
import com.fsquirrelsoft.financier.context.ContextsActivity;
import com.fsquirrelsoft.financier.core.R;
import com.fsquirrelsoft.financier.data.Account;
import com.fsquirrelsoft.financier.data.DataCreator;
import com.fsquirrelsoft.financier.data.Detail;
import com.fsquirrelsoft.financier.data.DetailTag;
import com.fsquirrelsoft.financier.data.IDataProvider;
import com.fsquirrelsoft.financier.data.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author dennis
 */
public class DataMaintenanceActivity extends ContextsActivity implements OnClickListener {

    String csvEncoding;

    String workingFolder;

    boolean backupcsv = false;

    static final String APPVER = "appver:";

    DateFormat backupformat = new SimpleDateFormat("yyyyMMdd-HHmmss");

    int vercode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datamain);
        workingFolder = getContexts().getWorkingFolder();
        backupcsv = getContexts().isPrefBackupCSV();

        vercode = getContexts().getApplicationVersionCode();
        csvEncoding = getContexts().getPrefCSVEncoding();
        initialListener();

    }

    private void initialListener() {
        findViewById(R.id.datamain_import_csv).setOnClickListener(this);
        findViewById(R.id.datamain_export_csv).setOnClickListener(this);
        findViewById(R.id.datamain_share_csv).setOnClickListener(this);
        findViewById(R.id.datamain_reset).setOnClickListener(this);
        findViewById(R.id.datamain_create_default).setOnClickListener(this);
        findViewById(R.id.datamain_clear_folder).setOnClickListener(this);
        findViewById(R.id.datamain_backup_db).setOnClickListener(this);
        findViewById(R.id.datamain_restore_db).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.datamain_import_csv) {
            doImportCSV();
        } else if (v.getId() == R.id.datamain_export_csv) {
            doExportCSV();
        } else if (v.getId() == R.id.datamain_share_csv) {
            doShareCSV();
        } else if (v.getId() == R.id.datamain_reset) {
            doReset();
        } else if (v.getId() == R.id.datamain_create_default) {
            doCreateDefault();
        } else if (v.getId() == R.id.datamain_clear_folder) {
            doClearFolder();
        } else if (v.getId() == R.id.datamain_backup_db) {
            doBackupDb();
        } else if (v.getId() == R.id.datamain_restore_db) {
            doRestoreDb();
        }
    }

    private void doRestoreDb() {
        // restore db & pref
        final Contexts ctxs = Contexts.instance();
        final GUIs.IBusyRunnable restoreJob = new GUIs.BusyAdapter() {
            @Override
            public void onBusyFinish() {
                GUIs.longToast(DataMaintenanceActivity.this, i18n.string(R.string.msg_db_retored));

                // push a dummy to trigger resume/reload
                Intent intent = new Intent(DataMaintenanceActivity.this, DummyActivity.class);
                startActivity(intent);
            }

            @Override
            public void run() {
                try {
                    Files.copyDatabases(ctxs.getBackupFolder(), ctxs.getDbFolder(), null);
                    Files.copyPrefFile(ctxs.getBackupFolder(), ctxs.getPrefFolder(), null);
                    Contexts.instance().reloadPreference();
                } catch (IOException e) {
                    Logger.e(e.getMessage(), e);
                }
            }
        };
        GUIs.confirm(this, i18n.string(R.string.qmsg_retore_db), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(Object data) {
                if ((Integer) data == GUIs.OK_BUTTON) {
                    GUIs.doBusy(DataMaintenanceActivity.this, restoreJob);
                } else {
                    IDataProvider idp = getContexts().getDataProvider();
                    if (idp.listAccount(null).size() == 0) {
                        // cause of this function is not ready in previous version, so i check the size for old user
                        new DataCreator(idp, i18n).createDefaultAccount();
                    }
                    GUIs.longToast(DataMaintenanceActivity.this, R.string.msg_firsttime_use_hint);
                }
                return true;
            }
        });
    }

    private void doBackupDb() {
        final Calendar now = Calendar.getInstance();
        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
            int count = -1;

            public void onBusyError(Throwable t) {
                GUIs.error(DataMaintenanceActivity.this, t);
            }

            public void onBusyFinish() {
                if (count > 0) {
                    String msg = i18n.string(R.string.msg_db_backuped, Integer.toString(count), workingFolder);
                    GUIs.alert(DataMaintenanceActivity.this, msg);
                    getContexts().setLastBackup(now.getTime());
                } else {
                    GUIs.alert(DataMaintenanceActivity.this, R.string.msg_no_db);
                }
            }

            @Override
            public void run() {
                try {
                    Contexts ctxs = getContexts();
                    count = Files.copyDatabases(ctxs.getDbFolder(), ctxs.getBackupFolder(), now.getTime());
                    count += Files.copyPrefFile(ctxs.getPrefFolder(), ctxs.getBackupFolder(), now.getTime());
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };
        GUIs.doBusy(DataMaintenanceActivity.this, job);
    }

    private void doClearFolder() {
        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
            @Override
            public void onBusyFinish() {
                GUIs.alert(DataMaintenanceActivity.this, i18n.string(R.string.msg_folder_cleared, workingFolder));
            }

            @Override
            public void run() {
                File sd = Environment.getExternalStorageDirectory();
                File folder = new File(sd, workingFolder);
                if (!folder.exists()) {
                    return;
                }
                for (File f : folder.listFiles()) {
                    String fnm = f.getName().toLowerCase();
                    if (f.isFile() && (fnm.endsWith(".csv") || fnm.endsWith(".bak"))) {
                        f.delete();
                    }
                }
            }
        };

        GUIs.confirm(this, i18n.string(R.string.qmsg_clear_folder, workingFolder), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
                    GUIs.doBusy(DataMaintenanceActivity.this, job);
                }
                return true;
            }
        });

    }

    private void doCreateDefault() {

        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
            @Override
            public void onBusyFinish() {
                GUIs.alert(DataMaintenanceActivity.this, R.string.msg_default_created);
            }

            @Override
            public void run() {
                IDataProvider idp = getContexts().getDataProvider();
                new DataCreator(idp, i18n).createDefaultAccount();
            }
        };

        GUIs.confirm(this, i18n.string(R.string.qmsg_create_default), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
                    GUIs.doBusy(DataMaintenanceActivity.this, job);
                }
                return true;
            }
        });
    }

    private void doReset() {

        new AlertDialog.Builder(this).setTitle(i18n.string(R.string.qmsg_reset)).setItems(R.array.csv_type_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
                    public void onBusyError(Throwable t) {
                        GUIs.error(DataMaintenanceActivity.this, t);
                    }

                    @Override
                    public void run() {
                        try {
                            _resetDate(which);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
                };
                GUIs.doBusy(DataMaintenanceActivity.this, job);
            }
        }).show();
    }

    private void doExportCSV() {
        final int workingBookId = getContexts().getWorkingBookId();
        new AlertDialog.Builder(this).setTitle(i18n.string(R.string.qmsg_export_csv)).setItems(R.array.csv_type_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
                    int count = -1;

                    public void onBusyError(Throwable t) {
                        GUIs.error(DataMaintenanceActivity.this, t);
                    }

                    public void onBusyFinish() {
                        if (count >= 0) {
                            String msg = i18n.string(R.string.msg_csv_exported, Integer.toString(count), workingFolder);
                            GUIs.alert(DataMaintenanceActivity.this, msg);
                        } else {
                            GUIs.alert(DataMaintenanceActivity.this, R.string.msg_no_csv);
                        }
                    }

                    @Override
                    public void run() {
                        try {
                            count = _exportToCSV(which, workingBookId);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
                };
                GUIs.doBusy(DataMaintenanceActivity.this, job);
            }
        }).show();
    }

    private void doImportCSV() {
        final int workingBookId = getContexts().getWorkingBookId();
        new AlertDialog.Builder(this).setTitle(i18n.string(R.string.qmsg_import_csv)).setItems(R.array.csv_type_import_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
                    int count = -1;

                    public void onBusyError(Throwable t) {
                        GUIs.error(DataMaintenanceActivity.this, t);
                    }

                    public void onBusyFinish() {
                        if (count >= 0) {
                            String msg = i18n.string(R.string.msg_csv_imported, Integer.toString(count), workingFolder);
                            GUIs.alert(DataMaintenanceActivity.this, msg);
                        } else {
                            GUIs.alert(DataMaintenanceActivity.this, R.string.msg_no_csv);
                        }
                    }

                    @Override
                    public void run() {
                        try {
                            count = _importFromCSV(which, workingBookId);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
                };
                GUIs.doBusy(DataMaintenanceActivity.this, job);
            }
        }).show();
    }

    private void doShareCSV() {
        new AlertDialog.Builder(this).setTitle(i18n.string(R.string.qmsg_share_csv)).setItems(R.array.csv_type_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
                    int count = -1;

                    public void onBusyError(Throwable t) {
                        GUIs.error(DataMaintenanceActivity.this, t);
                    }

                    public void onBusyFinish() {
                        if (count < 0) {
                            GUIs.alert(DataMaintenanceActivity.this, R.string.msg_no_csv);
                        }
                    }

                    @Override
                    public void run() {
                        try {
                            count = _shareCSV(which);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
                };
                GUIs.doBusy(DataMaintenanceActivity.this, job);
            }
        }).show();
    }

    private File getWorkingFile(String name) throws IOException {
        File sd = Environment.getExternalStorageDirectory();
        File folder = new File(sd, workingFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(folder, name);
        return file;
    }

    private void _resetDate(int mode) {
        if (Contexts.DEBUG) {
            Logger.d("reset date" + mode);
        }
        boolean account = false;
        boolean detail = false;
        boolean tag = false;
        boolean detailTag = false;
        switch (mode) {
            case 0:
                account = detail = tag = detailTag = true;
                break;
            case 1:
                account = true;
                break;
            case 2:
                detail = detailTag = true;
                break;
            case 3:
                tag = true;
                break;
        }
        IDataProvider idp = getContexts().getDataProvider();
        if (account && detail && tag && detailTag) {
            idp.reset();
        } else if (account) {
            idp.deleteAllAccount();
        } else if (detail && detailTag) {
            idp.deleteAllDetail();
            idp.deleteAllDetailTag();
        } else if (tag) {
            idp.deleteAllTag();
        }

    }

    /**
     * running in thread
     **/
    private int _exportToCSV(int mode, int workingBookId) throws IOException {
        if (Contexts.DEBUG) {
            Logger.d("export to csv " + mode);
        }
        boolean account = false;
        boolean detail = false;
        boolean detailTag = false;
        boolean tag = false;
        switch (mode) {
            case 0:
                account = detail = detailTag = tag = true;
                break;
            case 1:
                account = true;
                break;
            case 2:
                detail = detailTag = true;
                break;
            case 3:
                tag = true;
                break;
            default:
                return -1;
        }
        IDataProvider idp = getContexts().getDataProvider();
        StringWriter sw;
        CsvWriter csvw;
        int count = 0;
        String backupstamp = backupformat.format(new Date());
        if (detail) {
            sw = new StringWriter();
            csvw = new CsvWriter(sw, ',');
            csvw.writeRecord(new String[]{"id", "from", "to", "date", "value", "note", "archived", APPVER + vercode});
            for (Detail d : idp.listAllDetail()) {
                count++;
                csvw.writeRecord(new String[]{Integer.toString(d.getId()), d.getFrom(), d.getTo(), Formats.normalizeDate2String(d.getDate()), Formats.normalizeBigDecimal2String(d.getMoneyBD()),
                        d.getNote(), d.isArchived() ? "1" : "0"});
            }
            csvw.close();
            String csv = sw.toString();
            File file0 = getWorkingFile("details.csv");
            File file1 = getWorkingFile("details-" + workingBookId + ".csv");

            saveFile(file0, csv, backupstamp);
            saveFile(file1, csv, backupstamp);
        }

        if (account) {
            sw = new StringWriter();
            csvw = new CsvWriter(sw, ',');
            csvw.writeRecord(new String[]{"id", "type", "name", "init", "cash", APPVER + vercode});
            for (Account a : idp.listAccount(null)) {
                count++;
                csvw.writeRecord(new String[]{a.getId(), a.getType(), a.getName(), Formats.normalizeBigDecimal2String(a.getInitialValueBD()), a.isCashAccount() ? "1" : "0"});
            }
            csvw.close();
            String csv = sw.toString();
            File file0 = getWorkingFile("accounts.csv");
            File file1 = getWorkingFile("accounts-" + workingBookId + ".csv");

            saveFile(file0, csv, backupstamp);
            saveFile(file1, csv, backupstamp);
        }

        if (detailTag) {
            sw = new StringWriter();
            csvw = new CsvWriter(sw, ',');
            csvw.writeRecord(new String[]{"id", "detailId", "tagId", APPVER + vercode});
            for (DetailTag a : idp.listAllDetailTags()) {
                count++;
                csvw.writeRecord(new String[]{String.valueOf(a.getId()), String.valueOf(a.getDetailId()), String.valueOf(a.getTagId())});
            }
            csvw.close();
            String csv = sw.toString();
            File file0 = getWorkingFile("dettags.csv");
            File file1 = getWorkingFile("dettags-" + workingBookId + ".csv");

            saveFile(file0, csv, backupstamp);
            saveFile(file1, csv, backupstamp);
        }

        if (tag) {
            sw = new StringWriter();
            csvw = new CsvWriter(sw, ',');
            csvw.writeRecord(new String[]{"id", "name", APPVER + vercode});
            for (Tag a : idp.listAllTags()) {
                count++;
                csvw.writeRecord(new String[]{String.valueOf(a.getId()), a.getName()});
            }
            csvw.close();
            String csv = sw.toString();
            File file0 = getWorkingFile("tags.csv");
            File file1 = getWorkingFile("tags-" + workingBookId + ".csv");

            saveFile(file0, csv, backupstamp);
            saveFile(file1, csv, backupstamp);
        }

        return count;
    }

    private void saveFile(File file0, String csv, String backupstamp) throws IOException {
        if (file0.exists()) {
            if (backupcsv) {
                String fn = file0.getName();
                String ext = Files.getExtension(fn);
                String main = Files.getMain(fn);
                Files.copyFileTo(file0, new File(file0.getParentFile(), main + "." + backupstamp + "." + ext));
            }
        } else {
            file0.createNewFile();
        }

        Files.saveString(csv, file0, csvEncoding);
        if (Contexts.DEBUG) {
            Logger.d("export to " + file0.toString());
        }
    }

    private int getAppver(String str) {
        if (str != null && str.startsWith(APPVER)) {
            try {
                return Integer.parseInt(str.substring(APPVER.length()));
            } catch (Exception x) {
                if (Contexts.DEBUG) {
                    Logger.d(x.getMessage());
                }
            }
        }
        return 0;
    }

    /**
     * running in thread
     *
     * @param workingBookId
     **/
    private int _importFromCSV(int mode, int workingBookId) throws Exception {
        if (Contexts.DEBUG) {
            Logger.d("import from csv " + mode);
        }
        boolean account = false;
        boolean detail = false;
        boolean tag = false;
        boolean detailTag = false;
        boolean shared = mode >= 4;
        if (shared)
            mode = mode - 4;
        switch (mode) {
            case 0:
                account = detail = detailTag = tag = true;
                break;
            case 1:
                account = true;
                break;
            case 2:
                detail = detailTag = true;
                break;
            case 3:
                tag = true;
                break;
            default:
                return -1;
        }

        IDataProvider idp = getContexts().getDataProvider();
        File details = getWorkingFile(shared ? "details.csv" : "details-" + workingBookId + ".csv");
        File accounts = getWorkingFile(shared ? "accounts.csv" : "accounts-" + workingBookId + ".csv");
        File tags = getWorkingFile(shared ? "tags.csv" : "tags-" + workingBookId + ".csv");
        File detailTags = getWorkingFile(shared ? "dettags.csv" : "dettags-" + workingBookId + ".csv");

        if ((detail && (!details.exists() || !details.canRead())) || (account && (!accounts.exists() || !accounts.canRead())) || (tag && (!tags.exists() || !tags.canRead()))
                || (detailTag && (!detailTags.exists() || !detailTags.canRead()))) {
            return -1;
        }

        CsvReader accountReader = null;
        CsvReader detailReader = null;
        CsvReader detailTagReader = null;
        CsvReader tagReader = null;
        try {
            int count = 0;
            if (account) {
                accountReader = new CsvReader(new InputStreamReader(new FileInputStream(accounts), csvEncoding));
            }
            if (detail) {
                detailReader = new CsvReader(new InputStreamReader(new FileInputStream(details), csvEncoding));
            }

            if (detailTag) {
                detailTagReader = new CsvReader(new InputStreamReader(new FileInputStream(detailTags), csvEncoding));
            }

            if (tag) {
                tagReader = new CsvReader(new InputStreamReader(new FileInputStream(tags), csvEncoding));
            }

            if ((accountReader != null && !accountReader.readHeaders())) {
                return -1;
            }

            // don't combine with account checker
            if ((detailReader != null && !detailReader.readHeaders())) {
                return -1;
            }

            if ((detailTagReader != null && !detailTagReader.readHeaders())) {
                return -1;
            }

            if ((tagReader != null && !tagReader.readHeaders())) {
                return -1;
            }

            if (detail) {
                detailReader.setTrimWhitespace(true);
                int appver = getAppver(detailReader.getHeaders()[detailReader.getHeaderCount() - 1]);

                idp.deleteAllDetail();
                while (detailReader.readRecord()) {
                    Detail det = new Detail(detailReader.get("from"), detailReader.get("to"), Formats.normalizeString2Date(detailReader.get("date")), new BigDecimal(detailReader.get("value")),
                            detailReader.get("note"));
                    String archived = detailReader.get("archived");
                    if ("1".equals(archived)) {
                        det.setArchived(true);
                    } else if ("0".equals(archived)) {
                        det.setArchived(false);
                    } else {
                        det.setArchived(Boolean.parseBoolean(archived));
                    }

                    idp.newDetailNoCheck(Integer.parseInt(detailReader.get("id")), det);
                    count++;
                }
                detailReader.close();
                detailReader = null;
                if (Contexts.DEBUG) {
                    Logger.d("import from " + details + " ver:" + appver);
                }
            }

            if (account) {
                accountReader.setTrimWhitespace(true);
                int appver = getAppver(accountReader.getHeaders()[accountReader.getHeaderCount() - 1]);
                idp.deleteAllAccount();
                while (accountReader.readRecord()) {
                    Account acc = new Account(accountReader.get("type"), accountReader.get("name"), new BigDecimal(accountReader.get("init")));
                    String cash = accountReader.get("cash");
                    acc.setCashAccount("1".equals(cash) ? true : false);

                    idp.newAccountNoCheck(accountReader.get("id"), acc);
                    count++;
                }
                accountReader.close();
                accountReader = null;
                if (Contexts.DEBUG) {
                    Logger.d("import from " + accounts + " ver:" + appver);
                }
            }

            if (detailTag) {
                detailTagReader.setTrimWhitespace(true);
                int appver = getAppver(detailTagReader.getHeaders()[detailTagReader.getHeaderCount() - 1]);
                idp.deleteAllDetailTag();
                while (detailTagReader.readRecord()) {
                    DetailTag detTag = new DetailTag(Integer.parseInt(detailTagReader.get("detailId")), Integer.parseInt(detailTagReader.get("tagId")));

                    idp.newDetailTagNoCheck(Integer.parseInt(detailTagReader.get("id")), detTag);
                    count++;
                }
                detailTagReader.close();
                detailTagReader = null;
                if (Contexts.DEBUG) {
                    Logger.d("import from " + detailTags + " ver:" + appver);
                }
            }

            if (tag) {
                tagReader.setTrimWhitespace(true);
                int appver = getAppver(tagReader.getHeaders()[tagReader.getHeaderCount() - 1]);
                idp.deleteAllTag();
                while (tagReader.readRecord()) {
                    Tag acc = new Tag(tagReader.get("name"));

                    idp.newTagNoCheck(Integer.parseInt(tagReader.get("id")), acc);
                    count++;
                }
                tagReader.close();
                tagReader = null;
                if (Contexts.DEBUG) {
                    Logger.d("import from " + tags + " ver:" + appver);
                }
            }

            return count;
        } finally {
            if (accountReader != null) {
                accountReader.close();
            }
            if (detailReader != null) {
                detailReader.close();
            }
            if (detailTagReader != null) {
                detailTagReader.close();
            }
            if (tagReader != null) {
                tagReader.close();
            }
        }
    }

    /**
     * running in thread
     **/
    private int _shareCSV(int mode) throws Exception {
        if (Contexts.DEBUG) {
            Logger.d("share csv " + mode);
        }
        boolean account = false;
        boolean detail = false;
        boolean detailTag = false;
        boolean tag = false;
        switch (mode) {
            case 0:
                account = detail = detailTag = tag = true;
                break;
            case 1:
                account = true;
                break;
            case 2:
                detail = detailTag = true;
                break;
            case 3:
                tag = true;
                break;
            default:
                return -1;
        }

        File details = getWorkingFile("details.csv");
        File accounts = getWorkingFile("accounts.csv");
        File detailTags = getWorkingFile("dettags.csv");
        File tags = getWorkingFile("tags.csv");

        if ((detail && (!details.exists() || !details.canRead())) || (account && (!accounts.exists() || !accounts.canRead())) || (tag && (!tags.exists() || !tags.canRead()))
                || (detailTag && (!detailTags.exists() || !detailTags.canRead()))) {
            return -1;
        }

        int count = 0;

        List<File> files = new ArrayList<File>();

        if (detail) {
            files.add(details);
            count++;
        }

        if (account) {
            files.add(accounts);
            count++;
        }

        if (detailTag) {
            files.add(detailTags);
            count++;
        }

        if (tag) {
            files.add(tags);
            count++;
        }

        if (count > 0) {
            DateFormat df = getContexts().getDateFormat();
            getContexts().shareTextContent(i18n.string(R.string.msg_share_csv_title, df.format(new Date())), i18n.string(R.string.msg_share_csv_content), files);
        }
        return count;

    }
}
