package utils;

import lombok.extern.slf4j.Slf4j;
import me.saharnooby.qoi.QOIImage;
import me.saharnooby.qoi.QOIUtil;
import me.saharnooby.qoi.QOIUtilAWT;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Slf4j
public class ImageUtils {

    public static boolean writeImage(BufferedImage img, String type, OutputStream os) {
        try {
            ImageIO.write(img, type, os);
            return true;
        } catch (IOException e) {
            log.warn("unable to write image");
        }
        return false;
    }

    public static boolean writeImage(BufferedImage img, OutputStream os) {
        try {
            QOIImage qui = QOIUtilAWT.createFromBufferedImage(img);
            QOIUtil.writeImage(qui, os);
            return true;
        } catch (IOException e) {
            log.warn("unable to write image");
        }
        return false;
    }

    public static BufferedImage resize(BufferedImage img, int res) {
        int type = img.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : img.getType();
        return resize(img, res, type);
    }

    public static BufferedImage resize(BufferedImage img, int res, int colorType) {

        BufferedImage re;

        int   type  = img.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : img.getType();
        float ratio = (float) img.getWidth() / (float) img.getHeight();

        if (img.getWidth() > img.getHeight()) {
            re = new BufferedImage(res, (int) ((float) res / ratio), colorType);
        } else {
            re = new BufferedImage((int) ((float) res * ratio), res, colorType);
        }

        Graphics2D graphics = re.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.drawImage(img, 0, 0, re.getWidth(), re.getHeight(), null);
        graphics.dispose();

        return re;

    }

    public static BufferedImage loadImage(InputStream is) {
        try {
            BufferedImage read = ImageIO.read(is);
            if (read == null) {
                log.warn("can't load file");
            }
            return read;
        } catch (IOException e) {
            log.warn("can't load file:" + e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage loadImage(byte[] bytes) {
        return loadImage(new ByteArrayInputStream(bytes));
    }

    public static BufferedImage loadImage(File file) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            BufferedImage read = loadImage(bis);
            if (read == null) {
                log.warn("can't load file '" + file.getAbsolutePath() + "'");
            }
            return read;
        } catch (IOException e) {
            log.warn("can't load file '" + file.getAbsolutePath() + "' :" + e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

}
