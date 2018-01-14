package com.fsquirrelsoft.financier.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fsquirrelsoft.commons.util.GUIs;
import com.fsquirrelsoft.financier.context.Contexts;
import com.fsquirrelsoft.financier.core.R;
import com.fsquirrelsoft.financier.data.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lancelot
 */
public class TagListHelper implements OnItemClickListener {

    private static String[] bindingFrom = new String[]{"id", "name"};

    private static int[] bindingTo = new int[]{R.id.tagmgnt_item_id, R.id.tagmgnt_item_name};

    private List<Tag> listViewData = new ArrayList<Tag>();

    private List<Map<String, Object>> listViewMapList = new ArrayList<Map<String, Object>>();

    private ListView listView;

    private SimpleAdapter listViewAdapter;

    private boolean clickeditable;

    private Activity activity;
    private Resources resources;

    public TagListHelper(Activity activity, Resources resources, boolean clickeditable) {
        this.activity = activity;
        this.resources = resources;
        this.clickeditable = clickeditable;
    }

    public void setup(ListView listview) {

        int layout = R.layout.tagmgnt_item;

        listViewAdapter = new SimpleAdapter(activity, listViewMapList, layout, bindingFrom, bindingTo);

        listView = listview;
        listView.setAdapter(listViewAdapter);
        if (clickeditable) {
            listView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == listView) {
            doEditTag(pos);
        }
    }

    public void reloadData(List<Tag> data) {
        listViewData = data;
        listViewMapList.clear();
        for (Tag tag : listViewData) {
            Map<String, Object> row = toTagMap(tag);
            listViewMapList.add(row);
        }
        listViewAdapter.notifyDataSetChanged();
    }

    private Map<String, Object> toTagMap(Tag tag) {
        Map<String, Object> row = new HashMap<String, Object>();

        String id = String.valueOf(tag.getId());
        String name = tag.getName();

        row.put(bindingFrom[0], new NamedItem(bindingFrom[0], id));
        row.put(bindingFrom[1], new NamedItem(bindingFrom[1], name));
        return row;
    }

    public void doNewTag() {
        Tag tag = new Tag("");
        Intent intent = null;
        intent = new Intent(activity, TagEditorActivity.class);
        intent.putExtra(TagEditorActivity.INTENT_MODE_CREATE, true);
        intent.putExtra(TagEditorActivity.INTENT_TAG, tag);
        activity.startActivityForResult(intent, Constants.REQUEST_TAG_EDITOR_CODE);
    }

    public void doEditTag(int pos) {
        Tag tag = (Tag) listViewData.get(pos);
        Intent intent = null;
        intent = new Intent(activity, TagEditorActivity.class);
        intent.putExtra(TagEditorActivity.INTENT_MODE_CREATE, false);
        intent.putExtra(TagEditorActivity.INTENT_TAG, tag);
        activity.startActivityForResult(intent, Constants.REQUEST_TAG_EDITOR_CODE);
    }

    public void doCopyTag(int pos) {
        Tag tag = (Tag) listViewData.get(pos);
        Intent intent = null;
        intent = new Intent(activity, TagEditorActivity.class);
        intent.putExtra(TagEditorActivity.INTENT_MODE_CREATE, true);
        intent.putExtra(TagEditorActivity.INTENT_TAG, tag);
        activity.startActivityForResult(intent, Constants.REQUEST_TAG_EDITOR_CODE);
    }

    public void doDeleteTag(final int pos) {
        final Tag tag = (Tag) listViewData.get(pos);
        GUIs.confirm(activity, resources.getString(R.string.qmsg_delete_tag, tag.getName()), new GUIs.OnFinishListener() {
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
                    Contexts.instance().getDataProvider().deleteTag(tag.getId());
                    listViewData.remove(pos);
                    listViewMapList.remove(pos);
                    listViewAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
    }

}
