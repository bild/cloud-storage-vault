package no.ntnu.item.csv.cryptoutil;

import javax.crypto.SecretKey;

import no.ntnu.item.cryptoutil.Cryptoutil;
import no.ntnu.item.cryptoutil.KeyChain;

import org.junit.Assert;
import org.junit.Test;

public class KeyChainTest {

	private static final String password = "password2";

	@Test
	public void testInitialGeneration() {
		KeyChain test = new KeyChain(password);
		SecretKey key = test.getKey();
		Assert.assertEquals(Cryptoutil.SYM_SIZE / 8, key.getEncoded().length);
		Assert.assertEquals(Cryptoutil.SYM_SIZE / 8, test.getSalt().length);
	}

	@Test
	public void testThatAKeyCanBeReproduced() {
		KeyChain test = new KeyChain(password);
		SecretKey key = test.getKey();
		byte[] salt = test.getSalt();

		KeyChain newAttempt = new KeyChain(password, salt);
		SecretKey key2 = newAttempt.getKey();

		Assert.assertArrayEquals(key.getEncoded(), key2.getEncoded());
	}

}
