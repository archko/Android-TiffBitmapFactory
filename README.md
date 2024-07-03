# fork from Android-TiffBitmapFactory

TiffBitmapFactory is an Android library that allows opening and saving images in *.tif format (
See [Wikipedia](https://en.wikipedia.org/wiki/Tagged_Image_File_Format)) on Android devices.
[Android-TiffBitmapFactory](https://github.com/Beyka/Android-TiffBitmapFactory)

# Proguard

If you use proguard add this to you config file:

```Gradle
-keep class com.archko.tiff.**{ *; }
```

# updates

- remove convert
- remove saver
- remove libpng
- reuse decoder, it's very usefull,if a tiff is very large, a decoder can decode a tile once, but
  old factory is static and can't be reused.
- change origBufferSize to long, large tiff exceed int.max value, some tiff is large than 2gb.
- remove availableMemory, in my redmi k40 pro, it always 256mb.

# setup and decode tiff

private native ImageInfo nativeSetupFd(int fd, Options options, IProgressListener listener);
private native ImageInfo nativeSetupPath(String path, Options options, IProgressListener
listener);
if a tiff is large, before decode, setup and return imageinfo is usefull.

``` 
TiffBitmapFactory factory = new TiffBitmapFactory();
TiffBitmapFactory.ImageInfo info = factory.setupPath(path2);
String text = String.format("width==%s, height=%s, ori:%s", info.width, info.height, info.ori);
System.out.println(text);
mInfoTextView.setText(text);

//decode tile or fullsize image
bitmap = factory.decodePath(path2);
mImageView.setImageBitmap(bitmap);

bitmap = decodePath(String path, Options options) //decode tile

//destroy:
factory.nativeClose();

setupFd(),current is not avaliable
```

thanks TiffImage: https://github.com/m4coding/TiffImage, another decode tiff repo, update tiff to
4.0.5

# decode step
- setup and decode width,height,orientation,author,software,eg.don't decode bitmap.only writeDataToOptions()
- decode tile or full image: resolve option and decode bitmap tile.
not support multithread, because decode params are shared on decoder.

# TiffImage

decode tiff image for android

解码tiff图片，基于libtiff开源库

![](http://i.imgur.com/0mCC9jF.jpg)

### 支持 ###

本地路径读取，流方式的读取，Resource资源读取，assets资源读取

### 使用 ###

（1）本地路径读取

	TiffImage tiffImage = new TiffImage();

    Bitmap bitmap = tiffImage.decode(path);//path为绝对路径

	tiffImage.release();//释放资源

	imageView.setImageBitmap(bitmap);

（2）流方式的读取

	TiffImage tiffImage = new TiffImage();

	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

    Bitmap bitmap = tiffImage.decode(byteArrayInputStream);

	tiffImage.release();//释放资源

	imageView.setImageBitmap(bitmap);

（3）Resource资源读取

	TiffImage tiffImage = new TiffImage();

    Bitmap bitmap = tiffImage.decodeResource(getResources(),R.raw.test_res);

	tiffImage.release();//释放资源

	imageView.setImageBitmap(bitmap);

（4）assets资源读取

	TiffImage tiffImage = new TiffImage();

    InputStream inputStream = null;
    try {
        inputStream = getAssets().open("test_assets.tif");
        byte[] bytes= inputStream2Byte(inputStream);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Bitmap bitmap = tiffImage.decode(byteArrayInputStream);
    } catch (IOException e) {
        e.printStackTrace();
    }

	tiffImage.release();//释放资源

	imageView.setImageBitmap(bitmap);