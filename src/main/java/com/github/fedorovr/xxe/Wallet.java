package com.github.fedorovr.xxe;

import java.util.Objects;

public class Wallet {
  private String name;
  private String publicKey;
  private String privateKey;

  public Wallet() {
  }

  public Wallet(String name, String publicKey, String privateKey) {
    this.name = name;
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  public String getName() {
    return name;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  @Override
  public String toString() {
    return String.format("Wallet{name='%s', publicKey='%s', privateKey='%s'}", name, publicKey, privateKey);
  }
}

