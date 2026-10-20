# SimpleCaptcha

A Java library for generating image and audio CAPTCHAs. Version 2 targets Java 21 and has no
runtime dependencies: the jhlabs image filters, fonts and audio samples are bundled in the jar.

## Quick start

```java
Captcha captcha = Captcha.create(240, 80, Difficulty.MEDIUM);

BufferedImage image = captcha.getImage();   // send to the client
String answer = captcha.getAnswer();        // keep on the server

captcha.isCorrectIgnoreCase(userInput);     // constant-time check
```

### Difficulty presets

| Preset   | Answer length | What it does |
|----------|---------------|--------------|
| `EASY`   | 5 | Slight rotation, one interference curve, light background clutter |
| `MEDIUM` | 6 | Overlapping glyphs, two interference curves, wave warp, decoy glyphs |
| `HARD`   | 6 | Stronger rotation and overlap, denser clutter and warp |

Each character is drawn in a randomly chosen bundled font (DejaVu or Liberation), colour,
rotation, scale and shear, with neighbouring characters overlapping so OCR can't easily split
the word into letters. Interference curves use the same colours and stroke weight as the text,
and the background carries faint decoy glyphs. Answers use a lowercase alphabet without
look-alike characters (`Difficulty.alphabet()`).

For numeric answers, pass your own text producer:

```java
Captcha captcha = Difficulty.HARD.builder(240, 80, new NumbersAnswerProducer(6)).build();
```

### Custom composition

The original builder is still available:

```java
Captcha captcha = new Captcha.Builder(200, 50)
        .addText()
        .addNoise()
        .gimp()
        .addBackground(new SquigglesBackgroundProducer())
        .addBorder()
        .build();
```

## Using it safely

No image CAPTCHA is unbreakable; modern vision models can read many of them. Pair it with
server-side limits:

- Store the answer server-side and allow exactly one verification attempt per CAPTCHA, whether
  the answer is right or wrong.
- Give each CAPTCHA a short lifetime.
- Rate-limit how many CAPTCHAs one client can request.

## Building

```bash
mvn package
```

This produces `target/simplecaptcha-2.0.0.jar`, which includes everything it needs.

## Changes in 2.0

- Maven build targeting Java 21; tests use JUnit 5.
- New `Difficulty` presets, `DistortedWordRenderer`, `WaveGimpyRenderer`,
  `InterferenceNoiseProducer` and `ClutterBackgroundProducer`.
- Fonts are bundled, so output no longer depends on the fonts installed on the server.
- `Captcha` is immutable and no longer `Serializable`; answers are compared in constant time.
- jhlabs filters updated to the `BufferedImageOp` API.
- The `javax.servlet` classes and the Ant build were removed.

## Licence

BSD 3-Clause, see `license.txt`. Bundled fonts are under their own licences in
`src/main/resources/fonts/`.
