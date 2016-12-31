package com.fsquirrelsoft.financier.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.fsquirrelsoft.commons.util.GUIs;
import com.fsquirrelsoft.financier.context.ContextsActivity;
import com.fsquirrelsoft.financier.core.R;

/**
 * 
 * @author dennis
 * 
 */
public class PasswordProtectionActivity extends ContextsActivity implements OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdprotection);
        findViewById(R.id.pdprot_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.pdprot_ok) {
            doPasswordOk();
        }
    }

    private void doPasswordOk() {
        String password = getContexts().getPrefPassword();
        String pd = ((TextView) findViewById(R.id.pdprot_text)).getText().toString();
        if (password.equals(pd)) {
            setResult(RESULT_OK);
            finish();
        } else {
            GUIs.shortToast(this, R.string.msg_wrong_password);
        }
    }
}
