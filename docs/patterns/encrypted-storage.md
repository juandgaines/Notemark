# Encrypted Session Storage (Android)

Pattern for securely storing authentication tokens using AndroidKeyStore + DataStore.

## Crypto Object

```kotlin
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
```

## AuthInfo Domain Model

```kotlin
package com.juandgaines.notemark.core.domain

// TODO: Add or remove fields to match your auth token structure
data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
)
```

## AuthInfoSerializable (Data Layer DTO)

```kotlin
package com.juandgaines.notemark.core.data.auth

import com.juandgaines.notemark.core.domain.AuthInfo
import kotlinx.serialization.Serializable

// TODO: Add or remove fields to match your auth token structure
@Serializable
data class AuthInfoSerializable(
    val accessToken: String,
    val refreshToken: String,
)

fun AuthInfo.toSerializable(): AuthInfoSerializable {
    return AuthInfoSerializable(
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
}

fun AuthInfoSerializable.toAuthInfo(): AuthInfo {
    return AuthInfo(
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
}
```

## AuthInfoSerializer (DataStore Serializer with Encryption)

```kotlin
package com.juandgaines.notemark.core.data.auth

import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import android.util.Base64

object AuthInfoSerializer : Serializer<AuthInfoSerializable?> {

    override val defaultValue: AuthInfoSerializable? = null

    override suspend fun readFrom(input: InputStream): AuthInfoSerializable? {
        val encryptedString = input.readBytes().decodeToString()
        if (encryptedString.isBlank()) return null

        return try {
            val encryptedBytes = Base64.decode(encryptedString, Base64.DEFAULT)
            val decryptedBytes = Crypto.decrypt(encryptedBytes)
            Json.decodeFromString<AuthInfoSerializable>(decryptedBytes.decodeToString())
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun writeTo(t: AuthInfoSerializable?, output: OutputStream) {
        if (t == null) {
            output.write(ByteArray(0))
            return
        }
        val json = Json.encodeToString(AuthInfoSerializable.serializer(), t)
        val encryptedBytes = Crypto.encrypt(json.toByteArray())
        val base64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        output.write(base64.toByteArray())
    }
}
```

## SessionStorage Interface

```kotlin
package com.juandgaines.notemark.core.domain

import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    suspend fun get(): AuthInfo?
    suspend fun set(info: AuthInfo?)
    fun observe(): Flow<AuthInfo?>
}
```

## EncryptedSessionStorage Implementation

```kotlin
package com.juandgaines.notemark.core.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.juandgaines.notemark.core.domain.AuthInfo
import com.juandgaines.notemark.core.domain.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore: DataStore<AuthInfoSerializable?> by dataStore(
    fileName = "auth_info.json",
    serializer = AuthInfoSerializer
)

class EncryptedSessionStorage(
    private val context: Context
) : SessionStorage {

    override suspend fun get(): AuthInfo? {
        return context.authDataStore.data.first()?.toAuthInfo()
    }

    override suspend fun set(info: AuthInfo?) {
        context.authDataStore.updateData {
            info?.toSerializable()
        }
    }

    override fun observe(): Flow<AuthInfo?> {
        return context.authDataStore.data.map { it?.toAuthInfo() }
    }
}
```

## Notes

- Handle null/missing session gracefully (first launch, logout)
- The `set(null)` call clears the session (writes empty bytes)
- `observe()` is used by MainViewModel to detect session expiration
- DataStore file is `auth_info.json` — uses custom serializer, NOT preferences DataStore
- The `datastore-preferences` dependency is NOT needed; use `datastore` core instead (already covered by the DataStore library snippet)
