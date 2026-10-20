package nl.captcha.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Fonts bundled inside the jar, so rendering doesn't depend on what is installed on the host.
 * Falls back to the JDK's logical fonts if none of the bundled ones can be loaded.
 */
public final class Fonts {

    private static final String[] BUNDLED = {
            "/fonts/DejaVuSans-Bold.ttf",
            "/fonts/DejaVuSerif-Bold.ttf",
            "/fonts/DejaVuSansMono-Bold.ttf",
            "/fonts/LiberationSans-Bold.ttf",
            "/fonts/LiberationSerif-Bold.ttf",
            "/fonts/LiberationMono-Bold.ttf",
    };

    private static final List<Font> FONTS = load();

    private Fonts() {
    }

    /**
     * @return the bundled fonts at size 1; call {@link Font#deriveFont(float)} for the size you need
     */
    public static List<Font> bundled() {
        return FONTS;
    }

    /**
     * @return the bundled fonts at the given size
     */
    public static List<Font> bundled(float size) {
        List<Font> sized = new ArrayList<>(FONTS.size());
        for (Font f : FONTS) {
            sized.add(f.deriveFont(size));
        }
        return sized;
    }

    /**
     * @return the bundled font with the given family name at the given size, or a logical
     * {@code fallback} font if it isn't bundled
     */
    public static Font named(String family, String fallback, float size) {
        for (Font f : FONTS) {
            if (f.getFamily().equalsIgnoreCase(family)) {
                return f.deriveFont(size);
            }
        }
        return new Font(fallback, Font.BOLD, Math.round(size));
    }

    private static List<Font> load() {
        List<Font> fonts = new ArrayList<>();
        for (String path : BUNDLED) {
            try (InputStream in = Fonts.class.getResourceAsStream(path)) {
                if (in != null) {
                    fonts.add(Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(Font.BOLD, 1f));
                }
            } catch (IOException | FontFormatException ignored) {
                // Skip a font that can't be read; the rest (or the fallback) still work.
            }
        }
        if (fonts.isEmpty()) {
            fonts.add(new Font(Font.SANS_SERIF, Font.BOLD, 1));
            fonts.add(new Font(Font.SERIF, Font.BOLD, 1));
            fonts.add(new Font(Font.MONOSPACED, Font.BOLD, 1));
        }
        return List.copyOf(fonts);
    }
}
