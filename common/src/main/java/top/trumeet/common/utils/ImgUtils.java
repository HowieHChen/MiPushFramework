package top.trumeet.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Code implements port from
 * https://www.cnblogs.com/Imageshop/p/3307308.html
 * AND
 * http://imagej.net/Auto_Threshold
 *
 * @author zts
 */
public class ImgUtils {


    private static int NUM_256 = 256;

    public static Bitmap trimImgToCircle(Bitmap bitmap, int color) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        trimImgToCircle(color, width, height, pixels, 0);

        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

        return newBmp;
    }

    public static void trimImgToCircle(int color, int width, int height, int[] pixels, int rExpand) {
        double r = Math.min(width, height) / 2.0 + rExpand;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double a = (i - width / 2.0);
                double b = (j - height / 2.0);
                if (a * a + b * b > r * r) {
                    pixels[width * i + j] = color;
                }

            }
        }
    }

    public static Bitmap convertToTransparentAndWhite(Bitmap bitmap) {

        int calculateThreshold = calculateThreshold(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int dot = pixels[width * i + j];
                int red = ((dot & 0x00FF0000) >> 16);
                int green = ((dot & 0x0000FF00) >> 8);
                int blue = (dot & 0x000000FF);
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);

                if (gray > calculateThreshold) {
                    pixels[width * i + j] = Color.TRANSPARENT;
                } else {
                    pixels[width * i + j] = Color.WHITE;
                }
            }
        }

        trimImgToCircle(Color.TRANSPARENT, width, height, pixels, 0);

        invertColorIfWhitePredominate(width, height, pixels);

        trimImgToCircle(Color.TRANSPARENT, width, height, pixels, 0);

        //todo use bwareaopen
        denoiseWhitePoint(width, height, pixels, 3);

        return cropTransparent(width, height, pixels);
    }

    @NonNull
    private static Bitmap cropTransparent(int width, int height, int[] pixels) {
        int topPadding = height;
        int leftPadding = width;
        int rightPadding = width;
        int bottomPadding = height;


        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                if (pixels[width * h + w] != Color.TRANSPARENT) {
                    topPadding = Math.min(topPadding, h);
                    leftPadding = Math.min(leftPadding, w);
                    rightPadding = Math.min(rightPadding, width - 1 - w);
                    bottomPadding = Math.min(bottomPadding, height - 1 - h);

                }
            }
        }

        int verticalPadding = bottomPadding + topPadding;
        int horizontalPadding = leftPadding + rightPadding;
        int diff = verticalPadding - horizontalPadding;
        if (verticalPadding > horizontalPadding) {
            bottomPadding -= (diff / 2);
            topPadding -= (diff / 2);

            bottomPadding = Math.max(bottomPadding, 0);
            topPadding = Math.max(topPadding, 0);

        } else if (verticalPadding < horizontalPadding) {
            leftPadding += (diff / 2);
            rightPadding += (diff / 2);
            leftPadding = Math.max(leftPadding, 0);
            rightPadding = Math.max(rightPadding, 0);
        }


        int cropHeight = height - bottomPadding - topPadding;
        int cropWidth = width - leftPadding - rightPadding;

        int padding = (cropHeight + cropWidth) / 16;

        int[] newPix = new int[cropWidth * cropHeight];

        int i = 0;
        for (int h = topPadding; h < topPadding + cropHeight; h++) {
            for (int w = leftPadding; w < leftPadding + cropWidth; w++) {
                newPix[i++] = pixels[width * h + w];
            }
        }

        Bitmap newBmp = Bitmap.createBitmap(cropWidth + padding * 2, cropHeight + padding * 2, Bitmap.Config.ARGB_8888);
        newBmp.setPixels(newPix, 0, cropWidth, padding, padding, cropWidth, cropHeight);

        return newBmp;
    }

    private static void invertColorIfWhitePredominate(int width, int height, int[] pixels) {
        int whiteCnt = 0;
        int tsCnt = 0;
        for (int color : pixels) {
            if (color == Color.TRANSPARENT) {
                tsCnt++;
            } else {
                whiteCnt++;
            }
        }

        if (whiteCnt > tsCnt) {
            //revert WHITE and TRANSPARENT
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int dot = pixels[width * i + j];
                    if (dot == Color.WHITE) {
                        pixels[width * i + j] = Color.TRANSPARENT;
                    } else {
                        pixels[width * i + j] = Color.WHITE;
                    }
                }
            }
        }
    }

    private static void denoiseWhitePoint(int width, int height, int[] pixels, int exThre) {
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                int[] dots = new int[]{
                        getPixel(width, pixels, i - 1, j - 1),
                        getPixel(width, pixels, i - 1, j),
                        getPixel(width, pixels, i - 1, j + 1),
                        getPixel(width, pixels, i, j - 1),
//                        pixels[width * i + j],
                        getPixel(width, pixels, i, j + 1),
                        getPixel(width, pixels, i + 1, j - 1),
                        getPixel(width, pixels, i + 1, j),
                        getPixel(width, pixels, i + 1, j + 1)};

                int whCnt = 0;
                int trCnt = 0;

                for (int dot : dots) {
                    if (dot == Color.WHITE) {
                        whCnt++;
                    } else {
                        trCnt++;
                    }
                }

                if (trCnt + exThre > dots.length) {
                    pixels[width * i + j] = Color.TRANSPARENT;
                }
            }
        }
    }

    private static int getPixel(int width, int[] pixels, int h, int w) {
        if (w >= width) {
            return Color.TRANSPARENT;
        }
        if (width * h + w >= pixels.length) {
            return Color.TRANSPARENT;
        }
        return pixels[width * h + w];
    }

    private static void getGreyHistogram(Bitmap bitmap, int[] histogram) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int dot = bitmap.getPixel(x, y);
                int red = ((dot & 0x00FF0000) >> 16);
                int green = ((dot & 0x0000FF00) >> 8);
                int blue = (dot & 0x000000FF);
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                histogram[gray]++;
            }
        }
    }

    private static boolean isDimodal(double[] histogram) {
        // 对直方图的峰进行计数，只有峰数位2才为双峰
        int count = 0;
        for (int i = 1; i < (NUM_256 - 1); i++) {
            if (histogram[i - 1] < histogram[i] && histogram[i + 1] < histogram[i]) {
                count++;
                if (count > 2) {
                    return false;
                }
            }
        }
        return count == 2;
    }


    private static int calculateThreshold(Bitmap bitmap) {

        bitmap = trimImgToCircle(bitmap, Color.WHITE);

        int[] histogram = new int[NUM_256];
        getGreyHistogram(bitmap, histogram);

        ArrayList<Integer> thresholds = new ArrayList<>();
        thresholds.add(calculateThresholdByOSTU(bitmap, histogram));
        thresholds.add(calculateThresholdByMinimum(histogram));
        thresholds.add(calculateThresholdByMean(histogram));

        Collections.sort(thresholds);

        return (thresholds.get(thresholds.size() - 1) * 3 + thresholds.get(thresholds.size() - 2)) / 4;
    }


    private static int calculateThresholdByOSTU(Bitmap bitmap, int[] histogram) {

        int total = bitmap.getWidth() * bitmap.getHeight();
        double sum = 0;
        for (int i = 0; i < NUM_256; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0;

        double varMax = 0;
        int threshold = 0;

        for (int i = 0; i < NUM_256; i++) {
            wB += histogram[i];
            if (wB == 0) {
                continue;
            }
            int wF = total - wB;

            if (wF == 0) {
                break;
            }

            sumB += (double) (i * histogram[i]);
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;

            double varBetween = (double) wB * (double) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;
    }

    private static int calculateThresholdByMinimum(int[] histogram) {

        int y, iter = 0;
        double[] histgramc = new double[NUM_256];
        double[] histgramcc = new double[NUM_256];
        for (y = 0; y < NUM_256; y++) {
            histgramc[y] = histogram[y];
            histgramcc[y] = histogram[y];
        }

        while (!isDimodal(histgramcc)) {
            histgramcc[0] = (histgramc[0] + histgramc[0] + histgramc[1]) / 3;
            for (y = 1; y < (NUM_256 - 1); y++) {
                histgramcc[y] = (histgramc[y - 1] + histgramc[y] + histgramc[y + 1]) / 3;
            }
            histgramcc[255] = (histgramc[254] + histgramc[255] + histgramc[255]) / 3;
            System.arraycopy(histgramcc, 0, histgramc, 0, NUM_256);
            iter++;
            if (iter >= 1000) {
                return -1;
            }
        }
        // 阈值极为两峰之间的最小值
        boolean peakFound = false;
        for (y = 1; y < (NUM_256 - 1); y++) {
            if (histgramcc[y - 1] < histgramcc[y] && histgramcc[y + 1] < histgramcc[y]) {
                peakFound = true;
            }
            if (peakFound && histgramcc[y - 1] >= histgramcc[y] && histgramcc[y + 1] >= histgramcc[y]) {
                return y - 1;
            }
        }
        return -1;
    }


    private static int calculateThresholdByMean(int[] histogram) {

        int sum = 0, amount = 0;
        for (int i = 0; i < NUM_256; i++) {
            amount += histogram[i];
            sum += i * histogram[i];
        }
        return sum / amount;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            int w = (drawable.getIntrinsicWidth() <= 0) ? 1 : drawable.getIntrinsicWidth();
            int h = (drawable.getIntrinsicHeight() <= 0) ? 1 : drawable.getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    public static Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        if (!bm.isRecycled()) {
            bm.recycle();
        }
        return newbm;
    }


}
