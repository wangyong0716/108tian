package com.ksider.mobile.android.utils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SignUtils {

	private static final String ALGORITHM = "RSA";

	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	private static final String DEFAULT_CHARSET = "UTF-8";

	public static String sign(String content, String privateKey) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature
					.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(content.getBytes(DEFAULT_CHARSET));

			byte[] signed = signature.sign();

			return Base64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean verify(String content, String sign, String aliPubKey){
		return verify(content, sign, aliPubKey, DEFAULT_CHARSET);
	}

	public static boolean verify(String content, String sign, String aliPubKey, String inputCharset){
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
			byte[] endcodedKey = Base64.decode(aliPubKey);
			PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(endcodedKey));
			Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
			signature.initVerify(publicKey);
			signature.update(content.getBytes(inputCharset));

			byte[] signDecode = Base64.decode(sign);
			return signature.verify(signDecode);

		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

}
