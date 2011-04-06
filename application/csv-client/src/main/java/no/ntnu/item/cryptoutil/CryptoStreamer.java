package no.ntnu.item.cryptoutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoStreamer {

	private SecretKey secretKey;
	private IvParameterSpec iv;
	private Cipher encryptCipher;
	private Cipher decryptCipher;
	private byte[] buffer = new byte[1024];
	private byte[] digest;
	private Stack<InputStream> openInputStreams = new Stack<InputStream>();
	private Stack<OutputStream> openOutputStreams = new Stack<OutputStream>();

	private MessageDigest md;

	public CryptoStreamer(SecretKey secretKey, IvParameterSpec iv) {
		this.secretKey = secretKey;
		this.iv = iv;
		initCipher();
	}

	public CryptoStreamer(byte[] secretKey, byte[] iv) {
		this.iv = new IvParameterSpec(iv);
		this.secretKey = new SecretKeySpec(secretKey, Cryptoutil.SYM_CIPHER);
		initCipher();
	}

	public CryptoStreamer() {
		this.secretKey = Cryptoutil.generateSymmetricKey();
		this.iv = new IvParameterSpec(Cryptoutil.nHash(
				this.secretKey.getEncoded(), 2, 16));
		initCipher();
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;
	}

	public IvParameterSpec getIv() {
		return iv;
	}

	public void setIv(IvParameterSpec iv) {
		this.iv = iv;
	}

	public byte[] getDigest() {
		return digest;
	}

	private void initCipher() {
		try {
			this.encryptCipher = Cipher.getInstance(Cryptoutil.SYM_CIPHER + "/"
					+ Cryptoutil.SYM_MODE + "/" + Cryptoutil.SYM_PADDING);
			this.encryptCipher.init(Cipher.ENCRYPT_MODE, this.secretKey,
					this.iv);
			this.decryptCipher = Cipher.getInstance(Cryptoutil.SYM_CIPHER + "/"
					+ Cryptoutil.SYM_MODE + "/" + Cryptoutil.SYM_PADDING);
			this.decryptCipher.init(Cipher.DECRYPT_MODE, this.secretKey,
					this.iv);
			this.md = MessageDigest.getInstance(Cryptoutil.HASH_ALGORITHM);

		} catch (NoSuchAlgorithmException e) {
		} catch (NoSuchPaddingException e) {
		} catch (InvalidKeyException e) {
		} catch (InvalidAlgorithmParameterException e) {
		}
	}

	public byte[] encrypt(InputStream is) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		OutputStream cipherOutput = new CipherOutputStream(buffer,
				this.encryptCipher);
		int numRead = 0;
		try {
			while ((numRead = is.read(this.buffer)) >= 0) {
				cipherOutput.write(this.buffer, 0, numRead);
			}
			cipherOutput.close();
			return buffer.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public OutputStream getDecryptedAndHashedOutputStream(OutputStream os) {
		this.md.reset();
		DigestOutputStream digestOutputStream = new DigestOutputStream(os, md);
		this.openOutputStreams.push(digestOutputStream);
		this.openOutputStreams.push(os);
		CipherOutputStream cipherOutputStream = new CipherOutputStream(
				digestOutputStream, this.decryptCipher);
		this.md = digestOutputStream.getMessageDigest();
		return cipherOutputStream;
	}

	public InputStream getEncryptedAndHashedInputStream(InputStream is) {
		md.reset();
		DigestInputStream digestInputStream = new DigestInputStream(is, md);
		CipherInputStream cipherInputStream = new CipherInputStream(
				digestInputStream, this.encryptCipher);
		this.md = digestInputStream.getMessageDigest();
		this.openInputStreams.push(digestInputStream);
		this.openInputStreams.push(is);
		return cipherInputStream;
	}

	public byte[] finish() {
		closeStreams();
		byte[] tmp = this.md.digest();
		this.digest = Cryptoutil.singlehash(tmp, 16);
		this.md.reset();
		return this.digest;
	}

	public void closeStreams() {
		// Unknown if we actually need this

		while (true) {
			try {
				this.openOutputStreams.peek();
				OutputStream os = this.openOutputStreams.pop();
				os.flush();
				os.close();
			} catch (EmptyStackException e) {
				break;
			} catch (IOException e) {
			}
		}

		// while (true) {
		// try {
		// this.openInputStreams.peek();
		// this.openInputStreams.pop().close();
		// } catch (EmptyStackException e) {
		// return;
		// } catch (IOException e) {
		// }
		// }
	}

}
