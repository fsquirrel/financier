package com.fsquirrelsoft.financier.data;

import android.content.res.Resources;

import com.fsquirrelsoft.commons.util.CalendarHelper;
import com.fsquirrelsoft.commons.util.Logger;
import com.fsquirrelsoft.financier.context.Contexts;
import com.fsquirrelsoft.financier.core.R;

import java.util.Date;

/**
 * @author dennis
 */
public class DataCreator {

    Resources resources;
    IDataProvider idp;

    public DataCreator(IDataProvider idp, Resources resources) {
        this.idp = idp;
        this.resources = resources;
    }

    public void createDefaultAccount() {
        createAccountNoThrow(resources.getString(R.string.defacc_salary), AccountType.INCOME, 0D, false);
        createAccountNoThrow(resources.getString(R.string.defacc_otherincome), AccountType.INCOME, 0D, false);

        createAccountNoThrow(resources.getString(R.string.defacc_food1), AccountType.EXPENSE, 0D, false);
        createAccountNoThrow(resources.getString(R.string.defacc_food2), AccountType.EXPENSE, 0D, false);
        createAccountNoThrow(resources.getString(R.string.defacc_entertainment), AccountType.EXPENSE, 0D, false);
        createAccountNoThrow(resources.getString(R.string.defacc_otherexpense), AccountType.EXPENSE, 0D, false);

        createAccountNoThrow(resources.getString(R.string.defacc_cash), AccountType.ASSET, 0D, true);
        createAccountNoThrow(resources.getString(R.string.defacc_bank1), AccountType.ASSET, 0D, false);
        createAccountNoThrow(resources.getString(R.string.defacc_bank2), AccountType.ASSET, 0D, false);

        createAccountNoThrow(resources.getString(R.string.defacc_creditcard), AccountType.LIABILITY, 0D, false);
    }

    public void createTestData(int loop) {
        // only for call from ui, so use uiInstance
        CalendarHelper cal = Contexts.instance().getCalendarHelper();

        Account income1 = createAccountNoThrow(resources.getString(R.string.defacc_salary), AccountType.INCOME, 0D, false);
        Account income2 = createAccountNoThrow(resources.getString(R.string.defacc_otherincome), AccountType.INCOME, 0D, false);

        Account expense1 = createAccountNoThrow(resources.getString(R.string.defacc_food1), AccountType.EXPENSE, 0D, false);
        Account expense2 = createAccountNoThrow(resources.getString(R.string.defacc_entertainment), AccountType.EXPENSE, 0D, false);
        Account expense3 = createAccountNoThrow(resources.getString(R.string.defacc_otherexpense), AccountType.EXPENSE, 0D, false);

        Account asset1 = createAccountNoThrow(resources.getString(R.string.defacc_cash), AccountType.ASSET, 5000, true);
        Account asset2 = createAccountNoThrow(resources.getString(R.string.defacc_bank1), AccountType.ASSET, 100000, false);

        Account liability1 = createAccountNoThrow(resources.getString(R.string.defacc_creditcard), AccountType.LIABILITY, 0D, false);

        Account other1 = createAccountNoThrow("Other", AccountType.OTHER, 0D, false);

        Date today = new Date();

        int base = 0;

        for (int i = 0; i < loop; i++) {
            createDetail(income1, asset1, cal.dateBefore(today, base + 3), 5000, "salary " + i);
            createDetail(income2, asset2, cal.dateBefore(today, base + 3), 10, "some where " + i);

            createDetail(asset1, expense1, cal.dateBefore(today, base + 2), 100, "date with Cica " + i);
            createDetail(asset1, expense1, cal.dateBefore(today, base + 2), 30, "taiwan food is great " + i);
            createDetail(asset1, expense2, cal.dateBefore(today, base + 1), 11, "buy DVD " + i);
            createDetail(asset1, expense3, cal.dateBefore(today, base + 1), 300, "it is a secrt  " + i);

            createDetail(asset1, asset2, cal.dateBefore(today, base + 0), 4000, "deposit  " + i);
            createDetail(asset2, asset1, cal.dateBefore(today, base + 0), 1000, "drawing  " + i);

            createDetail(liability1, expense2, cal.dateBefore(today, base + 2), 20.9, "buy Game " + i);
            createDetail(asset1, liability1, cal.dateBefore(today, base + 1), 19.9, "pay credit card " + i);
            createDetail(asset1, other1, cal.dateBefore(today, base + 1), 1, "salary to other " + i);
            createDetail(other1, liability1, cal.dateBefore(today, base + 1), 1, "other pay credit card " + i);

            base = base + 5;
        }

    }

    private Detail createDetail(Account from, Account to, Date date, double money, String note) {
        Detail det = new Detail(from.getId(), to.getId(), date, money, note);
        idp.newDetail(det);
        return det;
    }

    private Account createAccountNoThrow(String name, AccountType type, double initval, boolean cashAccount) {
        try {
            Account account = null;
            if ((account = idp.findAccount(type.getType(), name)) == null) {
                if (Contexts.DEBUG) {
                    Logger.d("createDefaultAccount : " + name);
                }
                account = new Account(type.getType(), name, initval);
                account.setCashAccount(cashAccount);
                idp.newAccount(account);
            }
            return account;
        } catch (DuplicateKeyException e) {
            if (Contexts.DEBUG) {
                Logger.d(e.getMessage(), e);
            }
        }
        return null;
    }
}
