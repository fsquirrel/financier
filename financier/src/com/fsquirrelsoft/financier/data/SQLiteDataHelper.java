package com.fsquirrelsoft.financier.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fsquirrelsoft.commons.util.Logger;
import com.fsquirrelsoft.financier.context.Contexts;

import static com.fsquirrelsoft.financier.data.DataMeta.COL_ACC_CASHACCOUNT;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_ACC_ID;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_ACC_INITVAL;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_ACC_INITVAL_BD_;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_ACC_NAME;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_ACC_TYPE;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DETTAG_DET_ID;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DETTAG_ID;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DETTAG_TAG_ID;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_ARCHIVED;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_DATE;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_FROM;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_FROM_TYPE;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_ID;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_MONEY;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_MONEY_BD_;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_NOTE;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_TO;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_DET_TO_TYPE;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_TAG_ID;
import static com.fsquirrelsoft.financier.data.DataMeta.COL_TAG_NAME;
import static com.fsquirrelsoft.financier.data.DataMeta.TB_ACC;
import static com.fsquirrelsoft.financier.data.DataMeta.TB_DET;
import static com.fsquirrelsoft.financier.data.DataMeta.TB_DETTAG;
import static com.fsquirrelsoft.financier.data.DataMeta.TB_TAG;

/**
 * @author dennis
 */
public class SQLiteDataHelper extends SQLiteOpenHelper {
    /**
     * maintain this field carefully
     */
    // private static final int VERSION = 4;//0.9.1-0.9.3
    // private static final int VERSION = 5;// 0.9.4-0.9.5
    // private static final int VERSION = 6;// change double to BigDecimal
    private static final int VERSION = 7;// for tag

    private static final String ACC_CREATE_SQL = "CREATE TABLE " + TB_ACC + " (" + COL_ACC_ID + " TEXT PRIMARY KEY, " + COL_ACC_NAME + " TEXT NOT NULL, " + COL_ACC_TYPE + " TEXT NOT NULL, "
            + COL_ACC_CASHACCOUNT + " INTEGER NULL, " + COL_ACC_INITVAL + " REAL NOT NULL, " + COL_ACC_INITVAL_BD_ + " TEXT NOT NULL DEFAULT '' )";
    private static final String ACC_DROP_SQL = "DROP TABLE IF EXISTS " + TB_ACC;

    private static final String DET_CREATE_SQL = "CREATE TABLE " + TB_DET + " (" + COL_DET_ID + " INTEGER PRIMARY KEY, " + COL_DET_FROM + " TEXT NOT NULL, " + COL_DET_FROM_TYPE + " TEXT NOT NULL, "
            + COL_DET_TO + " TEXT NOT NULL, " + COL_DET_TO_TYPE + " TEXT NOT NULL, " + COL_DET_DATE + " INTEGER NOT NULL, " + COL_DET_MONEY + " REAL NOT NULL, " + COL_DET_ARCHIVED
            + " INTEGER NOT NULL, " + COL_DET_NOTE + " TEXT, " + COL_DET_MONEY_BD_ + " TEXT NOT NULL DEFAULT '' )";

    private static final String DET_DROP_SQL = "DROP TABLE IF EXISTS " + TB_DET;

    private static final String TAG_CREATE_SQL = "CREATE TABLE " + TB_TAG + " (" + COL_TAG_ID + " INTEGER PRIMARY KEY, " + COL_TAG_NAME + " TEXT NOT NULL)";

    private static final String TAG_DROP_SQL = "DROP TABLE IF EXISTS " + TB_TAG;

    private static final String DETTAG_CREATE_SQL = "CREATE TABLE " + TB_DETTAG + " (" + COL_DETTAG_ID + " INTEGER PRIMARY KEY, " + COL_DETTAG_DET_ID + " INTEGER NOT NULL, " + COL_DETTAG_TAG_ID
            + " INTEGER NOT NULL)";

    private static final String DETTAG_DROP_SQL = "DROP TABLE IF EXISTS " + TB_DETTAG;

    public SQLiteDataHelper(Context context, String dbname) {
        super(context, dbname, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (Contexts.DEBUG) {
            Logger.d("create schema " + ACC_CREATE_SQL);
        }
        db.execSQL(ACC_CREATE_SQL);

        if (Contexts.DEBUG) {
            Logger.d("create schema " + DET_CREATE_SQL);
        }
        db.execSQL(DET_CREATE_SQL);

        if (Contexts.DEBUG) {
            Logger.d("create schema " + TAG_CREATE_SQL);
        }
        db.execSQL(TAG_CREATE_SQL);

        if (Contexts.DEBUG) {
            Logger.d("create schema " + DETTAG_CREATE_SQL);
        }
        db.execSQL(DETTAG_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (Contexts.DEBUG) {
            Logger.d("update db from " + oldVersion + " to " + newVersion);
        }
        renameDMTableIfNeed(db);
        if (oldVersion < 0) {
            Logger.i("reset schema");
            // drop and create.
            Logger.i("drop schema " + ACC_DROP_SQL);
            db.execSQL(ACC_DROP_SQL);
            Logger.i("drop schema " + DET_DROP_SQL);
            db.execSQL(DET_DROP_SQL);
            Logger.i("drop schema " + TAG_DROP_SQL);
            db.execSQL(TAG_DROP_SQL);
            Logger.i("drop schema " + DETTAG_DROP_SQL);
            db.execSQL(DETTAG_DROP_SQL);
            onCreate(db);
            return;
        }
        if (oldVersion == 4) {// schema before 0.9.4
            // upgrade to 0.9.4
            Logger.i("upgrade schem from " + oldVersion + " to " + newVersion);
            db.execSQL("ALTER TABLE " + TB_ACC + " ADD " + COL_ACC_CASHACCOUNT + " INTEGER NULL ");
            oldVersion++;
        }

        // keep going check next id
        if (oldVersion == 5) {// schema before ?
            // upgrade to ?
            Logger.i("upgrade schem from " + oldVersion + " to " + newVersion);
            db.execSQL("ALTER TABLE " + TB_ACC + " ADD " + COL_ACC_INITVAL_BD_ + " TEXT NOT NULL DEFAULT '' ");
            db.execSQL("ALTER TABLE " + TB_DET + " ADD " + COL_DET_MONEY_BD_ + " TEXT NOT NULL DEFAULT '' ");
            db.execSQL("UPDATE " + TB_ACC + " SET " + COL_ACC_INITVAL_BD_ + " = " + COL_ACC_INITVAL);
            db.execSQL("UPDATE " + TB_DET + " SET " + COL_DET_MONEY_BD_ + " = " + COL_DET_MONEY);
            oldVersion++;
        }

        if (oldVersion == 6) {
            Logger.i("upgrade schem from " + oldVersion + " to " + newVersion);
            db.execSQL(TAG_CREATE_SQL);
            db.execSQL(DETTAG_CREATE_SQL);
            oldVersion++;
        }
    }

    private void renameDMTableIfNeed(SQLiteDatabase db) {
        if (tableExists(db, "dm_acc")) {
            db.execSQL("ALTER TABLE dm_acc RENAME TO " + TB_ACC);
        }
        if (tableExists(db, "dm_det")) {
            db.execSQL("ALTER TABLE dm_det RENAME TO " + TB_DET);
        }
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        if (tableName == null) {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

}
