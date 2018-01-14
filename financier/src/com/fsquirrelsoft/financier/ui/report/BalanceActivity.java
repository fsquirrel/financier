package com.fsquirrelsoft.financier.ui.report;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
import com.fsquirrelsoft.financier.data.Account;
import com.fsquirrelsoft.financier.data.AccountType;
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
public class BalanceActivity extends ContextsActivity implements OnClickListener, OnItemClickListener {

    public static final int MODE_MONTH = 0;
    public static final int MODE_YEAR = 1;
    public static final int MODE_WEEK = 2;
    public static final int MODE_DAY = 3;

    public static final String INTENT_BALANCE_DATE = "balanceDate";
    public static final String INTENT_MODE = "mode";
    public static final String INTENT_TARGET_DATE = "target";
    public static final String INTENT_TOTAL_MODE = "modeTotal";

    TextView infoView;
    View toolbarView;

    private Date targetDate;
    private Date currentDate;
    private int mode = MODE_MONTH;
    private boolean totalMode = false;

    private DateFormat monthDayDateFormat;
    private DateFormat monthDateFormat;
    private DateFormat yearDateFormat;
    private DateFormat dateFormat;

    private Date currentStartDate;
    private Date currentEndDate;

    ImageButton modeBtn;

    private static String[] bindingFrom = new String[]{"layout", "name", "money"};

    private static int[] bindingTo = new int[]{R.id.report_balance_layout, R.id.report_balance_item_name, R.id.report_balance_item_money};

    private List<Balance> listViewData = new ArrayList<Balance>();

    private List<Map<String, Object>> listViewMapList = new ArrayList<Map<String, Object>>();

    private ListView listView;

    private SimpleAdapter listViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_balance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
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
        totalMode = b.getBoolean(INTENT_TOTAL_MODE, true);
        Object o = b.get(INTENT_BALANCE_DATE);
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

        infoView = (TextView) findViewById(R.id.report_balance_infobar);
        toolbarView = findViewById(R.id.report_balance_toolbar);

        findViewById(R.id.report_balance_prev).setOnClickListener(this);
        findViewById(R.id.report_balance_next).setOnClickListener(this);
        findViewById(R.id.report_balance_today).setOnClickListener(this);
        modeBtn = (ImageButton) findViewById(R.id.report_balance_mode);
        modeBtn.setOnClickListener(this);
        reloadToolbar();

        listViewAdapter = new SimpleAdapter(this, listViewMapList, R.layout.report_balance_item, bindingFrom, bindingTo);
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
                AccountType at = AccountType.find(b.getType());
                TextView tv = (TextView) view;

                if (at == AccountType.INCOME) {
                    if (b.getIndent() == 0) {
                        tv.setTextColor(getResources().getColor(R.color.income_fgl));
                    } else {
                        tv.setTextColor(getResources().getColor(R.color.income_fgd));
                    }
                } else if (at == AccountType.EXPENSE) {
                    if (b.getIndent() == 0) {
                        tv.setTextColor(getResources().getColor(R.color.expense_fgl));
                    } else {
                        tv.setTextColor(getResources().getColor(R.color.expense_fgd));
                    }
                } else if (at == AccountType.ASSET) {
                    if (b.getIndent() == 0) {
                        tv.setTextColor(getResources().getColor(R.color.asset_fgl));
                    } else {
                        tv.setTextColor(getResources().getColor(R.color.asset_fgd));
                    }
                } else if (at == AccountType.LIABILITY) {
                    if (b.getIndent() == 0) {
                        tv.setTextColor(getResources().getColor(R.color.liability_fgl));
                    } else {
                        tv.setTextColor(getResources().getColor(R.color.liability_fgd));
                    }
                } else if (at == AccountType.OTHER) {
                    if (b.getIndent() == 0) {
                        tv.setTextColor(getResources().getColor(R.color.other_fgl));
                    } else {
                        tv.setTextColor(getResources().getColor(R.color.other_fgd));
                    }
                } else {
                    if (b.getIndent() == 0) {
                        tv.setTextColor(getResources().getColor(R.color.unknow_fgl));
                    } else {
                        tv.setTextColor(getResources().getColor(R.color.unknow_fgd));
                    }
                }
                adjustItem(tv, b, GUIs.getDPRatio(BalanceActivity.this));
                return false;
            }
        });

        listView = (ListView) findViewById(R.id.report_balance_list);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);
    }

    protected void adjustLayout(LinearLayout layout, Balance b) {
        switch (b.getIndent()) {
            case 0:
                layout.setBackgroundDrawable((getResources().getDrawable(R.drawable.selector_balance_indent0)));
                break;
            case 1:
                layout.setBackgroundDrawable((getResources().getDrawable(R.drawable.selector_balance_indent)));
                break;
            default:
                layout.setBackgroundDrawable((getResources().getDrawable(R.drawable.selector_balance_indent)));
                break;
        }
    }

    protected void adjustItem(TextView tv, Balance b, float dp) {
        float fontPixelSize = 18;
        float ratio = 0;
        int marginLeft = 0;
        int marginRight = 5;
        int paddingTB = 0;

        int indent = b.getIndent();

        if (indent <= 0) {
            ratio = 1F;
            paddingTB = 5;
            marginLeft = 5;
        } else {
            ratio = 0.85F;
            paddingTB = 3;
            marginLeft = 5 + 10 * indent;
        }

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
                currentStartDate = totalMode ? null : cal.yearStartDate(currentDate);
                break;
            case MODE_MONTH:
                currentEndDate = cal.monthEndDate(currentDate);
                currentStartDate = totalMode ? null : cal.monthStartDate(currentDate);
                break;
            case MODE_WEEK:
                currentEndDate = cal.weekEndDate(currentDate);
                currentStartDate = totalMode ? null : cal.weekStartDate(currentDate);
                break;
            case MODE_DAY:
                currentEndDate = cal.toDayEnd(currentDate);
                currentStartDate = totalMode ? null : cal.toDayStart(currentDate);
                break;
        }
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            List<Balance> all = new ArrayList<Balance>();

            @Override
            public void run() {
                boolean hierarchical = getContexts().isPrefHierarachicalReport();
                boolean hideZero = getContexts().isPrefHideZeroAccounts();

                List<Balance> asset = BalanceHelper.calculateBalanceList(AccountType.ASSET, currentStartDate, currentEndDate, hideZero);
                List<Balance> income = BalanceHelper.calculateBalanceList(AccountType.INCOME, currentStartDate, currentEndDate, hideZero);
                List<Balance> expense = BalanceHelper.calculateBalanceList(AccountType.EXPENSE, currentStartDate, currentEndDate, hideZero);
                List<Balance> liability = BalanceHelper.calculateBalanceList(AccountType.LIABILITY, currentStartDate, currentEndDate, hideZero);
                List<Balance> other = BalanceHelper.calculateBalanceList(AccountType.OTHER, currentStartDate, currentEndDate, hideZero);

                if (hierarchical) {
                    asset = BalanceHelper.adjustNestedTotalBalance(AccountType.ASSET, totalMode ? getResources().getString(R.string.label_balance_tasset) : getResources().getString(R.string.label_asset), asset, hideZero);
                    income = BalanceHelper.adjustNestedTotalBalance(AccountType.INCOME, totalMode ? getResources().getString(R.string.label_balance_tincome) : getResources().getString(R.string.label_income), income, hideZero);
                    expense = BalanceHelper.adjustNestedTotalBalance(AccountType.EXPENSE, totalMode ? getResources().getString(R.string.label_balance_texpense) : getResources().getString(R.string.label_expense), expense,
                            hideZero);
                    liability = BalanceHelper.adjustNestedTotalBalance(AccountType.LIABILITY, totalMode ? getResources().getString(R.string.label_balance_tliability) : getResources().getString(R.string.label_liability),
                            liability, hideZero);
                    other = BalanceHelper.adjustNestedTotalBalance(AccountType.OTHER, totalMode ? getResources().getString(R.string.label_balance_tother) : getResources().getString(R.string.label_other), other, hideZero);

                } else {
                    asset = BalanceHelper.adjustTotalBalance(AccountType.ASSET, totalMode ? getResources().getString(R.string.label_balance_tasset) : getResources().getString(R.string.label_asset), asset);
                    income = BalanceHelper.adjustTotalBalance(AccountType.INCOME, totalMode ? getResources().getString(R.string.label_balance_tincome) : getResources().getString(R.string.label_income), income);
                    expense = BalanceHelper.adjustTotalBalance(AccountType.EXPENSE, totalMode ? getResources().getString(R.string.label_balance_texpense) : getResources().getString(R.string.label_expense), expense);
                    liability = BalanceHelper.adjustTotalBalance(AccountType.LIABILITY, totalMode ? getResources().getString(R.string.label_balance_tliability) : getResources().getString(R.string.label_liability), liability);
                    other = BalanceHelper.adjustTotalBalance(AccountType.OTHER, totalMode ? getResources().getString(R.string.label_balance_tother) : getResources().getString(R.string.label_other), other);

                }

                if (totalMode) {
                    all.addAll(asset);
                    all.addAll(liability);
                    all.addAll(income);
                    all.addAll(expense);
                    all.addAll(other);
                } else {
                    all.addAll(income);
                    all.addAll(expense);
                    all.addAll(asset);
                    all.addAll(liability);
                    all.addAll(other);
                }
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
                        if (totalMode) {
                            infoView.setText(getResources().getString(R.string.label_balance_mode_year_total, yearDateFormat.format(currentDate)));
                        } else {
                            infoView.setText(getResources().getString(R.string.label_balance_mode_year, yearDateFormat.format(currentDate)));
                        }
                        break;
                    case MODE_MONTH:
                        if (totalMode) {
                            infoView.setText(getResources().getString(R.string.label_balance_mode_month_total, monthDateFormat.format(cal.monthStartDate(currentDate)),
                                    monthDayDateFormat.format(cal.monthEndDate(currentDate))));
                        } else {
                            infoView.setText(getResources().getString(R.string.label_balance_mode_month, monthDateFormat.format(cal.monthStartDate(currentDate)),
                                    monthDayDateFormat.format(cal.monthStartDate(currentDate)), monthDayDateFormat.format(cal.monthEndDate(currentDate))));
                        }
                        break;
                    case MODE_WEEK:
                        if (totalMode) {
                            infoView.setText(getResources().getString(R.string.label_balance_mode_week_total, yearDateFormat.format(currentDate), cal.weekOfYear(currentDate),
                                    monthDayDateFormat.format(cal.weekEndDate(currentDate))));
                        } else {
                            infoView.setText(getResources().getString(R.string.label_balance_mode_week, yearDateFormat.format(currentDate), cal.weekOfYear(currentDate),
                                    monthDayDateFormat.format(cal.weekEndDate(currentDate)), monthDayDateFormat.format(cal.weekStartDate(currentDate))));
                        }
                        break;
                    case MODE_DAY:
                        if (totalMode) {
                            infoView.setText(getResources().getString(R.string.label_balance_mode_day_total, dateFormat.format(currentDate)));
                        } else {
                            infoView.setText(getResources().getString(R.string.label_balance_mode_day, dateFormat.format(currentDate)));
                        }
                        break;
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.report_balance_prev) {
            onPrev();
        } else if (v.getId() == R.id.report_balance_next) {
            onNext();
        } else if (v.getId() == R.id.report_balance_today) {
            onToday();
        } else if (v.getId() == R.id.report_balance_mode) {
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
        getMenuInflater().inflate(R.menu.balance_optmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.balance_menu_yearly_runchart) {
            doYearlyRunChart();
            return true;
        } else if (item.getItemId() == R.id.toggle_hierarchical_report) {
            getContexts().setPrefHierarachicalReport(!getContexts().isPrefHierarachicalReport());
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });
            return true;
        } else if (item.getItemId() == R.id.hide_zero_accounts) {
            getContexts().setPrefHideZeroAccounts(!getContexts().isPrefHideZeroAccounts());
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.report_balance_list) {
            // AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            getMenuInflater().inflate(R.menu.balance_ctxmenu, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.balance_menu_piechart) {
            doPieChart(info.position);
            return true;
        } else if (item.getItemId() == R.id.balance_menu_yearly_timechart) {
            doYearlyTimeChart(info.position);
            return true;
        } else if (item.getItemId() == R.id.balance_menu_yearly_cumulative_timechart) {
            doYearlyCumulativeTimeChart(info.position);
            return true;
        } else if (item.getItemId() == R.id.balance_menu_yearly_runchart) {
            doYearlyRunChart();
            return true;
        } else if (item.getItemId() == R.id.balance_menu_detlist) {
            doDetailList(info.position);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void doPieChart(final int pos) {

        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
                Balance b = listViewData.get(pos);
                AccountType at;
                List<Balance> group = b.getGroup();
                if (b.getTarget() instanceof Account) {
                    group = new ArrayList<Balance>(group);
                    group.remove(b);
                    group.add(0, b);
                    at = AccountType.find(((Account) b.getTarget()).getType());
                } else {
                    at = AccountType.find(b.getType());
                }
                List<Balance> list = new ArrayList<Balance>();
                for (Balance g : group) {
                    if (!(g.getTarget() instanceof Account)) {
                        continue;
                    }
                    Account acc = (Account) g.getTarget();
                    Balance balance = BalanceHelper.calculateBalance(acc, currentStartDate, currentEndDate);
                    list.add(balance);
                }
                Intent intent = new BalancePieChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this)).createIntent(at, list);
                startActivity(intent);
            }
        });
    }

    private void doYearlyTimeChart(final int pos) {
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
                Balance b = listViewData.get(pos);
                AccountType at;
                List<Balance> group = b.getGroup();
                if (b.getTarget() instanceof Account) {
                    group = new ArrayList<Balance>(group);
                    group.remove(b);
                    group.add(0, b);
                    at = AccountType.find(((Account) b.getTarget()).getType());
                } else {
                    at = AccountType.find(b.getType());
                }

                List<List<Balance>> balances = new ArrayList<List<Balance>>();

                for (Balance g : group) {
                    if (!(g.getTarget() instanceof Account)) {
                        continue;
                    }
                    Account acc = (Account) g.getTarget();
                    List<Balance> blist = new ArrayList<Balance>();
                    balances.add(blist);
                    Date d = calHelper.yearStartDate(g.getDate());
                    for (int i = 0; i < 12; i++) {
                        Balance balance = BalanceHelper.calculateBalance(acc, calHelper.monthStartDate(d), calHelper.monthEndDate(d));
                        blist.add(balance);
                        d = calHelper.monthAfter(d, 1);
                    }
                }

                Intent intent = new BalanceTimeChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this))
                        .createIntent(getResources().getString(R.string.label_balance_yearly_timechart, at.getDisplay(getResources()), yearDateFormat.format(currentDate)), balances);
                startActivity(intent);
            }
        });
    }

    private void doYearlyCumulativeTimeChart(final int pos) {
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {

                Balance b = listViewData.get(pos);
                AccountType at;
                List<Balance> group = b.getGroup();
                if (b.getTarget() instanceof Account) {
                    group = new ArrayList<Balance>(group);
                    group.remove(b);
                    group.add(0, b);
                    at = AccountType.find(((Account) b.getTarget()).getType());
                } else {
                    at = AccountType.find(b.getType());
                }

                List<List<Balance>> balances = new ArrayList<List<Balance>>();

                for (Balance g : group) {
                    if (!(g.getTarget() instanceof Account)) {
                        continue;
                    }
                    Account acc = (Account) g.getTarget();
                    List<Balance> blist = new ArrayList<Balance>();
                    balances.add(blist);
                    Date d = calHelper.yearStartDate(g.getDate());
                    double total = 0D;
                    for (int i = 0; i < 12; i++) {
                        Balance balance = BalanceHelper.calculateBalance(acc, i == 0 ? null : calHelper.monthStartDate(d), calHelper.monthEndDate(d));
                        total += balance.getMoney();
                        balance.setMoney(total);
                        blist.add(balance);
                        d = calHelper.monthAfter(d, 1);
                    }
                }

                Intent intent = new BalanceTimeChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this))
                        .createIntent(getResources().getString(R.string.label_balance_yearly_cumulative_timechart, at.getDisplay(getResources()), yearDateFormat.format(currentDate)), balances);
                startActivity(intent);
            }
        });
    }

    private void doYearlyRunChart() {
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
                boolean[] yearly = new boolean[]{false, false, true, true, false};
                AccountType[] ats = new AccountType[]{AccountType.ASSET, AccountType.LIABILITY, AccountType.INCOME, AccountType.EXPENSE, AccountType.OTHER};
                List<List<Balance>> balances = new ArrayList<List<Balance>>();
                Date yearstart = calHelper.yearStartDate(currentDate);
                for (int j = 0; j < ats.length; j++) {
                    AccountType at = ats[j];
                    List<Balance> blist = new ArrayList<Balance>();
                    balances.add(blist);
                    Date d = yearstart;
                    if (yearly[j]) {
                        for (int i = 0; i < 12; i++) {
                            Balance balance = BalanceHelper.calculateBalance(at, yearstart, calHelper.monthEndDate(d));
                            blist.add(balance);
                            d = calHelper.monthAfter(d, 1);
                        }
                    } else {
                        double total = 0D;
                        for (int i = 0; i < 12; i++) {
                            Balance balance = BalanceHelper.calculateBalance(at, i == 0 ? null : calHelper.monthStartDate(d), calHelper.monthEndDate(d));
                            total += balance.getMoney();
                            balance.setMoney(total);
                            blist.add(balance);
                            d = calHelper.monthAfter(d, 1);
                        }
                    }
                }

                Intent intent = new BalanceTimeChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this))
                        .createIntent(getResources().getString(R.string.label_balance_yearly_runchart, yearDateFormat.format(currentDate)), balances);
                startActivity(intent);
            }
        });
    }

}
