package com.fsquirrelsoft.financier.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fsquirrelsoft.financier.core.R;
import com.fsquirrelsoft.commons.util.GUIs;
import com.fsquirrelsoft.financier.context.ContextsActivity;
import com.fsquirrelsoft.financier.data.DuplicateKeyException;
import com.fsquirrelsoft.financier.data.IDataProvider;
import com.fsquirrelsoft.financier.data.Tag;

/**
 * Edit or create a tag
 * 
 * @author Lancelot
 * 
 */
public class TagEditorActivity extends ContextsActivity implements android.view.View.OnClickListener {

    public static final String INTENT_MODE_CREATE = "modeCreate";
    public static final String INTENT_TAG = "tag";

    private boolean modeCreate;
    private int counterCreate;
    private Tag tag;
    private Tag workingTag;

    Activity activity;

    /** clone book without id **/
    private Tag clone(Tag tag) {
        Tag t = new Tag(tag.getName());
        return t;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tageditor);
        initIntent();
        initialEditor();
    }

    private void initIntent() {
        Bundle bundle = getIntentExtras();
        modeCreate = bundle.getBoolean(INTENT_MODE_CREATE, true);
        tag = (Tag) bundle.get(INTENT_TAG);
        workingTag = clone(tag);

        if (modeCreate) {
            setTitle(R.string.title_tageditor_create);
        } else {
            setTitle(R.string.title_tageditor_update);
        }
    }

    EditText nameEditor;

    Button okBtn;
    Button cancelBtn;
    Button closeBtn;

    private void initialEditor() {
        nameEditor = (EditText) findViewById(R.id.tageditor_name);
        nameEditor.setText(workingTag.getName());

        okBtn = (Button) findViewById(R.id.tageditor_ok);
        if (modeCreate) {
            okBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_add, 0, 0, 0);
            okBtn.setText(R.string.cact_create);
        } else {
            okBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_update, 0, 0, 0);
            okBtn.setText(R.string.cact_update);
        }
        okBtn.setOnClickListener(this);

        cancelBtn = (Button) findViewById(R.id.tageditor_cancel);
        closeBtn = (Button) findViewById(R.id.tageditor_close);

        cancelBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tageditor_ok) {
            doOk();
        } else if (v.getId() == R.id.tageditor_close) {
            doClose();
        } else if (v.getId() == R.id.tageditor_cancel) {
            doCancel();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doOk() {
        // verify
        String name = nameEditor.getText().toString().trim();
        if ("".equals(name)) {
            nameEditor.requestFocus();
            GUIs.alert(this, i18n.string(R.string.cmsg_field_empty, i18n.string(R.string.clabel_name)));
            return;
        }

        // assign
        workingTag.setName(name);

        IDataProvider idp = getContexts().getDataProvider();

        Tag namedTag = idp.findTag(name);
        if (modeCreate) {
            if (namedTag != null) {
                GUIs.alert(this, i18n.string(R.string.msg_tag_existed, name));
                return;
            } else {
                try {
                    idp.newTag(workingTag);
                    GUIs.shortToast(this, i18n.string(R.string.msg_tag_created, name));
                } catch (DuplicateKeyException e) {
                    GUIs.alert(this, i18n.string(R.string.cmsg_error, e.getMessage()));
                    return;
                }
            }
            setResult(RESULT_OK);
            workingTag = clone(workingTag);
            workingTag.setName("");
            nameEditor.setText("");
            nameEditor.requestFocus();
            counterCreate++;
            okBtn.setText(i18n.string(R.string.cact_create) + "(" + counterCreate + ")");
            cancelBtn.setVisibility(Button.GONE);
            closeBtn.setVisibility(Button.VISIBLE);

        } else {
            if (namedTag != null && namedTag.getId() != tag.getId()) {
                GUIs.alert(this, i18n.string(R.string.msg_tag_existed, name));
                return;
            } else {
                idp.updateTag(tag.getId(), workingTag);
                GUIs.shortToast(this, i18n.string(R.string.msg_tag_updated, name));
            }
            setResult(RESULT_OK);
            finish();
        }

    }

    private void doCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void doClose() {
        setResult(RESULT_OK);
        GUIs.shortToast(this, i18n.string(R.string.msg_created_tag, counterCreate));
        finish();
    }

}
