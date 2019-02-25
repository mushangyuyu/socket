package com.test.util;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import sun.misc.BASE64Decoder;

//将字符穿转换成公钥私钥
public class StrToP {
	/**
	  * 实例化公钥
	  * 
	  * @return
	  */
	 public static PublicKey getPubKey(String string) {
	  PublicKey publicKey = null;
	  try {

	   // X509EncodedKeySpec用于构建公钥规范
	    String pubKey =string;
	      java.security.spec.X509EncodedKeySpec bobPubKeySpec = new java.security.spec.X509EncodedKeySpec(
	     new BASE64Decoder().decodeBuffer(pubKey));
	   // RSA非对称加密算法
	   java.security.KeyFactory keyFactory;
	   keyFactory = java.security.KeyFactory.getInstance("RSA");
	   // 取公钥匙对象
	   publicKey = keyFactory.generatePublic(bobPubKeySpec);
	  } catch (NoSuchAlgorithmException e) {
	   e.printStackTrace();
	  } catch (InvalidKeySpecException e) {
	   e.printStackTrace();
	  } catch (IOException e) {
	   e.printStackTrace();
	  }
	  return publicKey;
	 }
	 
	 /**
	  * 实例化私钥
	  * 
	  * @return
	  */
	 public static PrivateKey getPrivateKey(String string) {
	  PrivateKey privateKey = null;
	  String priKey = string;
	  PKCS8EncodedKeySpec priPKCS8;
	  try {
		  //PKCS8EncodedKeySpec用于构建私钥规范
	   priPKCS8 = new PKCS8EncodedKeySpec(new BASE64Decoder().decodeBuffer(priKey));
	   KeyFactory keyf = KeyFactory.getInstance("RSA");
	   privateKey = keyf.generatePrivate(priPKCS8);
	  } catch (IOException e) {
	   e.printStackTrace();
	  } catch (NoSuchAlgorithmException e) {
	   e.printStackTrace();
	  } catch (InvalidKeySpecException e) {
	   e.printStackTrace();
	  }
	  return privateKey;
	 }
}
