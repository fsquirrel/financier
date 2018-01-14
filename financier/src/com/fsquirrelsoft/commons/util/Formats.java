package com.fsquirrelsoft.commons.util;

import com.fsquirrelsoft.financier.data.SymbolPosition;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author dennis
 */
public class Formats {

//    private static NumberFormat decimalFormat = new DecimalFormat("#0.###", DecimalFormatSymbols.getInstance());
//    private static NumberFormat moneyFormat = new DecimalFormat("###,###,###,##0.###", DecimalFormatSymbols.getInstance());

    /**
     * format should not be changed if i start a export/import function
     **/
    private static DateFormat norDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static DateFormat norDateFormatOld = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat norDatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static DateFormat norDatetimeFormatOld = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    private static DecimalFormat norDoubleFormat = new DecimalFormat("#0.###", DecimalFormatSymbols.getInstance());

    public static String double2String(Double d) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern("#0.###");
        return d == null ? df.format(0D) : df.format(d);
    }

    public static double string2Double(String d) {
        try {
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern("#0.###");
            return df.parse(d).doubleValue();
        } catch (ParseException e) {
            Logger.e(e.getMessage(), e);
            return 0D;
        }
    }

//    public static double normalizeString2Double(String d) throws ParseException {
//        return norDoubleFormat.parse(d).doubleValue();
//    }

    public static String normalizeDate2String(Date date) {
        return norDateFormat.format(date);
    }

    public static Date normalizeString2Date(String date) throws ParseException {
        try {
            return norDateFormat.parse(date);
        } catch (ParseException x) {
            return norDateFormatOld.parse(date);
        }
    }

    public static String normalizeDatetime2String(Date date) {
        return norDatetimeFormat.format(date);
    }

    public static Date normalizeString2Datetime(String date) throws ParseException {
        try {
            return norDatetimeFormat.parse(date);
        } catch (ParseException x) {
            return norDatetimeFormatOld.parse(date);
        }
    }

    public static String money2String(double money, String symbol, SymbolPosition pos) {
        StringBuilder sb = new StringBuilder();
        if (SymbolPosition.FRONT.equals(pos) && symbol != null) {
            sb.append(symbol);
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern("###,###,###,##0.###");
        sb.append(df.format(money));
        if (SymbolPosition.AFTER.equals(pos) && symbol != null) {
            sb.append(symbol);
        }
        return sb.toString();
    }

    public static String int2String(int d) {
        return DecimalFormat.getIntegerInstance().format(d);
    }

    public static int string2Int(String d) {
        try {
            return DecimalFormat.getInstance().parse(d).intValue();
        } catch (ParseException e) {
            Logger.e(e.getMessage(), e);
            return 0;
        }
    }

}
