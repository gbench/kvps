package gbench.whccb.client.util;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
	private static final String ENCODE = "utf-8";
	
	private static final String AES = "AES";
	
	private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";
	
	public static String encrypt(String content,String encryptKey) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
		keyGenerator .init(128);
		Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), AES));
		byte[] bytes = cipher.doFinal(content.getBytes(ENCODE));
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public static String decrypt(String content,String encryptKey) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
		keyGenerator .init(128);
		Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), AES));
		byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(content.getBytes(ENCODE)));
		return new String(bytes, ENCODE);
	}
	
	
	public static void main(String[] args) {
		try {
//			String key = MD5.md5("2").substring(8,24);
//			System.out.println(key);
	//		System.out.println(decrypt("OrPqrdJHcMlOlXT3+vIRRSu0Uyq24lWNDyvJ1XC3bg2lWMr/LOFWxTc6eoEc5Ai5hWrHIWIOagqujzNfr5zIQOt69gaGstDNrclLanZ+UoGPk+zAgf0Ljhbu+40vdm+0bzdNx/0So+AYEPWEiA20FXLcsT+qGI8dz06feqUSC+8=", "a0b923820dcc509a"));
			System.out.println(Base64Util.unescape(decrypt("eyJjb2RlIjowLCJjb3VudCI6MCwibWVzc2FnZSI6IuWPguaVsOmUmeivr++8gSIsInBhZ2UiOjAsInJvd3MiOm51bGwsInRvdGFsIjowfQ==","1ef00432c2a9cc73")));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
