package nl.captcha.backgrounds;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import nl.captcha.util.Fonts;

/**
 * A light, randomly coloured gradient covered with faint arcs, specks and decoy glyph
 * fragments. The decoys look like text to OCR but are low-contrast enough for people to
 * read past.
 */
public class ClutterBackgroundProducer implements BackgroundProducer {

    private static final Random RAND = new SecureRandom();
    private static final char[] DECOY_CHARS = "abcdefghkmnprwxyABCDEFGHKMNPRWXY2345678".toCharArray();

    private final int _arcs;
    private final int _decoys;
    private final List<Font> _fonts;

    public ClutterBackgroundProducer() {
        this(12, 4);
    }

    /**
     * @param arcs number of faint arcs
     * @param decoys number of faint decoy glyphs
     */
    public ClutterBackgroundProducer(int arcs, int decoys) {
        _arcs = arcs;
        _decoys = decoys;
        _fonts = Fonts.bundled();
    }

    @Override
    public BufferedImage addBackground(BufferedImage image) {
        return getBackground(image.getWidth(), image.getHeight());
    }

    @Override
    public BufferedImage getBackground(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setPaint(new GradientPaint(0, 0, lightColor(), width, height, lightColor()));
        g.fillRect(0, 0, width, height);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        for (int i = 0; i < _arcs; i++) {
            g.setColor(midColor());
            g.setStroke(new BasicStroke(1f + RAND.nextFloat() * 2f));
            double size = height * (0.4 + RAND.nextDouble() * 1.2);
            g.draw(new Arc2D.Double(RAND.nextDouble() * width - size / 2, RAND.nextDouble() * height - size / 2,
                    size, size, RAND.nextDouble() * 360, 60 + RAND.nextDouble() * 200, Arc2D.OPEN));
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
        AffineTransform base = g.getTransform();
        for (int i = 0; i < _decoys; i++) {
            g.setColor(midColor());
            Font font = _fonts.get(RAND.nextInt(_fonts.size())).deriveFont((float) (height * (0.4 + RAND.nextDouble() * 0.4)));
            g.setFont(font);
            g.rotate(Math.toRadians(RAND.nextDouble() * 60 - 30), width / 2.0, height / 2.0);
            g.drawString(String.valueOf(DECOY_CHARS[RAND.nextInt(DECOY_CHARS.length)]),
                    (float) (RAND.nextDouble() * width), (float) (height * (0.4 + RAND.nextDouble() * 0.6)));
            g.setTransform(base);
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        int specks = width * height / 60;
        for (int i = 0; i < specks; i++) {
            g.setColor(midColor());
            g.fillRect(RAND.nextInt(width), RAND.nextInt(height), 1, 1);
        }

        g.dispose();
        return img;
    }

    private static Color lightColor() {
        return Color.getHSBColor(RAND.nextFloat(), 0.08f + RAND.nextFloat() * 0.12f, 0.92f + RAND.nextFloat() * 0.08f);
    }

    private static Color midColor() {
        return Color.getHSBColor(RAND.nextFloat(), 0.25f + RAND.nextFloat() * 0.3f, 0.45f + RAND.nextFloat() * 0.3f);
    }
}
