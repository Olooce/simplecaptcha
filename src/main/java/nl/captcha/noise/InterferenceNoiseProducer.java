package nl.captcha.noise;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import nl.captcha.text.renderer.DistortedWordRenderer;

/**
 * Draws curves and specks in the same colours as the text, crossing through the text band.
 * Because they share the text's colours and stroke weight, they can't be removed with a
 * simple colour or thickness filter.
 */
public class InterferenceNoiseProducer implements NoiseProducer {

    private static final Random RAND = new SecureRandom();

    private final List<Color> _colors;
    private final int _curves;
    private final int _specksPer1000Px;

    public InterferenceNoiseProducer() {
        this(2, 4);
    }

    /**
     * @param curves number of curves drawn across the image
     * @param specksPer1000Px density of small specks, per 1000 pixels of image area
     */
    public InterferenceNoiseProducer(int curves, int specksPer1000Px) {
        this(DistortedWordRenderer.DEFAULT_COLORS, curves, specksPer1000Px);
    }

    public InterferenceNoiseProducer(List<Color> colors, int curves, int specksPer1000Px) {
        if (colors.isEmpty()) {
            throw new IllegalArgumentException("At least one colour is required");
        }
        _colors = List.copyOf(colors);
        _curves = curves;
        _specksPer1000Px = specksPer1000Px;
    }

    @Override
    public void makeNoise(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < _curves; i++) {
            g.setColor(_colors.get(RAND.nextInt(_colors.size())));
            // The first curve is as heavy as a glyph stroke so it can't be filtered by thickness; the rest stay thin.
            double weight = i == 0 ? 0.03 + RAND.nextDouble() * 0.015 : 0.012 + RAND.nextDouble() * 0.01;
            g.setStroke(new BasicStroke((float) (h * weight), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new CubicCurve2D.Double(
                    -w * 0.05, band(h),
                    w * (0.2 + RAND.nextDouble() * 0.2), RAND.nextDouble() * h,
                    w * (0.6 + RAND.nextDouble() * 0.2), RAND.nextDouble() * h,
                    w * 1.05, band(h)));
        }

        int specks = (int) ((long) w * h * _specksPer1000Px / 1000);
        for (int i = 0; i < specks; i++) {
            g.setColor(_colors.get(RAND.nextInt(_colors.size())));
            int r = 1 + RAND.nextInt(2);
            g.fillOval(RAND.nextInt(w), RAND.nextInt(h), r, r);
        }
        g.dispose();
    }

    private static double band(int h) {
        return h * (0.25 + RAND.nextDouble() * 0.5);
    }
}
