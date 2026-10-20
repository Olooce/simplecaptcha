package nl.captcha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CaptchaTest {

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void presetProducesImageOfRequestedSizeAndAnswerLength(Difficulty difficulty) {
        Captcha captcha = Captcha.create(240, 80, difficulty);

        assertEquals(240, captcha.getImage().getWidth());
        assertEquals(80, captcha.getImage().getHeight());
        assertEquals(difficulty.getLength(), captcha.getAnswer().length());
        assertTrue(captcha.getAnswer().matches("[a-z2-8]+"));
        assertNotNull(captcha.getCreatedAt());
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void presetDrawsTextOverTheBackground(Difficulty difficulty) {
        BufferedImage img = Captcha.create(240, 80, difficulty).getImage();

        int dark = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int luma = (((rgb >> 16) & 0xFF) * 299 + ((rgb >> 8) & 0xFF) * 587 + (rgb & 0xFF) * 114) / 1000;
                if (luma < 90) dark++;
            }
        }
        assertTrue(dark > img.getWidth() * img.getHeight() / 50, "expected visible text, found " + dark + " dark pixels");
    }

    @Test
    void answersAreRandom() {
        Set<String> answers = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            answers.add(Captcha.create(160, 50, Difficulty.EASY).getAnswer());
        }
        assertTrue(answers.size() > 45);
    }

    @Test
    void isCorrectIsExactAndNullSafe() {
        Captcha captcha = Captcha.create(200, 60, Difficulty.MEDIUM);
        String answer = captcha.getAnswer();

        assertTrue(captcha.isCorrect(answer));
        assertFalse(captcha.isCorrect(answer.toUpperCase()));
        assertFalse(captcha.isCorrect(answer + "x"));
        assertFalse(captcha.isCorrect(""));
        assertFalse(captcha.isCorrect(null));
    }

    @Test
    void isCorrectIgnoreCaseTrimsAndIgnoresCase() {
        Captcha captcha = Captcha.create(200, 60, Difficulty.MEDIUM);
        String answer = captcha.getAnswer();

        assertTrue(captcha.isCorrectIgnoreCase("  " + answer.toUpperCase() + " "));
        assertFalse(captcha.isCorrectIgnoreCase(answer.substring(1)));
        assertFalse(captcha.isCorrectIgnoreCase(null));
    }

    @Test
    void classicBuilderStillWorks() {
        Captcha captcha = new Captcha.Builder(200, 50)
                .addText()
                .addNoise()
                .gimp()
                .addBackground()
                .addBorder()
                .build();

        assertEquals(5, captcha.getAnswer().length());
        assertEquals(200, captcha.getImage().getWidth());
    }

    @Test
    void imagesDifferForTheSameDifficulty() {
        BufferedImage a = Captcha.create(120, 40, Difficulty.HARD).getImage();
        BufferedImage b = Captcha.create(120, 40, Difficulty.HARD).getImage();

        assertNotEquals(a.getRGB(60, 20) + "" + a.getRGB(10, 10), b.getRGB(60, 20) + "" + b.getRGB(10, 10));
    }

    @Test
    void rejectsNonPositiveSize() {
        assertThrows(IllegalArgumentException.class, () -> new Captcha.Builder(0, 50));
    }
}
