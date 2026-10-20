package nl.captcha;

import nl.captcha.backgrounds.ClutterBackgroundProducer;
import nl.captcha.gimpy.WaveGimpyRenderer;
import nl.captcha.noise.InterferenceNoiseProducer;
import nl.captcha.text.producer.DefaultTextProducer;
import nl.captcha.text.producer.TextProducer;
import nl.captcha.text.renderer.DistortedWordRenderer;

/**
 * Ready-made combinations of text length, glyph distortion, interference and background
 * clutter. Answers use a lowercase alphabet without look-alike characters, so they can be
 * checked with {@link Captcha#isCorrectIgnoreCase(String)}.
 */
public enum Difficulty {

    EASY(5, 15, 0.08, 0.08, 1, 2, 6, 1, 0.6),
    MEDIUM(6, 22, 0.14, 0.1, 2, 3, 10, 2, 0.85),
    HARD(6, 26, 0.17, 0.12, 2, 5, 14, 4, 1.0);

    /** Lowercase letters and digits that stay distinguishable when rotated and overlapped. */
    private static final char[] ALPHABET = "abcdefhkmnprwxy2345678".toCharArray();

    private final int _length;
    private final double _rotation;
    private final double _overlap;
    private final double _verticalJitter;
    private final int _curves;
    private final int _specks;
    private final int _arcs;
    private final int _decoys;
    private final double _wave;

    Difficulty(int length, double rotation, double overlap, double verticalJitter,
               int curves, int specks, int arcs, int decoys, double wave) {
        _length = length;
        _rotation = rotation;
        _overlap = overlap;
        _verticalJitter = verticalJitter;
        _curves = curves;
        _specks = specks;
        _arcs = arcs;
        _decoys = decoys;
        _wave = wave;
    }

    public int getLength() {
        return _length;
    }

    /**
     * @return a copy of the alphabet the presets draw answers from
     */
    public static char[] alphabet() {
        return ALPHABET.clone();
    }

    /**
     * @return a builder already configured for this difficulty; add a border or extra noise if wanted, then call build()
     */
    public Captcha.Builder builder(int width, int height) {
        return builder(width, height, new DefaultTextProducer(_length, ALPHABET));
    }

    /**
     * Same as {@link #builder(int, int)}, but with your own answer source, for example
     * {@link nl.captcha.text.producer.NumbersAnswerProducer} for numeric answers.
     */
    public Captcha.Builder builder(int width, int height, TextProducer text) {
        return new Captcha.Builder(width, height)
                .addBackground(new ClutterBackgroundProducer(_arcs, _decoys))
                .addText(text, new DistortedWordRenderer(_rotation, _overlap, _verticalJitter))
                .addNoise(new InterferenceNoiseProducer(_curves, _specks))
                .gimp(new WaveGimpyRenderer(_wave));
    }
}
