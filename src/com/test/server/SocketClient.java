package com.test.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import com.test.util.AESUtil;
import com.test.util.SHA1;
import com.test.util.StrToP;

public class SocketClient {
	public static void main(String[] args) throws Exception {
		// 报文的消息体
		String str1 = "<?xml version=\"1.0\" encoding=\"utf-8\"?><name>张三</name><age>23</age><gender>男</gender><address>中国北京市</address><telephone>0123456789</telephone>";
		// AES密钥,注意: 这里的秘钥必须是16位的
		final String keyBytes = "abcdefgabcdefg12";
		// 客户端的私钥，用于签名（注意，这里的私钥和密钥是提前自己用工具类生成好的）
		String cpri = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAKzdxIgoWBIcHZcwKTtgAhtgtMsZ"
				+ "99Y5n1AcFcfUgkMeIYj1E1OhajHFxbDp9K938l5aKcs/6Ry8DM4U7lr0D7sIm0R03/RYVVFll+HY"
				+ "jeh2Cam53EAegev2J6ojFkqM/9wGp8V2ItFj8Djkgw4T4nZnJ+slLxJ1Rbs8Zn+6UoK9AgMBAAEC"
				+ "gYEAjM94r5BtbRMJ54WLkJn8HWEA5BAbGZ2GcDLwav5nvVRl7LH1NEJfxntbNWjdr7D89G5tbOp6"
				+ "cLWdmKDkJ6J2zIQ4zbJtd+DhRWBXZsA9J1XBKlbBwTUg3oi8tizrpFoCTNxwAxPnrcn2x4e2qCxS"
				+ "03fnp3sXaAXrx2Iqw08tXOECQQDfXRAD+k1yqoMtrniVhhCt7TGSTRW6YunFj4pWjrVFvR2qk0lZ"
				+ "/OwEYS8HbWBHmraDzBR7RK03jewEhmIWEDy3AkEAxh/ZnnUygpGSGFDOKOLmnUuKfiBLiuWPjUPY"
				+ "PjsA3Gy3/1T57HLsAJZINixt1OXO86/H60ELNvKiOkg1TXowKwJBAI1N3nflvWExJdOccISHT734"
				+ "NEgNpBVJxgJJkIa5uUvpG/9xNhJLwzPyF0Jnm2UxJw2x6bMh+0MN8aXbLeovU3kCQBnDRn8OalUE"
				+ "LOUwjL5QUwCdNBMWi/wxwary6YmeDoybLonrONyATSP+ZsF0oYuPnoudjfePa6/neSTvAs9iViUC"
				+ "QQDeD2+GXZaky69txElLCvd2654VuZ6kqaEfmlV6hvKkZ+N5mfKgSo1IE9PpGaJm/Rb9ntIfVLd/"
				+ "4hcSFQiQjC8L";
		// 这是服务器端的公钥，用于加密aes密钥
		String spub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCHmb80T+sQFHsIITXovApX99+WYizFh8QDOXGM"
				+ "TG4gN5tcPazlsbc/WbHSKRD6ph2nsDUMnb0RARSSLwsnmbsy5l+PEOm9/PmdO77pz5KesWE6doqt"
				+ "2ySw+NTdXXY5oOFSZwZU90MDVcQe7qelcu1GF++J2IViBEw7Mi4TwupU8QIDAQAB";
		//字符串的私钥密钥是经过Base64编码过的，但是将其转化成私钥和公钥的时候已将其进行base64解码了，所以可以直接将字符串变成私钥或公钥
		// 对字符串的私钥和公钥转换成私钥和公钥的类型
		PrivateKey cpri1 = StrToP.getPrivateKey(cpri);
		PublicKey spub1 = StrToP.getPubKey(spub);
		// 第一步：先对明文进行签名
		String sign = SHA1.encode(str1);// 这个是利用sha1生成的摘要
		// 第二步：用客户端的私钥cpri1对摘要进行加密（签名）
		byte[] s = sign.getBytes();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, cpri1);
		byte[] enBytes = cipher.doFinal(s);
		// 第三步：使用aes密钥对明文进行加密
		String str2 = AESUtil.encode(str1, keyBytes);
		System.out.println("aes加密后的密文："+str2);
		// 第四步：使用服务端的公钥spub1对aes密钥进行加密
		byte[] s2 = keyBytes.getBytes();
//		System.out.println(new String(s2));//这里的二进制可以直接转换成明文
		Cipher cipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");//这里必须是这样（"RSA/ECB/PKCS1Padding"）
		cipher2.init(Cipher.ENCRYPT_MODE, spub1);
		byte[] enBytes2 = cipher.doFinal(s2);
		// 第五步：是向服务端传输数据(异常全部先抛出去)
		OutputStream os1 = null;
		DataOutputStream dos = null;
		try {
			System.out.println("客户端启动：");
			Socket socket = new Socket("127.0.0.1", 8899);
			os1 = socket.getOutputStream();
			dos = new DataOutputStream(os1);
			dos.writeInt(enBytes2.length + enBytes.length
					+ str2.getBytes().length);// 传输数据的总字节数
			dos.write(enBytes2);// 用服务端的公钥加密的aes密钥
			dos.write(enBytes);// 用客户端的私钥加密的签名
			dos.write(str2.getBytes());// 用AES密钥加密的明文
			dos.flush();
			os1.flush();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
