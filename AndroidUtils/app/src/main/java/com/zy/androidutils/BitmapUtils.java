package com.zy.androidutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.text.format.DateFormat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

public class BitmapUtils {
    private final static int MAX_NUM_PIXELS = 320 * 490;
    private final static int MIN_SIDE_LENGTH = 350;
    /**
     * Bitmap 转换成 byte[]
     * @param bm
     * @return
     */
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
    /**
     * 保存图片
     *
     * @param path
     * @param bitmap
     */
    public static void saveBitmap(String path, Bitmap bitmap) {
        try {
            File f = new File(path);
            if (f.exists())
                f.delete();
            f.createNewFile();
            FileOutputStream fOut = null;
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }
    /**
     * 根据Url返回图片
     *
     * @param imageUrl
     * @return
     */
    public static Bitmap loadImageFromUrl2Bitmap(String imageUrl) {
        Bitmap bitmap = null;
        if (imageUrl == null)
            return null;
        try {
            InputStream is = new URL(imageUrl).openStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 防止Bitmap内存溢出
     *
     * @param filepath
     * @param height
     *            要缩放的高度
     * @return
     */
    public static Drawable getPhotoItem(String filepath, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, options); // 此时返回bm为空
        options.inJustDecodeBounds = false;
        // 计算缩放比
        int rate = (int) (options.outHeight / (float) height);
        if (rate <= 0)
            rate = 1;
        options.inSampleSize = rate;
        // 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(filepath, options);
        return new BitmapDrawable(bitmap);
    }

    /**
     * 加载本地图片
     *
     * @param filePath
     * @return
     */
    public static Bitmap getBitmap(String filePath) {
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        return bm;
    }

    /**
     * 加载本地图片
     *
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        if (bm == null) {
            return null;
        }
        int degree = readPictureDegree(filePath);
        bm = rotateBitmap(bm, degree);
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            Bitmap.CompressFormat format = null;
            if (filePath.toLowerCase().endsWith("jpg") || filePath.toLowerCase().endsWith("jpeg")) {
                format = Bitmap.CompressFormat.JPEG;
            } else if (filePath.toLowerCase().endsWith("png")) {
                format = Bitmap.CompressFormat.PNG;
            } else {
                format = Bitmap.CompressFormat.JPEG;
            }
            bm.compress(format, 100, baos);
        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null)
            return null;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
    /**
     * 计算inSampleSize值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = Math.round((float) height / (float) reqHeight);
            int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }
    /**
     * 图片本身加上圆角
     * @return
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, boolean isSidesRound, int r) {
        int sideLength = bitmap.getWidth();
        if (bitmap.getHeight() > sideLength) {
            sideLength = bitmap.getHeight();
        }

        Rect rectRound = new Rect(0, 0, sideLength, sideLength);
        Rect rectSrc = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect rectDest = new Rect((sideLength - bitmap.getWidth()) / 2, (sideLength - bitmap.getHeight()) / 2,
                ((sideLength - bitmap.getWidth()) / 2) + bitmap.getWidth(), ((sideLength - bitmap.getHeight()) / 2) + bitmap.getHeight());

        Bitmap output = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xffffffff;
        Paint paint = new Paint();
        float roundPx = 0;
        if (isSidesRound) {
            roundPx = sideLength / 2;
        } else {
            roundPx = r / 2;
        }
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(rectRound), roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rectSrc, rectDest, paint);
        return output;
    }

    /**
     * Bitmap转成byte 可能所占空间会变大
     *
     * @param bm
     * @return
     */
    public static byte[] BitmapToBytes(Bitmap bm) {
        return BitmapToBytes(bm, "png");
    }

    /**
     * Bitmap转成byte 可能所占空间会变大
     *
     * @param bm
     * @param suffix
     *            图片后缀
     * @return
     */
    public static byte[] BitmapToBytes(Bitmap bm, String suffix) {
        return BitmapToBytes(bm, suffix, 80);
    }

    /**
     * Bitmap转成byte
     *
     * @param bm
     * @param suffix
     * @param quality
     *            图片质量 0~100
     * @return
     */
    public static byte[] BitmapToBytes(Bitmap bm, String suffix, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap.CompressFormat format = null;
        if (suffix == null || (suffix != null && suffix.length() == 0)) {
            format = Bitmap.CompressFormat.JPEG;
        } else if (suffix.toLowerCase().endsWith("jpg") || suffix.toLowerCase().endsWith("jpeg")) {
            format = Bitmap.CompressFormat.JPEG;
        } else if (suffix.toLowerCase().endsWith("png")) {
            format = Bitmap.CompressFormat.PNG;
        } else {
            format = Bitmap.CompressFormat.JPEG;
        }
        bm.compress(format, quality, baos);
        return baos.toByteArray();
    }
    /**
     * 给图片命名
     */
    public static String getPicName() {
        new DateFormat();
        return DateFormat.format("yyyyMMddhhmmss",
                Calendar.getInstance(Locale.CHINA))
                + ".png";
    }
    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotatingImageView(int angle, Bitmap bitmap) {
        if (null == bitmap) {
            return null;
        }
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return newBitmap;
    }

    /**
     *
     * @Description 生成图片的压缩图
     * @param filePath
     * @return
     */
    public static Bitmap createImageThumbnail(String filePath) {
        if (null == filePath || !new File(filePath).exists())
            return null;
        Bitmap bitmap = null;
        int degree = readPictureDegree(filePath);
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, MAX_NUM_PIXELS);
            opts.inJustDecodeBounds = false;
            if (opts.inSampleSize == 1) {
                bitmap = BitmapFactory.decodeFile(filePath);

            } else {
                bitmap = BitmapFactory.decodeFile(filePath, opts);
            }
        } catch (Exception e) {
            return null;
        }
        Bitmap newBitmap = rotatingImageView(degree, bitmap);
        return newBitmap;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path
     *            图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? MIN_SIDE_LENGTH : (int) Math
                .min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);// 压缩位图
            byte[] bytes = baos.toByteArray();// 创建分配字节数组
            return bytes;
        } catch (Exception e) {
            return null;
        } finally {
            if (null != baos) {
                try {
                    baos.flush();
                    baos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * @Description 上传服务器前调用该方法进行压缩
     * @param path
     * @return
     * @throws IOException
     */
    public static Bitmap revisionImage(String path) throws IOException {
        if (null == path || TextUtils.isEmpty(path) || !new File(path).exists())
            return null;
        BufferedInputStream in = null;
        try {
            int degree = readPictureDegree(path);
            in = new BufferedInputStream(new FileInputStream(new File(path)));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // options.
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            int i = 0;
            Bitmap bitmap = null;
            while (true) {
                if ((options.outWidth >> i <= 1000)
                        && (options.outHeight >> i <= 1000)) {
                    in = new BufferedInputStream(new FileInputStream(new File(
                            path)));
                    options.inSampleSize = (int) Math.pow(2.0D, i);
                    // int height = options.outHeight * 100 / options.outWidth;
                    // options.outWidth = 100;
                    // options.outHeight = height;
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(in, null, options);
                    break;
                }
                i += 1;
            }
            Bitmap newBitmap = rotatingImageView(degree, bitmap);
            return newBitmap;
        } catch (Exception e) {
            return null;
        } finally {
            if (null != in) {
                in.close();
                in = null;
            }
        }
    }
}
