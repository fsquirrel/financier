package com.fsquirrelsoft.financier.ui.report;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.fsquirrelsoft.commons.util.CalendarHelper;
import com.fsquirrelsoft.commons.util.GUIs;
import com.fsquirrelsoft.financier.context.ContextsActivity;
import com.fsquirrelsoft.financier.core.R;
import com.fsquirrelsoft.financier.data.Balance;
import com.fsquirrelsoft.financier.data.BalanceHelper;
import com.fsquirrelsoft.financier.ui.AccountDetailListActivity;
import com.fsquirrelsoft.financier.ui.Constants;
import com.fsquirrelsoft.financier.ui.NamedItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dennis
 */
public class TagActivity extends ContextsActivity implements OnClickListener, OnItemClickListener {

    public static final int MODE_MONTH = 0;
    public static final int MODE_YEAR = 1;
    public static final int MODE_WEEK = 2;
    public static final int MODE_DAY = 3;

    public static final String INTENT_TAG_DATE = "tagDate";
    public static final String INTENT_MODE = "mode";
    public static final String INTENT_TARGET_DATE = "target";

    TextView infoView;
    View toolbarView;

    private Date targetDate;
    private Date currentDate;
    private int mode = MODE_MONTH;

    private DateFormat monthDayDateFormat;
    private DateFormat monthDateFormat;
    private DateFormat yearDateFormat;
    private DateFormat dateFormat;

    private Date currentStartDate;
    private Date currentEndDate;

    ImageButton modeBtn;

    private static String[] bindingFrom = new String[]{"layout", "name", "money"};

    private static int[] bindingTo = new int[]{R.id.report_tag_layout, R.id.report_tag_item_name, R.id.report_tag_item_money};

    private List<Balance> listViewData = new ArrayList<Balance>();

    private List<Map<String, Object>> listViewMapList = new ArrayList<Map<String, Object>>();

    private ListView listView;

    private SimpleAdapter listViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_tag);
        initialIntent();
        initialContent();
        GUIs.delayPost(new Runnable() {
            @Override
            public void run() {
                reloadData();
            }
        }, 25);
    }

    private void initialIntent() {
        Bundle b = getIntentExtras();
        mode = b.getInt(INTENT_MODE, MODE_MONTH);
        Object o = b.get(INTENT_TAG_DATE);
        if (o instanceof Date) {
            targetDate = (Date) o;
        } else {
            targetDate = new Date();
        }
        currentDate = targetDate;
    }

    private void initialContent() {
        monthDayDateFormat = new SimpleDateFormat("MM/dd");
        monthDateFormat = new SimpleDateFormat("yyyy/MM");
        yearDateFormat = new SimpleDateFormat("yyyy");
        dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        infoView = (TextView) findViewById(R.id.report_tag_infobar);
        toolbarView = findViewById(R.id.report_tag_toolbar);

        findViewById(R.id.report_tag_prev).setOnClickListener(this);
        findViewById(R.id.report_tag_next).setOnClickListener(this);
        findViewById(R.id.report_tag_today).setOnClickListener(this);
        modeBtn = (ImageButton) findViewById(R.id.report_tag_mode);
        modeBtn.setOnClickListener(this);
        reloadToolbar();

        listViewAdapter = new SimpleAdapter(this, listViewMapList, R.layout.report_tag_item, bindingFrom, bindingTo);
        listViewAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Object data, String text) {
                NamedItem item = (NamedItem) data;
                String name = item.getName();
                Balance b = (Balance) item.getValue();
                if ("layout".equals(name)) {
                    LinearLayout layout = (LinearLayout) view;
                    adjustLayout(layout, b);
                    return true;
                }
                // not textview, not initval
                if (!(view instanceof TextView)) {
                    return false;
                }
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(R.color.liability_fgd));
                adjustItem(tv, b, GUIs.getDPRatio(TagActivity.this));
                return false;
            }
        });

        listView = (ListView) findViewById(R.id.report_tag_list);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);
    }

    protected void adjustLayout(LinearLayout layout, Balance b) {
        layout.setBackgroundDrawable((getResources().getDrawable(R.drawable.selector_balance_indent)));
    }

    protected void adjustItem(TextView tv, Balance b, float dp) {
        float fontPixelSize = 18;
        float ratio = 0;
        int marginLeft = 0;
        int marginRight = 5;
        int paddingTB = 0;

        int indent = b.getIndent();
        ratio = 0.85F;
        paddingTB = 3;
        marginLeft = 5 + 10 * indent;

        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontPixelSize * ratio);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tv.getLayoutParams();
        lp.setMargins((int) (marginLeft * dp), lp.topMargin, (int) (marginRight * dp), lp.bottomMargin);
        tv.setPadding(tv.getPaddingLeft(), (int) (paddingTB * dp), tv.getPaddingRight(), (int) (paddingTB * dp));
    }

    private void reloadToolbar() {
        switch (mode) {
            case MODE_MONTH:
                modeBtn.setImageResource(R.drawable.btn_year);
                break;
            case MODE_YEAR:
                modeBtn.setImageResource(R.drawable.btn_day);
                break;
            case MODE_DAY:
                modeBtn.setImageResource(R.drawable.btn_week);
                break;
            case MODE_WEEK:
                modeBtn.setImageResource(R.drawable.btn_month);
                break;
        }
    }

    private void reloadData() {
        final CalendarHelper cal = getContexts().getCalendarHelper();
        currentEndDate = null;
        currentStartDate = null;
        infoView.setText("");
        reloadToolbar();
        switch (mode) {
            case MODE_YEAR:
                currentEndDate = cal.yearEndDate(currentDate);
                currentStartDate = cal.yearStartDate(currentDate);
                break;
            case MODE_MONTH:
                currentEndDate = cal.monthEndDate(currentDate);
                currentStartDate = cal.monthStartDate(currentDate);
                break;
            case MODE_WEEK:
                currentEndDate = cal.weekEndDate(currentDate);
                currentStartDate = cal.weekStartDate(currentDate);
                break;
            case MODE_DAY:
                currentEndDate = cal.toDayEnd(currentDate);
                currentStartDate = cal.toDayStart(currentDate);
                break;
        }
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            List<Balance> all = new ArrayList<Balance>();

            @Override
            public void run() {
                boolean hierarchical = getContexts().isPrefHierarachicalReport();

                List<Balance> tag = BalanceHelper.calculateBalanceListForTag(currentStartDate, currentEndDate);

                if (hierarchical) {
                    tag = BalanceHelper.adjustNestedTotalBalanceForTag(tag);
                } else {
                    tag = BalanceHelper.adjustTotalBalanceForTag(tag);
                }

                all.addAll(tag);
            }

            @Override
            public void onBusyFinish() {
                final CalendarHelper cal = getContexts().getCalendarHelper();
                listViewData.clear();
                listViewData.addAll(all);
                listViewMapList.clear();

                for (Balance b : listViewData) {
                    Map<String, Object> row = new HashMap<String, Object>();
                    listViewMapList.add(row);
                    String money = getContexts().toFormattedMoneyString(b.getMoney());
                    row.put(bindingFrom[0], new NamedItem(bindingFrom[0], b, ""));// layout
                    row.put(bindingFrom[1], new NamedItem(bindingFrom[1], b, b.getName()));
                    row.put(bindingFrom[2], new NamedItem(bindingFrom[2], b, money));
                }

                listViewAdapter.notifyDataSetChanged();

                // update info
                switch (mode) {
                    case MODE_YEAR:
                        infoView.setText(getResources().getString(R.string.label_balance_mode_year, yearDateFormat.format(currentDate)));
                        break;
                    case MODE_MONTH:
                        infoView.setText(getResources().getString(R.string.label_balance_mode_month, monthDateFormat.format(cal.monthStartDate(currentDate)), monthDayDateFormat.format(cal.monthStartDate(currentDate)),
                                monthDayDateFormat.format(cal.monthEndDate(currentDate))));
                        break;
                    case MODE_WEEK:
                        infoView.setText(getResources().getString(R.string.label_balance_mode_week, yearDateFormat.format(currentDate), cal.weekOfYear(currentDate),
                                monthDayDateFormat.format(cal.weekEndDate(currentDate)), monthDayDateFormat.format(cal.weekStartDate(currentDate))));
                        break;
                    case MODE_DAY:
                        infoView.setText(getResources().getString(R.string.label_balance_mode_day, dateFormat.format(currentDate)));
                        break;
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.report_tag_prev) {
            onPrev();
        } else if (v.getId() == R.id.report_tag_next) {
            onNext();
        } else if (v.getId() == R.id.report_tag_today) {
            onToday();
        } else if (v.getId() == R.id.report_tag_mode) {
            onMode();
        }
    }

    private void onMode() {
        switch (mode) {
            case MODE_MONTH:
                mode = MODE_YEAR;
                break;
            case MODE_YEAR:
                mode = MODE_DAY;
                break;
            case MODE_DAY:
                mode = MODE_WEEK;
                break;
            case MODE_WEEK:
                mode = MODE_MONTH;
                break;
        }
        reloadData();
    }

    private void onNext() {
        CalendarHelper cal = getContexts().getCalendarHelper();
        switch (mode) {
            case MODE_DAY:
                currentDate = cal.dateAfter(currentDate, 1);
                break;
            case MODE_WEEK:
                currentDate = cal.dateAfter(currentDate, 7);
                break;
            case MODE_MONTH:
                currentDate = cal.monthAfter(currentDate, 1);
                break;
            case MODE_YEAR:
                currentDate = cal.yearAfter(currentDate, 1);
                break;
        }
        reloadData();
    }

    private void onPrev() {
        CalendarHelper cal = getContexts().getCalendarHelper();
        switch (mode) {
            case MODE_DAY:
                currentDate = cal.dateBefore(currentDate, 1);
                break;
            case MODE_WEEK:
                currentDate = cal.dateBefore(currentDate, 7);
                break;
            case MODE_MONTH:
                currentDate = cal.monthBefore(currentDate, 1);
                break;
            case MODE_YEAR:
                currentDate = cal.yearBefore(currentDate, 1);
                break;
        }
        reloadData();
    }

    private void onToday() {
        currentDate = targetDate;
        reloadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.tag_optmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toggle_hierarchical_report) {
            getContexts().setPrefHierarachicalReport(!getContexts().isPrefHierarachicalReport());
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == listView) {
            doDetailList(position);
            // doPieChart(position);
        }
    }

    private void doDetailList(int position) {
        Balance b = listViewData.get(position);
        if (b.getTarget() == null) {
            // TODO some message
            return;
        }

        Intent intent = null;
        intent = new Intent(this, AccountDetailListActivity.class);
        if (currentStartDate != null) {
            intent.putExtra(AccountDetailListActivity.INTENT_START, currentStartDate);
        }
        if (currentEndDate != null) {
            intent.putExtra(AccountDetailListActivity.INTENT_END, currentEndDate);
        }
        intent.putExtra(AccountDetailListActivity.INTENT_TARGET, b.getTarget());
        intent.putExtra(AccountDetailListActivity.INTENT_TARGET_INFO, b.getName());
        this.startActivityForResult(intent, Constants.REQUEST_ACCOUNT_DETAIL_LIST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ACCOUNT_DETAIL_LIST_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });
        }
    }

}
