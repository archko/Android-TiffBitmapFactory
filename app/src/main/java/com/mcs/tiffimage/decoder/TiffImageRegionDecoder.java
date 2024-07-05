package com.mcs.tiffimage.decoder;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.archko.tiff.DecodeArea;
import com.archko.tiff.TiffBitmapFactory;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;

import java.util.concurrent.locks.ReadWriteLock;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Default implementation of {@link com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder}
 * using Android's {@link BitmapRegionDecoder}, based on the Skia library. This
 * works well in most circumstances and has reasonable performance due to the cached decoder instance,
 * however it has some problems with grayscale, indexed and CMYK images.
 * <p>
 * A {@link ReadWriteLock} is used to delegate responsibility for multi threading behaviour to the
 * {@link BitmapRegionDecoder} instance on SDK &gt;= 21, whilst allowing this class to block until no
 * tiles are being loaded before recycling the decoder. In practice, {@link BitmapRegionDecoder} is
 * synchronized internally so this has no real impact on performance.
 */
public class TiffImageRegionDecoder implements ImageRegionDecoder {

    private static final String FILE_PREFIX = "file://";
    private static final String ASSET_PREFIX = FILE_PREFIX + "/android_asset/";
    private static final String RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";

    private TiffBitmapFactory factory;
    private final TiffBitmapFactory.ImageConfig bitmapConfig;
    private String path;
    private int width;
    private int height;

    @Keep
    @SuppressWarnings("unused")
    public TiffImageRegionDecoder() {
        this(null);
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public TiffImageRegionDecoder(@Nullable Bitmap.Config config) {
        Bitmap.Config globalBitmapConfig = SubsamplingScaleImageView.getPreferredBitmapConfig();
        /*if (config != null) {
            this.bitmapConfig = config;
        } else if (globalBitmapConfig != null) {
            this.bitmapConfig = globalBitmapConfig;
        } else {
            this.bitmapConfig = Bitmap.Config.ARGB_8888;
        }*/
        bitmapConfig = TiffBitmapFactory.ImageConfig.ARGB_8888;
        factory = new TiffBitmapFactory();
    }

    @Override
    @NonNull
    public Point init(Context context, @NonNull Uri uri) throws Exception {
        String uriString = uri.toString();
        TiffBitmapFactory.ImageInfo imageInfo = null;
        if (uriString.startsWith(FILE_PREFIX)) {
            path = uriString.substring(FILE_PREFIX.length());
            imageInfo = factory.setupPath(path);
            width = imageInfo.width;
            height = imageInfo.height;
        }

        return new Point(width, height);
    }

    @Override
    @NonNull
    public Bitmap decodeRegion(@NonNull Rect sRect, int sampleSize) {
        TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
        options.inSampleSize = sampleSize;
        //options.inPreferredConfig = bitmapConfig;
        options.inDecodeArea = new DecodeArea(sRect.left, sRect.top, sRect.width(), sRect.height());
        Bitmap bitmap = factory.decodePath(path, options);
        if (bitmap == null) {
            Log.d("TAG", String.format("sampleSize:%s,rect:%S,area:%s", sampleSize, sRect, options.inDecodeArea));
            throw new RuntimeException("tiff image decoder returned null bitmap - image format may not be supported");
        }
        return bitmap;
    }

    @Override
    public synchronized boolean isReady() {
        return factory != null && !TextUtils.isEmpty(path);
    }

    @Override
    public synchronized void recycle() {
        /*decoderLock.writeLock().lock();
        try {
            factory.nativeClose();
            factory = null;
        } finally {
            decoderLock.writeLock().unlock();
        }*/
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
