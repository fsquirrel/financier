package com.fsquirrelsoft.financier.data;

/**
 * @author dennis
 */
public class DataMeta {

    public static final String TB_ACC = "fsf_acc";
    public static final String COL_ACC_ID = "id_";
    public static final String COL_ACC_NAME = "nm_";
    public static final String COL_ACC_TYPE = "tp_";
    public static final String COL_ACC_CASHACCOUNT = "ca_";
    public static final String COL_ACC_INITVAL = "iv_";
    public static final String COL_ACC_INITVAL_BD_ = "ivb_";
    public static final String[] COL_ACC_ALL = new String[]{COL_ACC_ID, COL_ACC_NAME, COL_ACC_TYPE, COL_ACC_CASHACCOUNT, COL_ACC_INITVAL, COL_ACC_INITVAL_BD_};

    public static final String TB_DET = "fsf_det";
    public static final String COL_DET_ID = "id_";
    public static final String COL_DET_FROM = "fr_";
    public static final String COL_DET_TO = "to_";
    public static final String COL_DET_FROM_TYPE = "frt_";
    public static final String COL_DET_TO_TYPE = "tot_";
    public static final String COL_DET_DATE = "dt_";
    public static final String COL_DET_MONEY = "mn_";
    public static final String COL_DET_MONEY_BD_ = "mnb_";
    public static final String COL_DET_NOTE = "nt_";
    public static final String COL_DET_ARCHIVED = "ar_";
    public static final String[] COL_DET_ALL = new String[]{COL_DET_ID, COL_DET_FROM, COL_DET_FROM_TYPE, COL_DET_TO, COL_DET_TO_TYPE, COL_DET_DATE, COL_DET_MONEY, COL_DET_NOTE, COL_DET_ARCHIVED,
            COL_DET_MONEY_BD_};

    public static final String TB_TAG = "fsf_tag";
    public static final String COL_TAG_ID = "id_";
    public static final String COL_TAG_NAME = "nm_";
    public static final String[] COL_TAG_ALL = new String[]{COL_TAG_ID, COL_TAG_NAME};

    public static final String TB_DETTAG = "fsf_dettag";
    public static final String COL_DETTAG_ID = "id_";
    public static final String COL_DETTAG_DET_ID = "detid_";
    public static final String COL_DETTAG_TAG_ID = "tagid_";
    public static final String[] COL_DETTAG_ALL = new String[]{COL_DETTAG_ID, COL_DETTAG_DET_ID, COL_DETTAG_TAG_ID};

}
