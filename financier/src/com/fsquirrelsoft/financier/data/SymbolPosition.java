/**
 *
 */
package com.fsquirrelsoft.financier.data;

import android.content.res.Resources;

import com.fsquirrelsoft.financier.core.R;

/**
 * @author dennis
 */
public enum SymbolPosition {

    NONE(0),
    FRONT(1),
    AFTER(2);

    int type;

    SymbolPosition(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getDisplay(Resources resource) {
        return getDisplay(resource, type);
    }

    static public SymbolPosition find(int type) {
        switch (type) {
            case 1:
                return FRONT;
            case 2:
                return AFTER;
            default:
                return NONE;
        }
    }

    static public String getDisplay(Resources resource, int type) {
        SymbolPosition pos = find(type);
        switch (pos) {
            case FRONT:
                return resource.getString(R.string.label_position_front);
            case AFTER:
                return resource.getString(R.string.label_position_after);
            default:
                return resource.getString(R.string.label_position_none);
        }
    }

    static private final SymbolPosition[] available = new SymbolPosition[]{NONE, FRONT, AFTER};

    public static SymbolPosition[] getAvailable() {
        return available;
    }
}
