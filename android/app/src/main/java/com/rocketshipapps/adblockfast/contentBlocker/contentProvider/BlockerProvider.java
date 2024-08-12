package com.rocketshipapps.adblockfast.contentBlocker.contentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.util.Objects;

import com.rocketshipapps.adblockfast.utils.Ruleset;

public class BlockerProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int update(@NonNull Uri uri,
                      ContentValues values,
                      String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, String arg, Bundle extras) { return null; }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri,
                                         @NonNull String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(
            Ruleset.get(Objects.requireNonNull(getContext())), ParcelFileDescriptor.MODE_READ_ONLY
        );
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri,
                                         @NonNull String mode,
                                         CancellationSignal signal) throws FileNotFoundException {
        return ParcelFileDescriptor.open(
            Ruleset.get(Objects.requireNonNull(getContext())), ParcelFileDescriptor.MODE_READ_ONLY
        );
    }
}
