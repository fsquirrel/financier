package com.fsquirrelsoft.financier.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * modified from http://stackoverflow.com/a/6022474
 * 
 * @author Lancelot
 * 
 */
public class MultiSpinner extends Spinner implements OnMultiChoiceClickListener, OnCancelListener {

    private List<Object> items;
    private boolean[] selected;
    private MultiSpinnerListener listener;

    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked) {
            selected[which] = true;
        } else {
            selected[which] = false;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        listener.onItemsSelected(selected);
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        List<String> itemArray = new ArrayList<String>();
        for (Object obj : items) {
            itemArray.add(obj.toString());
        }
        builder.setMultiChoiceItems(itemArray.toArray(new CharSequence[itemArray.size()]), selected, this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setOnCancelListener(this);
        builder.show();
        return true;
    }

    public void setItems(List<Object> items, String allText, MultiSpinnerListener listener) {
        this.items = items;
        this.listener = listener;

        // all unselected by default
        selected = new boolean[items.size()];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = false;
        }

        // all text on the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, new String[] { allText });
        setAdapter(adapter);
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }

    public void setSelectedItems(List<Object> selectedItems) {
        for (int i = 0; i < items.size(); i++) {
            if (selectedItems.contains(items.get(i))) {
                selected[i] = true;
            } else {
                selected[i] = false;
            }
        }
        if (selected != null && selected.length > 0) {
            listener.onItemsSelected(selected);
        }
    }
}
