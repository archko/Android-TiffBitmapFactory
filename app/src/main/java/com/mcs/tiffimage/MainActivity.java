package com.mcs.tiffimage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.archko.tiff.DecodeArea;
import com.archko.tiff.TiffBitmapFactory;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.mcs.library.TiffImage;
import com.mcs.tiffimage.decoder.TiffImageDecoder;
import com.mcs.tiffimage.decoder.TiffPooledImageRegionDecoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DEFAULT_PIC_PATH = "default_pic";
    private static final String ASSETS_PIC_PATH = "test_assets.tif";
    private static final String ASSETS_TEST_PIC_PATH = "test_path.tif";
    private static final String ASSETS_TEST_STREAM_PATH = "test_stream.tif";

    private Button mButton;
    private ImageView mImageView;
    private String mPath, mStringPath;
    private Button mInfoButton;
    private Button mStreamButton, mResButton, mAssetsButton;
    private Button factoryButton, factoryFdButton, recycleButton, scaleButton;
    private TextView mInfoTextView;
    private String path2 = "/sdcard/DCIM/院本清明上河图.清.陈枚等合绘.67704X2036像素.台湾故宫博物院藏[Dujin.org].tif";
    private String path3 = "/sdcard/DCIM/清院本 清明上河图(一版)陈枚、孙祜、金昆、戴洪、程志道绢本 台北故宫35.6x1152.tif";

    private SubsamplingScaleImageView scaleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        .setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        }

        initView();
        initData();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.decode_button);
        mStreamButton = findViewById(R.id.decode_stream_button);
        mResButton = findViewById(R.id.decode_resource_button);
        mInfoButton = findViewById(R.id.info_button);
        mAssetsButton = findViewById(R.id.decode_assets_button);
        mImageView = findViewById(R.id.image);
        mInfoTextView = findViewById(R.id.info_text);
        mButton.setOnClickListener(mOnClickListener);
        mInfoButton.setOnClickListener(mOnClickListener);
        mStreamButton.setOnClickListener(mOnClickListener);
        mResButton.setOnClickListener(mOnClickListener);
        mAssetsButton.setOnClickListener(mOnClickListener);

        factoryButton = findViewById(R.id.factory_button);
        factoryButton.setOnClickListener(mOnClickListener);
        factoryFdButton = findViewById(R.id.factory_fd_button);
        factoryFdButton.setOnClickListener(mOnClickListener);
        recycleButton = findViewById(R.id.recycle_button);
        recycleButton.setOnClickListener(mOnClickListener);
        scaleButton = findViewById(R.id.scale_button);
        scaleButton.setOnClickListener(mOnClickListener);

        scaleImageView = findViewById(R.id.scaleImageView);
    }

    private void initData() {
        copyAssets();

        mPath = getFilesDir().getAbsolutePath() +
                File.separator + DEFAULT_PIC_PATH + File.separator + ASSETS_TEST_PIC_PATH;

        mStringPath = getFilesDir().getAbsolutePath() +
                File.separator + DEFAULT_PIC_PATH + File.separator + ASSETS_TEST_STREAM_PATH;
    }

    private void copyAssets() {
        String dirPath = getFilesDir().getAbsolutePath() + File.separator + DEFAULT_PIC_PATH;
        File file = new File(dirPath);

        if (!file.exists()) {
            file.mkdirs();
        }

        copyAssetsPath(dirPath, ASSETS_TEST_PIC_PATH, ASSETS_TEST_PIC_PATH);
        copyAssetsPath(dirPath, ASSETS_TEST_STREAM_PATH, ASSETS_TEST_STREAM_PATH);
    }

    private void copyAssetsPath(String outDir, String name, String assetsPath) {
        //复制
        AssetManager assetManager = getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(assetsPath);
            File outFile = new File(outDir, name);
            out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    TiffBitmapFactory factory = null;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            TiffImage tiffImage = null;
            byte[] bytes = null;
            Bitmap bitmap = null;
            ByteArrayInputStream byteArrayInputStream = null;
            boolean isSetBitmap = false;

            if (id == R.id.decode_button) {
                tiffImage = new TiffImage();
                bitmap = tiffImage.decode(mPath);
                isSetBitmap = true;
            } else if (id == R.id.info_button) {
                TiffImage.ImageInfo imageInfo = TiffImage.getImageInfo(mPath);
                mInfoTextView.setText("width==" + imageInfo.width + "; height==" + imageInfo.height);
                mInfoTextView.setVisibility(View.VISIBLE);
                return;
            } else if (id == R.id.decode_stream_button) {
                tiffImage = new TiffImage();
                bytes = file2Byte(mStringPath);
                byteArrayInputStream = new ByteArrayInputStream(bytes);
                bitmap = tiffImage.decode(byteArrayInputStream);
                isSetBitmap = true;
            } else if (id == R.id.decode_resource_button) {
                tiffImage = new TiffImage();
                bitmap = tiffImage.decodeResource(getResources(), R.raw.test_res);
                isSetBitmap = true;
            } else if (id == R.id.decode_assets_button) {
                tiffImage = new TiffImage();
                InputStream inputStream = null;
                try {
                    inputStream = MainActivity.this.getAssets().open(ASSETS_PIC_PATH);
                    bytes = inputStream2Byte(inputStream);
                    byteArrayInputStream = new ByteArrayInputStream(bytes);
                    bitmap = tiffImage.decode(byteArrayInputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isSetBitmap = true;
            } else if (id == R.id.factory_button) {
                if (null == factory) {
                    factory = new TiffBitmapFactory();
                    TiffBitmapFactory.ImageInfo info = factory.setupPath(path2);
                    String text = String.format("width==%s, height=%s, ori:%s", info.width, info.height, info.ori);
                    System.out.println(text);
                    mInfoTextView.setText(text);
                    mInfoTextView.setVisibility(View.VISIBLE);
                }
                TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
                options.inSampleSize = 1;
                DecodeArea area = new DecodeArea(1000, 100, 1080, 1600);
                options.inDecodeArea = area;
                bitmap = factory.decodePath(path2, options);
                mImageView.setImageBitmap(bitmap);
                isSetBitmap = false;
            } else if (id == R.id.factory_fd_button) {
                /*ParcelFileDescriptor descriptor = null;
                try {
                    descriptor = ParcelFileDescriptor.open(new File(path2), ParcelFileDescriptor.MODE_READ_ONLY);
                } catch (FileNotFoundException e) {
                }
                if (null == factory) {
                    factory = new TiffBitmapFactory();
                    TiffBitmapFactory.ImageInfo info = factory.setupFd(descriptor.getFd());
                    String text = String.format("width==%s, height=%s, ori:%s", info.width, info.height, info.ori);
                    System.out.println(text);
                    mInfoTextView.setText(text);
                    mInfoTextView.setVisibility(View.VISIBLE);
                }
                TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
                options.inSampleSize = 16;
                bitmap = factory.decodeFileDescriptor(descriptor.getFd(), options);
                mImageView.setImageBitmap(bitmap);
                isSetBitmap = false;*/
            } else if (id == R.id.recycle_button) {
                if (null != factory) {
                    factory.nativeClose();
                    factory = null;
                }

                mImageView.setImageBitmap(null);
            } else if (id == R.id.scale_button) {
                scaleImageView.setExecutor(AsyncTask.SERIAL_EXECUTOR);
                scaleImageView.setBitmapDecoderFactory(TiffImageDecoder::new);
                scaleImageView.setRegionDecoderFactory(TiffPooledImageRegionDecoder::new);
                scaleImageView.setImage(ImageSource.uri(path2));
            }

            if (isSetBitmap) {
                mImageView.setImageBitmap(bitmap);
            }

            if (bytes != null) {
                Log.d(TAG, "tiffImage decode stream width==" + tiffImage.getWidth() + "; height=="
                        + tiffImage.getHeight() + "; size==" +
                        Formatter.formatFileSize(MainActivity.this, tiffImage.getSize()) + "; byte'length==" +
                        Formatter.formatFileSize(MainActivity.this, bytes.length));
            } else {
                //Log.d(TAG, "tiffImage decode width==" + tiffImage.getWidth() + "; height=="
                //        + tiffImage.getHeight() + "; size==" +
                //        Formatter.formatFileSize(MainActivity.this, tiffImage.getSize()));
            }

            if (tiffImage != null) {
                tiffImage.release();
                tiffImage = null;
            }
        }
    };


    public byte[] file2Byte(String filePath) {
        byte[] buffer = null;
        FileInputStream fis = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            buffer = inputStream2Byte(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer;
    }

    public byte[] inputStream2Byte(InputStream inputStream) {
        byte[] buffer = null;
        InputStream fis = inputStream;
        ByteArrayOutputStream bos = null;

        try {
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return buffer;
    }
}
