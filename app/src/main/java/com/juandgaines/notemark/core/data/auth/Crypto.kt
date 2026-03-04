package com.juandgaines.notemark.core.data.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object Crypto {

    private const val KEYSTORE_ALIAS = "session_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: generateKey()
    }

    private fun generateKey(): SecretKey {
        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        ).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(bytes)
        return iv + encrypted
    }

    fun decrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = bytes.copyOfRange(0, cipher.blockSize)
        val data = bytes.copyOfRange(cipher.blockSize, bytes.size)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), IvParameterSpec(iv))
        return cipher.doFinal(data)
    }
}
