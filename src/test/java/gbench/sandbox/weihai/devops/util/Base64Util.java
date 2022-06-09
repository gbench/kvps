package gbench.sandbox.weihai.devops.util;

import java.util.Base64;

public class Base64Util {
	
	/**
	 * 加密
	 * @param src
	 * @return
	 */
	public static String escape(String src) {
		return new String(Base64.getEncoder().encodeToString(src.getBytes()));
	}
	/**
	 * 解密
	 * @param src
	 * @return
	 */
	public static String unescape(String src) {
		return new String(Base64.getDecoder().decode(src));
	}
	public static void main(String[] args) {
		try {
			/*JSONObject object = new JSONObject();
			object.put("username", "admin");
			object.put("password", "123456");
			
			//加密参数并post到指定地址
			JSONObject object1 = new JSONObject();
			String base64=Base64Util.escape(object.toJSONString());
			System.out.println(base64);
//			String key = MD5.md5("2").substring(8,24);
//			System.out.println(key);
//			System.out.println(decrypt("OrPqrdJHcMlOlXT3+vIRRSu0Uyq24lWNDyvJ1XC3bg2lWMr/LOFWxTc6eoEc5Ai5hWrHIWIOagqujzNfr5zIQOt69gaGstDNrclLanZ+UoGPk+zAgf0Ljhbu+40vdm+0bzdNx/0So+AYEPWEiA20FXLcsT+qGI8dz06feqUSC+8=", key));
			System.out.println(base64);*/
			System.out.println(Base64Util.unescape("eyJjb2RlIjowLCJjb3VudCI6MCwibWVzc2FnZSI6IuWPguaVsOmUmeivr++8gSIsInBhZ2UiOjAsInJvd3MiOm51bGwsInRvdGFsIjowfQ=="));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
