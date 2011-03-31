package no.ntnu.item.csv.csvobject;

import javax.crypto.SecretKey;

import no.ntnu.item.cryptoutil.Cryptoutil;
import no.ntnu.item.csv.capability.Capability;
import no.ntnu.item.csv.capability.CapabilityImpl;
import no.ntnu.item.csv.capability.CapabilityType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CSVFolderTest {

	private CSVFolder newFolder;

	@Before
	public void setUp() {
		this.newFolder = new CSVFolder();
	}

	@Test
	public void testCapabilityGeneration() {
		Capability cap = this.newFolder.getCapability();
		Assert.assertEquals(CapabilityType.RW, cap.getType());
		Assert.assertNotNull(cap.getStorageIndex());
		Assert.assertEquals(16, cap.getVerificationKey().length);
		Assert.assertEquals(16, cap.getKey().length);
	}

	@Test
	public void testEncryption() {
		SecretKey key = Cryptoutil.generateSymmetricKey();
		Capability cap = new CapabilityImpl(CapabilityType.RO,
				key.getEncoded(), null, false);
		this.newFolder.addContent("Hallo", cap);
		this.newFolder.encrypt();
		Assert.assertNotNull(this.newFolder.getCipherText());

	}

	@Test
	public void testDecryption() {
		testEncryption();
		CSVFolder dec = new CSVFolder(this.newFolder.getCapability(),
				this.newFolder.getCipherText(), this.newFolder.getPubKey(),
				this.newFolder.getIV(), null);
		dec.decrypt();
		Assert.assertTrue(dec.getContents().containsKey("Hallo"));
	}

	@Test
	public void testSigning() {
		testEncryption();
		Assert.assertTrue(this.newFolder.isValid());
	}

	@Test
	public void testSerialization() {
		CapabilityImpl cap = new CapabilityImpl(CapabilityType.RO, Cryptoutil
				.generateSymmetricKey().getEncoded(), null, true);
		this.newFolder.addContent("Foobar", cap);
		this.newFolder.encrypt();
		byte[] enc = this.newFolder.getTransferArray();
		int expectedLength = 272 + 132 + 128 + 16
				+ this.newFolder.getCipherText().length;
		// encPrivkey + pubkey + signature + iv + Ciphertext
		Assert.assertEquals(expectedLength, enc.length);
		String decCap = this.newFolder.getCapability().toString();
		Capability newCap = CapabilityImpl.fromString(decCap);
		CSVFolder dec = CSVFolder.createFromByteArray(enc, newCap);
		Assert.assertArrayEquals(this.newFolder.getCipherText(),
				dec.getCipherText());
		Assert.assertArrayEquals(this.newFolder.getPubKey(), dec.getPubKey());

		Assert.assertArrayEquals(this.newFolder.getTransferArray(),
				dec.getTransferArray());
		dec.decrypt();
		dec.encrypt();
		Assert.assertArrayEquals(this.newFolder.getPlainText(),
				dec.getPlainText());
		Assert.assertTrue(dec.getContents().containsKey("Foobar"));
		Assert.assertArrayEquals(cap.getKey(), dec.getContents().get("Foobar")
				.getKey());
		Assert.assertEquals(cap.getType(), dec.getContents().get("Foobar")
				.getType());

		Assert.assertEquals(enc.length, dec.getTransferArray().length);

		Assert.assertTrue(dec.isValid());
	}

}
