package com.mcs.tiffimage.decoder;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;

import java.util.concurrent.Executor;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>
 * An implementation of {@link com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder} using a pool of {@link BitmapRegionDecoder}s,
 * to provide true parallel loading of tiles. This is only effective if parallel loading has been
 * enabled in the view by calling {@link SubsamplingScaleImageView#setExecutor(Executor)}
 * with a multi-threaded {@link Executor} instance.
 * </p><p>
 * One decoder is initialised when the class is initialised. This is enough to decode base layer tiles.
 * Additional decoders are initialised when a subregion of the image is first requested, which indicates
 * interaction with the view. Creation of additional encoders stops when {@link #allowAdditionalDecoder(int, long)}
 * returns false. The default implementation takes into account the file size, number of CPU cores,
 * low memory status and a hard limit of 4. Extend this class to customise this.
 * </p><p>
 * <b>WARNING:</b> This class is highly experimental and not proven to be stable on a wide range of
 * devices. You are advised to test it thoroughly on all available devices, and code your app to use
 * {@link TiffImageRegionDecoder} on old or low powered devices you could not test.
 * </p>
 */
public class TiffPooledImageRegionDecoder implements ImageRegionDecoder {

    private static final String TAG = TiffPooledImageRegionDecoder.class.getSimpleName();

    private static boolean debug = false;

    private static final String FILE_PREFIX = "file://";
    private static final String ASSET_PREFIX = FILE_PREFIX + "/android_asset/";
    private static final String RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";

    private final Bitmap.Config bitmapConfig;

    private Context context;
    private Uri uri;

    private long fileLength = Long.MAX_VALUE;
    private final Point imageDimensions = new Point(0, 0);
    private TiffImageRegionDecoder decoder;

    @Keep
    public TiffPooledImageRegionDecoder() {
        this(null);
    }

    @Keep
    public TiffPooledImageRegionDecoder(@Nullable Bitmap.Config bitmapConfig) {
        Bitmap.Config globalBitmapConfig = SubsamplingScaleImageView.getPreferredBitmapConfig();
        if (bitmapConfig != null) {
            this.bitmapConfig = bitmapConfig;
        } else if (globalBitmapConfig != null) {
            this.bitmapConfig = globalBitmapConfig;
        } else {
            this.bitmapConfig = Bitmap.Config.RGB_565;
        }
    }

    /**
     * Controls logging of debug messages. All instances are affected.
     *
     * @param debug true to enable debug logging, false to disable.
     */
    @Keep
    @SuppressWarnings("unused")
    public static void setDebug(boolean debug) {
        TiffPooledImageRegionDecoder.debug = debug;
    }

    @Override
    @NonNull
    public Point init(final Context context, @NonNull final Uri uri) throws Exception {
        this.context = context;
        this.uri = uri;
        initialiseDecoder();
        return this.imageDimensions;
    }

    private void lazyInit() {
        debug("Starting lazy init of additional decoders");
        try {
            if (null == decoder) {
                long start = System.currentTimeMillis();
                debug("Starting decoder");
                initialiseDecoder();
                long end = System.currentTimeMillis();
                debug("Started decoder, took " + (end - start) + "ms");
            }
        } catch (Exception e) {
            debug("Failed to start decoder: " + e.getMessage());
        }
    }

    private void initialiseDecoder() throws Exception {
        decoder = new TiffImageRegionDecoder(null);
        decoder.init(context, uri);

        //this.fileLength = decoder.getFileLength();

        this.imageDimensions.set(decoder.getWidth(), decoder.getHeight());
    }

    /**
     * Acquire a read lock to prevent decoding overlapping with recycling, then check the pool still
     * exists and acquire a decoder to load the requested region. There is no check whether the pool
     * currently has decoders, because it's guaranteed to have one decoder after {@link #init(Context, Uri)}
     * is called and be null once {@link #recycle()} is called. In practice the view can't call this
     * method until after {@link #init(Context, Uri)}, so there will be no blocking on an empty pool.
     */
    @Override
    @NonNull
    public Bitmap decodeRegion(@NonNull Rect sRect, int sampleSize) {
        debug("Decode region " + sRect + " on thread " + Thread.currentThread().getName());
        if (sRect.width() < imageDimensions.x || sRect.height() < imageDimensions.y) {
            lazyInit();
        }
        try {
            if (decoder != null /*&& !decoder.isRecycled()*/) {
                //BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = sampleSize;
                //options.inPreferredConfig = bitmapConfig;
                Bitmap bitmap = decoder.decodeRegion(sRect, sampleSize);
                if (bitmap == null) {
                    debug("Tiff image decoder returned null bitmap - image format may not be supported");
                }
                return bitmap;
            }
        } catch (Exception e) {
            debug(String.format("Failed to decode:%s-%s, %s ", sRect, sampleSize, e.getMessage()));
        }
        return null;
    }

    @Override
    public synchronized boolean isReady() {
        return decoder != null;
    }

    @Override
    public synchronized void recycle() {
        if (decoder != null) {
            decoder.recycle();
            decoder = null;
            context = null;
            uri = null;
        }
    }

    private void debug(String message) {
        if (debug) {
            Log.d(TAG, message);
        }
    }
}
