package com.juandgaines.notemark.core.data.auth

import android.util.Base64
import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

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
