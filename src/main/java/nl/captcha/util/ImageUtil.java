package nl.captcha.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

public final class ImageUtil {

	private ImageUtil() {
	}

	/**
	 * Applies {@code op} to {@code img} and writes the result back into {@code img}, replacing its pixels.
	 */
	public static void applyFilter(BufferedImage img, BufferedImageOp op) {
		BufferedImage filtered = op.filter(img, null);
		Graphics2D g = img.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(filtered, 0, 0, null);
		g.dispose();
	}
}
