package nl.captcha;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import nl.captcha.backgrounds.BackgroundProducer;
import nl.captcha.backgrounds.TransparentBackgroundProducer;
import nl.captcha.gimpy.GimpyRenderer;
import nl.captcha.gimpy.RippleGimpyRenderer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.noise.NoiseProducer;
import nl.captcha.text.producer.DefaultTextProducer;
import nl.captcha.text.producer.TextProducer;
import nl.captcha.text.renderer.DefaultWordRenderer;
import nl.captcha.text.renderer.WordRenderer;

/**
 * A builder for generating a CAPTCHA image/answer pair.
 *
 * <p>
 * The quickest way to get a CAPTCHA that holds up against off-the-shelf OCR is a preset:
 * </p>
 * <pre>Captcha captcha = Captcha.create(240, 80, Difficulty.HARD);</pre>
 * <p>
 * For full control, compose one with the builder:
 * </p>
 * <pre>Captcha captcha = new Captcha.Builder(200, 50)
 * 	.addText()
 * 	.addNoise()
 * 	.addNoise()
 * 	.addBackground()
 * 	.build();</pre>
 * <p>Note that the <code>build()</code> must always be called last. Other methods are optional,
 * and can sometimes be repeated. Adding multiple backgrounds has no affect; the last background
 * added will simply be the one that is eventually rendered.</p>
 * <p>To validate that <code>answerStr</code> is a correct answer to the CAPTCHA:</p>
 *
 * <code>captcha.isCorrect(answerStr);</code>
 *
 * <p>Instances are immutable. Store the answer server-side (never send it to the client) and
 * allow a single verification attempt per CAPTCHA.</p>
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 *
 */
public final class Captcha {

    public static final String NAME = "simpleCaptcha";

    private final String _answer;
    private final BufferedImage _img;
    private final Instant _createdAt;

    private Captcha(Builder builder) {
        _answer = builder._answer;
        _img = builder._img;
        _createdAt = builder._createdAt;
    }

    /**
     * Generates a CAPTCHA using one of the built-in {@link Difficulty} presets.
     *
     * @param width  image width in pixels
     * @param height image height in pixels
     * @param difficulty how aggressively the text is distorted and obscured
     */
    public static Captcha create(int width, int height, Difficulty difficulty) {
        return difficulty.builder(width, height).build();
    }

    public static class Builder {
        private String _answer = "";
        private BufferedImage _img;
        private BufferedImage _bg;
        private Instant _createdAt;

        private boolean _addBorder = false;

        public Builder(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Width and height must be positive, got " + width + "x" + height);
            }
            _img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        /**
         * Add a background using the default {@link BackgroundProducer} (a {@link TransparentBackgroundProducer}).
         */
        public Builder addBackground() {
            return addBackground(new TransparentBackgroundProducer());
        }

        /**
         * Add a background using the given {@link BackgroundProducer}.
         *
         * @param bgProd
         */
        public Builder addBackground(BackgroundProducer bgProd) {
            _bg = bgProd.getBackground(_img.getWidth(), _img.getHeight());

            return this;
        }

        /**
         * Generate the answer to the CAPTCHA using the {@link DefaultTextProducer}.
         */
        public Builder addText() {
            return addText(new DefaultTextProducer());
        }

        /**
         * Generate the answer to the CAPTCHA using the given
         * {@link TextProducer}.
         *
         * @param txtProd
         */
        public Builder addText(TextProducer txtProd) {
            return addText(txtProd, new DefaultWordRenderer());
        }

        /**
         * Generate the answer to the CAPTCHA using the default
         * {@link TextProducer}, and render it to the image using the given
         * {@link WordRenderer}.
         *
         * @param wRenderer
         */
        public Builder addText(WordRenderer wRenderer) {
            return addText(new DefaultTextProducer(), wRenderer);
        }

        /**
         * Generate the answer to the CAPTCHA using the given
         * {@link TextProducer}, and render it to the image using the given
         * {@link WordRenderer}.
         *
         * @param txtProd
         * @param wRenderer
         */
        public Builder addText(TextProducer txtProd, WordRenderer wRenderer) {
            _answer += txtProd.getText();
            wRenderer.render(_answer, _img);

            return this;
        }

        /**
         * Add noise using the default {@link NoiseProducer} (a {@link CurvedLineNoiseProducer}).
         */
        public Builder addNoise() {
            return this.addNoise(new CurvedLineNoiseProducer());
        }

        /**
         * Add noise using the given NoiseProducer.
         *
         * @param nProd
         */
        public Builder addNoise(NoiseProducer nProd) {
            nProd.makeNoise(_img);
            return this;
        }

        /**
         * Gimp the image using the default {@link GimpyRenderer} (a {@link RippleGimpyRenderer}).
         */
        public Builder gimp() {
            return gimp(new RippleGimpyRenderer());
        }

        /**
         * Gimp the image using the given {@link GimpyRenderer}.
         *
         * @param gimpy
         */
        public Builder gimp(GimpyRenderer gimpy) {
            gimpy.gimp(_img);
            return this;
        }

        /**
         * Draw a single-pixel wide black border around the image.
         */
        public Builder addBorder() {
            _addBorder = true;

            return this;
        }

        /**
         * Build the CAPTCHA. This method should always be called, and should always
         * be called last.
         */
        public Captcha build() {
            if (_bg == null) {
                _bg = new TransparentBackgroundProducer().getBackground(_img.getWidth(), _img.getHeight());
            }

            // Paint the main image over the background
            Graphics2D g = _bg.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g.drawImage(_img, null, null);

            if (_addBorder) {
                int width = _img.getWidth();
                int height = _img.getHeight();

                g.setColor(Color.BLACK);
                g.drawLine(0, 0, 0, width);
                g.drawLine(0, 0, width, 0);
                g.drawLine(0, height - 1, width, height - 1);
                g.drawLine(width - 1, height - 1, width - 1, 0);
            }
            g.dispose();

            _img = _bg;

            _createdAt = Instant.now();

            return new Captcha(this);
        }

        @Override
        public String toString() {
            return "[Answer: " + _answer + "][Timestamp: " + _createdAt + "][Image: " + _img + "]";
        }
    }

    /**
     * Checks an answer, case-sensitively, in constant time with respect to its content.
     */
    public boolean isCorrect(String answer) {
        if (answer == null) {
            return false;
        }
        return MessageDigest.isEqual(
                answer.getBytes(StandardCharsets.UTF_8),
                _answer.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Checks an answer ignoring case and surrounding whitespace, in constant time with respect to its content.
     */
    public boolean isCorrectIgnoreCase(String answer) {
        if (answer == null) {
            return false;
        }
        return MessageDigest.isEqual(
                answer.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8),
                _answer.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    public String getAnswer() {
        return _answer;
    }

    public BufferedImage getImage() {
        return _img;
    }

    public Instant getCreatedAt() {
        return _createdAt;
    }

    /**
     * @deprecated use {@link #getCreatedAt()}
     */
    @Deprecated
    public Date getTimeStamp() {
        return Date.from(_createdAt);
    }

    @Override
    public String toString() {
        return "[Answer: " + _answer + "][Timestamp: " + _createdAt + "][Image: " + _img + "]";
    }
}
