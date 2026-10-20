package nl.captcha.text.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.captcha.util.Fonts;

/**
 * Renders each character as a separately transformed glyph outline: its own font, colour,
 * rotation, scale, shear and vertical offset. Neighbouring glyphs overlap, which makes it
 * hard for OCR to split the word into individual characters.
 */
public class DistortedWordRenderer implements WordRenderer {

    private static final Random RAND = new SecureRandom();

    public static final List<Color> DEFAULT_COLORS = List.of(
            new Color(0x1B2A49), new Color(0x7A1F1F), new Color(0x1F5A2E),
            new Color(0x4B2366), new Color(0x2B2B2B), new Color(0x0F4C5C));

    private final List<Color> _colors;
    private final List<Font> _fonts;
    private final double _maxRotationDegrees;
    private final double _overlap;
    private final double _verticalJitter;

    public DistortedWordRenderer() {
        this(25, 0.18, 0.12);
    }

    /**
     * @param maxRotationDegrees each glyph is rotated by a random angle in [-max, max]
     * @param overlap fraction of each glyph's width that the next glyph overlaps, 0 to 0.5
     * @param verticalJitter maximum vertical offset of each glyph, as a fraction of the image height
     */
    public DistortedWordRenderer(double maxRotationDegrees, double overlap, double verticalJitter) {
        this(DEFAULT_COLORS, Fonts.bundled(), maxRotationDegrees, overlap, verticalJitter);
    }

    public DistortedWordRenderer(List<Color> colors, List<Font> fonts,
                                 double maxRotationDegrees, double overlap, double verticalJitter) {
        if (colors.isEmpty() || fonts.isEmpty()) {
            throw new IllegalArgumentException("At least one colour and one font are required");
        }
        _colors = List.copyOf(colors);
        _fonts = List.copyOf(fonts);
        _maxRotationDegrees = maxRotationDegrees;
        _overlap = Math.max(0, Math.min(0.5, overlap));
        _verticalJitter = verticalJitter;
    }

    @Override
    public void render(final String word, BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        FontRenderContext frc = g.getFontRenderContext();

        List<Shape> glyphs = new ArrayList<>(word.length());
        List<Color> colors = new ArrayList<>(word.length());
        double cursor = 0;
        double baseline = height * 0.5;

        for (char c : word.toCharArray()) {
            float size = (float) (height * (0.55 + RAND.nextDouble() * 0.2));
            Font font = _fonts.get(RAND.nextInt(_fonts.size())).deriveFont(size);
            Shape outline = font.createGlyphVector(frc, String.valueOf(c)).getOutline();
            Rectangle2D b = outline.getBounds2D();

            AffineTransform t = new AffineTransform();
            double y = baseline + (RAND.nextDouble() * 2 - 1) * _verticalJitter * height;
            t.translate(cursor + b.getWidth() / 2, y);
            t.rotate(Math.toRadians((RAND.nextDouble() * 2 - 1) * _maxRotationDegrees));
            t.shear((RAND.nextDouble() * 2 - 1) * 0.25, 0);
            t.scale(0.85 + RAND.nextDouble() * 0.3, 0.9 + RAND.nextDouble() * 0.2);
            t.translate(-b.getCenterX(), -b.getCenterY());

            Shape placed = t.createTransformedShape(outline);
            glyphs.add(placed);
            colors.add(_colors.get(RAND.nextInt(_colors.size())));
            cursor += b.getWidth() * (1 - _overlap);
        }

        if (glyphs.isEmpty()) {
            g.dispose();
            return;
        }

        Rectangle2D all = glyphs.get(0).getBounds2D();
        for (Shape s : glyphs) {
            all = all.createUnion(s.getBounds2D());
        }

        // Fit the word inside the image with a margin (left for any later warping), then place it at a random offset.
        double scale = Math.min(width * 0.88 / all.getWidth(), height * 0.72 / all.getHeight());
        scale = Math.min(scale, 1.0);
        double fittedWidth = all.getWidth() * scale;
        double fittedHeight = all.getHeight() * scale;
        double offsetX = (width - fittedWidth) * (0.15 + RAND.nextDouble() * 0.7);
        double offsetY = (height - fittedHeight) * (0.4 + RAND.nextDouble() * 0.2);

        AffineTransform fit = new AffineTransform();
        fit.translate(offsetX, offsetY);
        fit.scale(scale, scale);
        fit.translate(-all.getX(), -all.getY());

        for (int i = 0; i < glyphs.size(); i++) {
            g.setColor(colors.get(i));
            g.fill(fit.createTransformedShape(glyphs.get(i)));
        }
        g.dispose();
    }
}
