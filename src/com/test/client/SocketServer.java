package com.test.client;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.Cipher;
import com.test.util.AESUtil;
import com.test.util.SHA1;
import com.test.util.StrToP;

public class SocketServer {
	public static void main(String[] args) throws Exception {
		// 这是客户端的公钥;用于验证签名
		String cpub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCs3cSIKFgSHB2XMCk7YAIbYLTLGffWOZ9QHBXH"
				+ "1IJDHiGI9RNToWoxxcWw6fSvd/JeWinLP+kcvAzOFO5a9A+7CJtEdN/0WFVRZZfh2I3odgmpudxA"
				+ "HoHr9ieqIxZKjP/cBqfFdiLRY/A45IMOE+J2ZyfrJS8SdUW7PGZ/ulKCvQIDAQAB";
		// 这是服务器端的私钥；用于解密加密的aes
		String spri = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIeZvzRP6xAUewghNei8Clf335Zi"
				+ "LMWHxAM5cYxMbiA3m1w9rOWxtz9ZsdIpEPqmHaewNQydvREBFJIvCyeZuzLmX48Q6b38+Z07vunP"
				+ "kp6xYTp2iq3bJLD41N1ddjmg4VJnBlT3QwNVxB7up6Vy7UYX74nYhWIETDsyLhPC6lTxAgMBAAEC"
				+ "gYBT7gG+60hfJ44PNh8b8mekkzO87P8xNBtKs59oa1YucryilzoQ4bK+b1H09p4fxfnM9O+g43FG"
				+ "uRh2cs/ArxKCOKBWipF26zcTdEhbGemiJNUJDUCgMEm8auqzUM80/2PpSNYkXlkn11ozg9jLF5CY"
				+ "rLGom02QPm0Sqmot2FqBAQJBAP8QKapEn205dRv/niSqo5CdpBjjdL/GaIrY7d7GbJLn5ObbU46K"
				+ "ubhERrK8aV/saOTMBTWi3hbaWpe4YcdFjSkCQQCIGUC6bhP0aUziJydNRXWYup0kXSXde6MnFEG3"
				+ "xdpXgSkpB3RKq3mZ2fKm01XSJ0I+W409CV5qN8is23pHUrqJAkEA+ugWZIWO4y5TcpB5LA6kSj2Y"
				+ "0FboYUK3UmblUHGXGsh9l0+IGb+DxK19cpP/gLoZ0YCVW1b07DwuQ/PAU/uqSQJAF4bOAqw+tNRz"
				+ "HvEHwTrNnk48FeooPoY96OZ+iccg/FluhRItzy6fQbJEWQRVZJO2xtLKso9pC9GC1ibGl4e8iQJB"
				+ "AK+0UKcP+lltj+GIgZxvD/gcpW3mNXqU8Of4o7ddQIPK7A47ya38iSKJf/ASGQgY98u1aPqCmbVL"
				+ "Z1eTQXLGVtE=";//这种字符串时经过base64编码的,有等号是补位
		// 对字符串的私钥和公钥转换成私钥和公钥的类
		PrivateKey spri1 = StrToP.getPrivateKey(spri);
		PublicKey cpub1 = StrToP.getPubKey(cpub);
		
		/*System.out.println(spri1);
		System.out.println(cpub1);*/    //ctrl+shift+/  多行注释
		
		ServerSocket ss = new ServerSocket(8899);
		System.out.println("服务端启动：");
		Socket socket = ss.accept();
		// 第一步：接收数据（记住这里必须要用DataInputStream，与客户端对应）
		DataInputStream in = new DataInputStream(socket.getInputStream());
		int sb = in.readInt();// 一个字节8比特bit
		System.out.println("客户端传过来的字节数："+sb);// 得到传过来的字节数
		byte[] b = new byte[sb];
		in.readFully(b);// 传过来的数据都存在b中
		// 第二步：截取数据
		byte[] b1 = Arrays.copyOfRange(b,0,128);// 服务端的公钥加密的aes密钥
		System.out.println(b1.toString());
		byte[] b2 = Arrays.copyOfRange(b, 128, 256);// RSA客户端的私钥加密的摘要签名
		byte[] b3 = Arrays.copyOfRange(b, 256, sb);// 密文
		// 第三步：用服务端的私钥对加密的aes进行解密
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");//这里必须是这样（"RSA/ECB/PKCS1Padding"）
		cipher.init(Cipher.DECRYPT_MODE, spri1);
		byte[] output = cipher.doFinal(b1);//异常全部抛掉
		String s = new String(output);//将byte[]转化成字符串,按照utf-8进行编码
		System.out.println("解密后的aes密钥："+s);
		//第四步：用解密后的aes密钥对密文进行解密
		String s2=AESUtil.decode(new String(b3),s);
		System.out.println(s2);
		//第五步：利用客户端的公钥进行解签名
		Cipher cipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");//这里必须是这样（"RSA/ECB/PKCS1Padding"）
		cipher.init(Cipher.DECRYPT_MODE, cpub1);
		byte[] output2 = cipher2.doFinal(b2);//异常全部抛掉
		System.out.println(new String(output2,"UTF-8"));
		//第六步：对解密的明文利用SHA1进行摘要提取，然后和上一步对比结果
		String str=SHA1.encode(s2);
		if(str.equals(output2)){
			System.out.println("密文正常！");
		}else {
			System.out.println("密文已被篡改！");
		}
	}
}
