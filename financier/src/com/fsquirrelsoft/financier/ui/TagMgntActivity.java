package com.fsquirrelsoft.financier.ui;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.fsquirrelsoft.financier.core.R;
import com.fsquirrelsoft.commons.util.GUIs;
import com.fsquirrelsoft.financier.context.ContextsActivity;
import com.fsquirrelsoft.financier.data.IDataProvider;
import com.fsquirrelsoft.financier.data.Tag;

/**
 * 
 * @author Lancelot
 * 
 */
public class TagMgntActivity extends ContextsActivity {

    TagListHelper tagListHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tagmgnt);
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

    }

    private void initialContent() {

        tagListHelper = new TagListHelper(this, i18n, true);
        ListView listView = (ListView) findViewById(R.id.tagmgnt_list);
        tagListHelper.setup(listView);

        registerForContextMenu(listView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_TAG_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });
        }
    }

    private void reloadData() {
        final IDataProvider idp = getContexts().getDataProvider();
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            List<Tag> data = null;

            @Override
            public void run() {
                data = idp.listAllTags();
            }

            @Override
            public void onBusyFinish() {
                // update data
                tagListHelper.reloadData(data);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.tagmgnt_optmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.tagmgnt_menu_new) {
            tagListHelper.doNewTag();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.tagmgnt_list) {
            getMenuInflater().inflate(R.menu.tagmgnt_ctxmenu, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.tagmgnt_menu_edit) {
            tagListHelper.doEditTag(info.position);
            return true;
        } else if (item.getItemId() == R.id.tagmgnt_menu_delete) {
            tagListHelper.doDeleteTag(info.position);
            return true;
        } else if (item.getItemId() == R.id.tagmgnt_menu_copy) {
            tagListHelper.doCopyTag(info.position);
            finish();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
