package com.fsquirrelsoft.financier.ui;

import android.content.Context;
import android.content.Intent;

import com.fsquirrelsoft.financier.core.R;
import com.fsquirrelsoft.financier.ui.report.BalanceActivity;
import com.fsquirrelsoft.financier.ui.report.SearchActivity;
import com.fsquirrelsoft.financier.ui.report.TagActivity;

/**
 * 
 * @author dennis
 * 
 */
public class ReportsDesktop extends AbstractDesktop {

    public ReportsDesktop() {
        label = i18n.string(R.string.dt_reports);
        icon = -1;
    }

    @Override
    protected void init(Context context) {
        Intent intent;

        intent = new Intent(context, BalanceActivity.class);
        intent.putExtra(BalanceActivity.INTENT_TOTAL_MODE, false);
        intent.putExtra(BalanceActivity.INTENT_MODE, BalanceActivity.MODE_MONTH);
        DesktopItem monthBalance = new DesktopItem(new IntentRun(context, intent), i18n.string(R.string.dtitem_report_monthly_balance), R.drawable.dtitem_balance_month);
        addItem(monthBalance);

        intent = new Intent(context, BalanceActivity.class);
        intent.putExtra(BalanceActivity.INTENT_TOTAL_MODE, true);
        intent.putExtra(BalanceActivity.INTENT_MODE, BalanceActivity.MODE_MONTH);
        DesktopItem totalBalance = new DesktopItem(new IntentRun(context, intent), i18n.string(R.string.dtitem_report_cumulative_balance), R.drawable.dtitem_balance_cumulative_month, 99);
        addItem(totalBalance);

        intent = new Intent(context, TagActivity.class);
        intent.putExtra(TagActivity.INTENT_MODE, TagActivity.MODE_MONTH);
        DesktopItem tagBalance = new DesktopItem(new IntentRun(context, intent), i18n.string(R.string.dtitem_report_tag_cumulative_balance), R.drawable.dtitem_tag_month);
        addItem(tagBalance);

        intent = new Intent(context, SearchActivity.class);
        DesktopItem search = new DesktopItem(new IntentRun(context, intent), i18n.string(R.string.dtitem_report_search), R.drawable.dtitem_search, 99);
        addItem(search);
    }

}
