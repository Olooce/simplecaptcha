package nl.captcha.text.producer;

import static nl.captcha.text.producer.ChineseTextProducer.CODE_POINT_END;
import static nl.captcha.text.producer.ChineseTextProducer.CODE_POINT_START;
import static nl.captcha.text.producer.ChineseTextProducer.DEFAULT_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChineseTextProducerTest {

	@Test
	void defaultConstructorProducesCharactersInRange() {
		ChineseTextProducer cProd = new ChineseTextProducer();
		String ans = cProd.getText();
		assertEquals(DEFAULT_LENGTH, ans.length());
		for (int i = 0; i < ans.length(); i++) {
			char c = ans.charAt(i);
			assertTrue(c >= CODE_POINT_START && c <= CODE_POINT_END,
					"Character was '" + c + "' (" + (int) c + "), should be between "
							+ CODE_POINT_START + " and " + CODE_POINT_END);
		}
	}
}
