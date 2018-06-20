package com.wanari.coin

object HashHelpers {
  def sha256Hash(text: String) : String = String.format("%064x", BigInt(1, java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))).bigInteger)
}
