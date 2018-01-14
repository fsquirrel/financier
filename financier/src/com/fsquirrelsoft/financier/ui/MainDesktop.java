package com.fsquirrelsoft.financier.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.fsquirrelsoft.financier.context.Contexts;
import com.fsquirrelsoft.financier.core.R;
import com.fsquirrelsoft.financier.data.Detail;

import java.util.Date;

/**
 * @author dennis
 */
public class MainDesktop extends AbstractDesktop {
    private Resources resources;

    public MainDesktop() {
        super();
        resources = Contexts.instance().getResources();
        label = resources.getString(R.string.dt_main);
        icon = -1;
    }

    @Override
    protected void init(final Context context) {
        DesktopItem adddetdt = new DesktopItem(new Runnable() {
            public void run() {
                Detail d = new Detail("", "", new Date(), 0D, "");
                Intent intent = null;
                intent = new Intent(context, DetailEditorActivity.class);
                intent.putExtra(DetailEditorActivity.INTENT_MODE_CREATE, true);
                intent.putExtra(DetailEditorActivity.INTENT_DETAIL, d);
                MainDesktop.this.getActivity().startActivityForResult(intent, Constants.REQUEST_DETAIL_EDITOR_CODE);
            }
        }, resources.getString(R.string.dtitem_adddetail), R.drawable.dtitem_adddetail, 999);

        Intent intent = new Intent(context, DetailListActivity.class);
        intent.putExtra(DetailListActivity.INTENT_MODE, DetailListActivity.MODE_DAY);
        String title = resources.getString(R.string.dtitem_detlist);
        intent.putExtra(DetailListActivity.INTENT_MODE_TITLE, title);
        DesktopItem daylist = new DesktopItem(new IntentRun(context, intent), title, R.drawable.dtitem_detail_day);

        DesktopItem accmgntdt = new DesktopItem(new ActivityRun(context, AccountMgntActivity.class), resources.getString(R.string.dtitem_accmgnt), R.drawable.dtitem_account);

        DesktopItem bookmgntdt = new DesktopItem(new ActivityRun(context, BookMgntActivity.class), resources.getString(R.string.dtitem_books), R.drawable.dtitem_books);

        DesktopItem datamaindt = new DesktopItem(new ActivityRun(context, DataMaintenanceActivity.class), resources.getString(R.string.dtitem_datamain), R.drawable.dtitem_datamain);

        DesktopItem prefdt = new DesktopItem(new ActivityRun(context, PrefsActivity.class), resources.getString(R.string.dtitem_prefs), R.drawable.dtitem_prefs);

        DesktopItem tagmgntdt = new DesktopItem(new ActivityRun(context, TagMgntActivity.class), resources.getString(R.string.dtitem_tags), R.drawable.dtitem_tags);

        intent = new Intent(context, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.INTENT_URI_ID, R.string.path_how2use);
        DesktopItem how2use = new DesktopItem(new IntentRun(context, intent), resources.getString(R.string.dtitem_how2use), -1, 0);
        how2use.setHidden(true);

        DesktopItem aboutdt = new DesktopItem(new ActivityRun(context, AboutActivity.class), resources.getString(R.string.dtitem_about), -1, 0);
        aboutdt.setHidden(true);

        addItem(adddetdt);
        addItem(daylist);
        addItem(accmgntdt);
        addItem(datamaindt);
        addItem(prefdt);
        addItem(bookmgntdt);
        addItem(tagmgntdt);

        addItem(how2use);

        addItem(aboutdt);
    }

}
