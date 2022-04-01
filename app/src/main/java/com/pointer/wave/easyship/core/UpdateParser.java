package com.pointer.wave.easyship.core;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/** Parse an A/B update zip file. */
public class UpdateParser {

    private static final String TAG = "ROTAUpdateManager";
    private static final String PAYLOAD_BIN_FILE = "payload.bin";
    private static final String PAYLOAD_PROPERTIES = "payload_properties.txt";
    private static final String FILE_URL_PREFIX = "file://";
    private static final int ZIP_FILE_HEADER = 30;

    private UpdateParser() {
    }

    /**
     * Parse a zip file containing a system update and return a non null ParsedUpdate.
     */
    static ParsedUpdate parse(File file) throws IOException {
        long payloadOffset = 0;
        long payloadSize = 0;
        boolean payloadFound = false;
        String[] props = null;

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                long fileSize = entry.getCompressedSize();
                if (!payloadFound) {
                    payloadOffset += ZIP_FILE_HEADER + entry.getName().length();
                    if (entry.getExtra() != null) {
                        payloadOffset += entry.getExtra().length;
                    }
                }

                if (entry.isDirectory()) {
                    continue;
                } else if (entry.getName().equals(PAYLOAD_BIN_FILE)) {
                    payloadSize = fileSize;
                    payloadFound = true;
                } else if (entry.getName().equals(PAYLOAD_PROPERTIES)) {
                    try (BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(zipFile.getInputStream(entry)))) {
                        props = buffer.lines().toArray(String[]::new);
                    }
                }
                if (!payloadFound) {
                    payloadOffset += fileSize;
                }

                //if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, String.format("Entry %s", entry.getName()));
                //}
            }
        }
        return new ParsedUpdate(file, payloadOffset, payloadSize, props);
    }

    /** Information parsed from an update file. */
    static class ParsedUpdate {
        final String mUrl;
        final long mOffset;
        final long mSize;
        final String[] mProps;

        ParsedUpdate(File file, long offset, long size, String[] props) {
            mUrl = FILE_URL_PREFIX + file.getAbsolutePath();
            mOffset = offset;
            mSize = size;
            mProps = props;
        }

        /** Verify the update information is correct. */
        boolean isValid() {
            return mOffset >= 0 && mSize > 0 && mProps != null;
        }

        @Override
        public String toString() {
            return String.format(Locale.getDefault(),
                    "ParsedUpdate: URL=%s, offset=%d, size=%s, props=%s",
                    mUrl, mOffset, mSize, Arrays.toString(mProps));
        }
    }
}