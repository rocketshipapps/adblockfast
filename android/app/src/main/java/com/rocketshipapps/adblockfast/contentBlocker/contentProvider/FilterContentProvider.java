package com.rocketshipapps.adblockfast.contentBlocker.contentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;

import com.rocketshipapps.adblockfast.utils.Rule;

import java.io.IOException;

public class FilterContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        return null;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) {
        try {
            return ParcelFileDescriptor.open(Rule.get(getContext()), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) {
        try {
            return ParcelFileDescriptor.open(Rule.get(getContext()), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
