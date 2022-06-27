package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImageCompare {

    private static final int rescaleSize     = 64;
    private static final int windowSize      = 8;
    private static final int windowsPerLine  = rescaleSize / windowSize;
    private static final int numWindows      = windowsPerLine * windowsPerLine;
    private static final int pixelsPerWindow = windowSize * windowSize;
    private static final int bytesPerPixel   = 3;

    private static final double k1           = 0.01; //Magic Number
    private static final double k2           = 0.03; //Magic Number
    private static final double dynamicRange = Math.pow(2f, bytesPerPixel) - 1;
    private static final double c1           = Math.pow(k1 * dynamicRange, 2);
    private static final double c2           = Math.pow(k2 * dynamicRange, 2);

    public static void main(String[] args) {

        System.out.println("loading images");
        long start = System.currentTimeMillis();
        final var files = IntStream.range(1, 3)
                                   .mapToObj(i -> String.format("C:\\tmp\\cmp\\%02d.jpg", i))
                                   .sorted()
//                                   .peek(System.out::println)
                                   .map(File::new).toList();

        final var imgs = files.stream().collect(Collectors.toMap(f -> f, ImageUtils::loadImage));
        long      stop = System.currentTimeMillis();
        System.out.println("loading " + files.size() + " images done. took " + (stop - start) + "ms");

        long       d     = 0;
        int        n     = 25;
        double[][] ssims = new double[0][];
        for (int t = 0; t < n; t++) {
            start = System.currentTimeMillis();
            ssims = new double[files.size()][files.size()];
            for (int i = 0; i < files.size(); i++) {
                for (int j = 0; j < files.size(); j++) {
                    BufferedImage img1 = imgs.get(files.get(i));
                    BufferedImage img2 = imgs.get(files.get(j));
                    ssims[i][j] = compareImagesSSIM(img1, img2, true);
                }
            }
            stop = System.currentTimeMillis();
            d += stop - start;
        }
        d /= n;

        System.out.println("Calculated similarities between " + files.size() * files.size() + " images. took " + d + "ms");

        printBoardPercent(ssims, .7);


    }

    private static void printBoardPercent(double[][] ssims, double threshold) {
        String h = "   | " + IntStream.range(1, ssims.length + 1).mapToObj(i -> String.format("%02d", i)).collect(Collectors.joining(" | ")) + " |";
        System.out.println(h);
        System.out.println("---+" + "----+".repeat(ssims.length));
        for (int i = 0; i < ssims.length; i++) {
            double[] ssimline = ssims[i];
            String s = String.format("%02d ", i + 1) + "|" + Arrays.stream(ssimline)
                                                                   .map(d -> Math.abs(d) < threshold ? 0 : d)
                                                                   .mapToInt(d -> (int) Math.round(d * 100))
                                                                   .mapToObj(d -> d == 0 ? "    " : String.format("%3d%%", d))
                                                                   .collect(Collectors.joining("|"));
            System.out.println(s);
        }
    }

    private static int[] resize(BufferedImage img) {
        BufferedImage r = new BufferedImage(rescaleSize, rescaleSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D    g = r.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(img, 0, 0, rescaleSize, rescaleSize, null);
        g.dispose();
        return ((DataBufferInt) r.getRaster().getDataBuffer()).getData();
    }

    private static void blur(int[] pxs, int res) {
        int[] blurred = new int[pxs.length];
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {

                int r = 0, b = 0, g = 0;
                for (int u = -1; u < 2; u++) {
                    int ny = y + u;
                    if (ny < 0 || ny >= res) continue;

                    for (int v = -1; v < 2; v++) {
                        int nx = x + v;

                        if (nx < 0 || nx >= res) continue;

                        int rgb = pxs[ny * res + nx];
                        r += (rgb >> 16) & 0xff;
                        g += (rgb >> 8) & 0xff;
                        b += rgb & 0xff;
                    }
                }
                r /= 9;
                g /= 9;
                b /= 9;

                blurred[y * res + x] = (r << 16) + (g << 8) + b;
            }
        }
        System.arraycopy(blurred, 0, pxs, 0, pxs.length);
    }

    public static double compareImagesSSIM(BufferedImage img1, BufferedImage img2, boolean blur) {

        int[] pxs1 = resize(img1);
        int[] pxs2 = resize(img2);

        if (blur) {
            blur(pxs1, rescaleSize);
        }

        double MSSIM = 0;

        for (int wy = 0; wy < windowsPerLine; wy++) {
            for (int wx = 0; wx < windowsPerLine; wx++) {
                double LAverage1   = 0;
                double LAverage2   = 0;
                double LVariance1  = 0;
                double LVariance2  = 0;
                double LCovariance = 0;

                for (int x = 0; x < windowSize; x++) {
                    for (int y = 0; y < windowSize; y++) {
                        int i    = (wy * windowSize + y) * windowSize + x;
                        int rgb1 = pxs1[i];
                        int rgb2 = pxs2[i];

                        // ITU-R-recommendation BT.601 for luma values.
                        double L1 = 0.299 * ((rgb1 >> 16) & 0xff) + 0.587 * ((rgb1 >> 8) & 0xff) + 0.114 * (rgb1 & 0xff);
                        double L2 = 0.299 * ((rgb2 >> 16) & 0xff) + 0.587 * ((rgb2 >> 8) & 0xff) + 0.114 * (rgb2 & 0xff);
                        L1 /= 255;
                        L2 /= 255;

                        LAverage1 += L1;
                        LAverage2 += L2;
                    }
                }

                LAverage1 /= pixelsPerWindow;
                LAverage2 /= pixelsPerWindow;

                for (int y = 0; y < windowSize; y++) {
                    for (int x = 0; x < windowSize; x++) {
                        int i    = (wy * windowSize + y) * windowSize + x;
                        int rgb1 = pxs1[i];
                        int rgb2 = pxs2[i];

                        // ITU-R-recommendation BT.601 for luma values.
                        double L1 = 0.299 * ((rgb1 >> 16) & 0xff) + 0.587 * ((rgb1 >> 8) & 0xff) + 0.114 * (rgb1 & 0xff);
                        double L2 = 0.299 * ((rgb2 >> 16) & 0xff) + 0.587 * ((rgb2 >> 8) & 0xff) + 0.114 * (rgb2 & 0xff);
                        L1 /= 255;
                        L2 /= 255;

                        LVariance1 += (L1 - LAverage1) * (L1 - LAverage1);
                        LVariance2 += (L2 - LAverage2) * (L2 - LAverage2);
                        LCovariance += (L1 - LAverage1) * (L2 - LAverage2);
                    }
                }

                LVariance1 /= pixelsPerWindow;
                LVariance2 /= pixelsPerWindow;
                LCovariance /= pixelsPerWindow;

                double numer = (2 * LAverage1 * LAverage2 + c1) * (2 * LCovariance + c2);
                double denom = (LAverage1 * LAverage1 + LAverage2 * LAverage2 + c1) * (LVariance1 + LVariance2 + c2);

                MSSIM += numer / denom;
            }
        }

        MSSIM /= numWindows;

        return MSSIM;
    }

}
