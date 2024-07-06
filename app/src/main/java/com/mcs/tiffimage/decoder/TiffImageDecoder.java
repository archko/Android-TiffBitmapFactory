package com.mcs.tiffimage.decoder;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.archko.tiff.TiffBitmapFactory;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Default implementation of {@link com.davemorrissey.labs.subscaleview.decoder.ImageDecoder}
 * using Android's {@link BitmapFactory}, based on the Skia library. This
 * works well in most circumstances and has reasonable performance, however it has some problems
 * with grayscale, indexed and CMYK images.
 */
public class TiffImageDecoder implements ImageDecoder {

    private static final String FILE_PREFIX = "file://";
    private static final String ASSET_PREFIX = FILE_PREFIX + "/android_asset/";
    private static final String RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";

    private final Bitmap.Config bitmapConfig;
    private final TiffBitmapFactory factory;
    private String path;

    @Keep
    @SuppressWarnings("unused")
    public TiffImageDecoder() {
        this(null);
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public TiffImageDecoder(@Nullable Bitmap.Config bitmapConfig) {
        Bitmap.Config globalBitmapConfig = SubsamplingScaleImageView.getPreferredBitmapConfig();
        if (bitmapConfig != null) {
            this.bitmapConfig = bitmapConfig;
        } else if (globalBitmapConfig != null) {
            this.bitmapConfig = globalBitmapConfig;
        } else {
            this.bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        factory = new TiffBitmapFactory();
    }

    @Override
    @NonNull
    public Bitmap decode(Context context, @NonNull Uri uri) throws Exception {
        String uriString = uri.toString();
        TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
        Bitmap bitmap = null;
        /*if (uriString.startsWith(RESOURCE_PREFIX)) {
            Resources res;
            String packageName = uri.getAuthority();
            if (context.getPackageName().equals(packageName)) {
                res = context.getResources();
            } else {
                PackageManager pm = context.getPackageManager();
                res = pm.getResourcesForApplication(packageName);
            }

            int id = 0;
            List<String> segments = uri.getPathSegments();
            int size = segments.size();
            if (size == 2 && segments.get(0).equals("drawable")) {
                String resName = segments.get(1);
                id = res.getIdentifier(resName, "drawable", packageName);
            } else if (size == 1 && TextUtils.isDigitsOnly(segments.get(0))) {
                try {
                    id = Integer.parseInt(segments.get(0));
                } catch (NumberFormatException ignored) {
                }
            }

            bitmap = TiffBitmapFactory.decodeResource(context.getResources(), id, options);
        } else */
        /*if (uriString.startsWith(ASSET_PREFIX)) {
            String assetName = uriString.substring(ASSET_PREFIX.length());
            ParcelFileDescriptor descriptor = context.getAssets().openFd(assetName).getParcelFileDescriptor();
            factory.setup(descriptor.getFd());
            bitmap = TiffBitmapFactory.decodeFileDescriptor(descriptor.getFd(), options);
        } else */
        if (uriString.startsWith(FILE_PREFIX)) {
            path = uriString.substring(FILE_PREFIX.length());
            factory.setupPath(path);
            bitmap = factory.decodePath(path, options);
        } else {
        }
        if (bitmap == null) {
            throw new RuntimeException("Skia image region decoder returned null bitmap - image format may not be supported");
        }
        return bitmap;
    }
}
