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
