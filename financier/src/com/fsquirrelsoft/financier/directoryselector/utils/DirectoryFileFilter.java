package com.fsquirrelsoft.financier.directoryselector.utils;

import java.io.File;
import java.io.FileFilter;

/**
 * from https://github.com/lemberg/directory-selector-dialog-preference
 */
public class DirectoryFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory();
    }
}
