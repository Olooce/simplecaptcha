package nl.captcha.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;

import org.junit.jupiter.api.Test;

class FontsTest {

    @Test
    void loadsAllBundledFonts() {
        assertEquals(6, Fonts.bundled().size());
        assertTrue(Fonts.bundled().stream().anyMatch(f -> f.getFamily().startsWith("DejaVu")));
        assertTrue(Fonts.bundled().stream().anyMatch(f -> f.getFamily().startsWith("Liberation")));
    }

    @Test
    void namedFindsBundledFontOrFallsBack() {
        assertEquals("Liberation Sans", Fonts.named("Liberation Sans", Font.SANS_SERIF, 40).getFamily());
        assertEquals(Font.SANS_SERIF, Fonts.named("No Such Font", Font.SANS_SERIF, 40).getName());
    }
}
