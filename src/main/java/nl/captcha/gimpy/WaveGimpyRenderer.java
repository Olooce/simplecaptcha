package nl.captcha.gimpy;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Warps the image with two overlapping sine waves on each axis. Wavelengths and phases are
 * randomised on every call, so no two images share the same distortion.
 */
public class WaveGimpyRenderer implements GimpyRenderer {

    private static final Random RAND = new SecureRandom();

    private final double _strength;

    public WaveGimpyRenderer() {
        this(1.0);
    }

    /**
     * @param strength scales the wave amplitude; 1.0 is a moderate warp, 0 disables it
     */
    public WaveGimpyRenderer(double strength) {
        _strength = Math.max(0, strength);
    }

    @Override
    public void gimp(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] src = image.getRGB(0, 0, w, h, null, 0, w);
        int[] dst = new int[src.length];

        double ax1 = h * 0.06 * _strength * (0.7 + RAND.nextDouble() * 0.6);
        double ax2 = h * 0.03 * _strength * (0.7 + RAND.nextDouble() * 0.6);
        double ay1 = h * 0.08 * _strength * (0.7 + RAND.nextDouble() * 0.6);
        double ay2 = h * 0.03 * _strength * (0.7 + RAND.nextDouble() * 0.6);
        double lx1 = h * (0.6 + RAND.nextDouble() * 0.6);
        double lx2 = w * (0.15 + RAND.nextDouble() * 0.2);
        double ly1 = w * (0.3 + RAND.nextDouble() * 0.4);
        double ly2 = h * (0.25 + RAND.nextDouble() * 0.3);
        double p1 = RAND.nextDouble() * Math.PI * 2;
        double p2 = RAND.nextDouble() * Math.PI * 2;
        double p3 = RAND.nextDouble() * Math.PI * 2;
        double p4 = RAND.nextDouble() * Math.PI * 2;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double sx = x + ax1 * Math.sin(2 * Math.PI * y / lx1 + p1) + ax2 * Math.sin(2 * Math.PI * x / lx2 + p2);
                double sy = y + ay1 * Math.sin(2 * Math.PI * x / ly1 + p3) + ay2 * Math.sin(2 * Math.PI * y / ly2 + p4);
                dst[y * w + x] = sample(src, w, h, sx, sy);
            }
        }
        image.setRGB(0, 0, w, h, dst, 0, w);
    }

    private static int sample(int[] px, int w, int h, double x, double y) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        double fx = x - x0;
        double fy = y - y0;
        int c00 = pixel(px, w, h, x0, y0);
        int c10 = pixel(px, w, h, x0 + 1, y0);
        int c01 = pixel(px, w, h, x0, y0 + 1);
        int c11 = pixel(px, w, h, x0 + 1, y0 + 1);

        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            double top = ((c00 >>> shift) & 0xFF) * (1 - fx) + ((c10 >>> shift) & 0xFF) * fx;
            double bottom = ((c01 >>> shift) & 0xFF) * (1 - fx) + ((c11 >>> shift) & 0xFF) * fx;
            int v = (int) Math.round(top * (1 - fy) + bottom * fy);
            out |= (v & 0xFF) << shift;
        }
        return out;
    }

    private static int pixel(int[] px, int w, int h, int x, int y) {
        if (x < 0 || y < 0 || x >= w || y >= h) {
            return 0;
        }
        return px[y * w + x];
    }
}
