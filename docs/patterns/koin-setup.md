# Koin DI Setup (Android)

Pattern for organizing Koin dependency injection in an Android project.

## Application Class

```kotlin
package com.juandgaines.notemark

import android.app.Application
import com.juandgaines.notemark.di.initKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initKoin(this)
    }
}
```

Register in `AndroidManifest.xml`:
```xml
<application
    android:name=".App"
    ... >
```

## initKoin Function

```kotlin
package com.juandgaines.notemark.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun initKoin(app: Application) {
    startKoin {
        androidContext(app)
        modules(
            appModule,
            coreDataModule,
            // Feature modules:
            // authModule,
        )
    }
}
```

## Module Organization

### appModule (App-Scoped Dependencies)

```kotlin
package com.juandgaines.notemark.di

import com.juandgaines.notemark.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainViewModel)
}
```

### coreDataModule (Data Layer)

```kotlin
package com.juandgaines.notemark.di

import com.juandgaines.notemark.core.data.auth.EncryptedSessionStorage
import com.juandgaines.notemark.core.data.networking.HttpClientFactory
import com.juandgaines.notemark.core.domain.SessionStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    singleOf(::EncryptedSessionStorage) bind SessionStorage::class

    single {
        HttpClientFactory(get()).build()
    }
}
```

### Feature Module Example (Auth)

```kotlin
package com.juandgaines.notemark.auth.di

import com.juandgaines.notemark.auth.data.AuthRepositoryImpl
import com.juandgaines.notemark.auth.domain.AuthRepository
import com.juandgaines.notemark.auth.domain.UserDataValidator
import com.juandgaines.notemark.auth.presentation.login.LoginViewModel
import com.juandgaines.notemark.auth.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::UserDataValidator)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}
```

## Key Patterns

### Binding Interface to Implementation
```kotlin
singleOf(::EncryptedSessionStorage) bind SessionStorage::class
```

### ViewModel Registration
```kotlin
viewModelOf(::LoginViewModel)
```

### ViewModel Access in Compose
```kotlin
@Composable
fun MyScreenRoot(
    viewModel: MyScreenViewModel = koinViewModel(),
) { ... }
```

### Activity-Level ViewModel
```kotlin
class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()
}
```

## Module Assembly Order

1. `coreDataModule` — SessionStorage, HttpClient (no dependencies on features)
2. `appModule` — MainViewModel (depends on SessionStorage from coreDataModule)
3. Feature modules — depend on core modules

## Notes

- Use `singleOf` for singletons, `factoryOf` for new instances each time
- `viewModelOf` automatically scopes to the Android lifecycle
- All constructor parameters are auto-resolved by Koin
- Keep one module per feature/layer for maintainability
- The `bind` keyword tells Koin to also register the implementation under its interface type
