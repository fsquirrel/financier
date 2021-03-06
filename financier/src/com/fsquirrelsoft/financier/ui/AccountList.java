package com.fsquirrelsoft.financier.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.fsquirrelsoft.commons.util.Formats;
import com.fsquirrelsoft.commons.util.GUIs;
import com.fsquirrelsoft.financier.context.Contexts;
import com.fsquirrelsoft.financier.core.R;
import com.fsquirrelsoft.financier.data.Account;
import com.fsquirrelsoft.financier.data.AccountType;
import com.fsquirrelsoft.financier.data.IDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountList extends Fragment implements AdapterView.OnItemClickListener {

    public static final String TYPE_KEY = "type";
    private static final String[] bindingFrom = new String[]{"name", "initvalue", "id"};
    private static final int[] bindingTo = new int[]{R.id.accmgnt_item_name, R.id.accmgnt_item_initvalue, R.id.accmgnt_item_id};

    private List<Account> listViewData = new ArrayList<Account>();
    private List<Map<String, Object>> listViewMapList = new ArrayList<>();
    private SimpleAdapter listViewAdapter;
    private ListView listView;

    public static AccountList newInstance(AccountType accountType) {
        AccountList accountList = new AccountList();
        Bundle args = new Bundle();
        args.putSerializable(TYPE_KEY, accountType);
        accountList.setArguments(args);
        return accountList;
    }

    private SimpleAdapter getListViewAdapter() {
        if (listViewAdapter == null && this.getContext() != null) {
            listViewAdapter = new SimpleAdapter(this.getContext(), listViewMapList, R.layout.accmgnt_item, bindingFrom, bindingTo);
        }
        return listViewAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.accmgnt_fragment, container, false);

        getListViewAdapter().setViewBinder(new AccountListViewBinder());

        this.listView = (ListView) fragment.findViewById(R.id.accmgnt_list);
        this.listView.setAdapter(getListViewAdapter());
        this.listView.setOnItemClickListener(this);

        this.registerForContextMenu(this.listView);
        this.reloadData();

        return fragment;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == this.listView) {
            doEditAccount(pos);
        }
    }

    public CharSequence getLabel(Resources resources) {
        AccountType accountType = this.getAccountType();
        return accountType.getDisplay(resources);
    }

    public void reloadData() {
        IDataProvider idp = Contexts.instance().getDataProvider();

        AccountType type = this.getAccountType();
        listViewData = idp.listAccount(type);
        listViewMapList.clear();

        for (Account acc : listViewData) {
            Map<String, Object> row = new HashMap<>();
            AccountType accountType = AccountType.find(acc.getType());
            row.put(bindingFrom[0], new NamedItem(bindingFrom[0], accountType, acc.getName()));
            row.put(bindingFrom[1], new NamedItem(bindingFrom[1], accountType, Formats.double2String(acc.getInitialValue())));
            row.put(bindingFrom[2], new NamedItem(bindingFrom[2], accountType, acc.getId()));
            listViewMapList.add(row);
        }

        getListViewAdapter().notifyDataSetChanged();
    }

    public AccountType getAccountType() {
        return (AccountType) this.getArguments().getSerializable("type");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.accmgnt_list) {
            this.getActivity().getMenuInflater().inflate(R.menu.accmgnt_ctxmenu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            if (item.getItemId() == R.id.accmgnt_menu_edit) {
                doEditAccount(info.position);
                return true;
            } else if (item.getItemId() == R.id.accmgnt_menu_delete) {
                doDeleteAccount(info.position);
                return true;
            } else if (item.getItemId() == R.id.accmgnt_menu_copy) {
                doCopyAccount(info.position);
                return true;
            } else {
                return super.onContextItemSelected(item);
            }
        } else {
            return false;
        }
    }

    private void doEditAccount(int pos) {
        Account acc = this.listViewData.get(pos);
        Intent intent = new Intent(this.getContext(), AccountEditorActivity.class);
        intent.putExtra(AccountEditorActivity.INTENT_MODE_CREATE, false);
        intent.putExtra(AccountEditorActivity.INTENT_ACCOUNT, acc);
        this.startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
    }

    private void doDeleteAccount(int pos) {
        Account acc = this.listViewData.get(pos);
        String name = acc.getName();

        Contexts.instance().getDataProvider().deleteAccount(acc.getId());
        this.reloadData();
        GUIs.shortToast(this.getContext(), Contexts.instance().getResources().getString(R.string.msg_account_deleted, name));
    }

    private void doCopyAccount(int pos) {
        Account acc = listViewData.get(pos);
        Intent intent = new Intent(this.getContext(), AccountEditorActivity.class);
        intent.putExtra(AccountEditorActivity.INTENT_MODE_CREATE, true);
        intent.putExtra(AccountEditorActivity.INTENT_ACCOUNT, acc);
        this.startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ACCOUNT_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    AccountList.this.reloadData();
                }
            });
        }
    }

    private class AccountListViewBinder implements SimpleAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Object data, String text) {
            NamedItem item = (NamedItem) data;
            AccountType at = (AccountType) item.getValue();

            TextView tv = (TextView) view;
            tv.setTextColor(getResources().getColor(at.getDarkColor()));

            String name = item.getName();
            if (name.equals(bindingFrom[1])) {
                text = Contexts.instance().getResources().getString(R.string.label_initial_value) + " : " + text;
            }

            tv.setText(text);
            return true;
        }
    }

}
