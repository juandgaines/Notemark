# Device Configuration / Form Factor Handling (Android)

Pattern for responsive layout design across phone portrait, phone landscape, tablet, and desktop form factors using Material3 adaptive window size classes.

## Dependencies

Add to `libs.versions.toml`:
```toml
[libraries]
androidx-compose-material3-adaptive = { group = "androidx.compose.material3.adaptive", name = "adaptive", version = "1.1.0" }
```

Add to `app/build.gradle.kts`:
```kotlin
implementation(libs.androidx.compose.material3.adaptive)
```

## DeviceConfiguration Enum

```kotlin
package com.juandgaines.notemark.core.presentation.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND

@Composable
fun currentDeviceConfiguration(): DeviceConfiguration {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
}

enum class DeviceConfiguration {
    MOBILE_PORTRAIT,
    MOBILE_LANDSCAPE,
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE,
    DESKTOP;

    val isMobile: Boolean
        get() = this in listOf(MOBILE_PORTRAIT, MOBILE_LANDSCAPE)

    val isWideScreen: Boolean
        get() = this in listOf(TABLET_LANDSCAPE, DESKTOP)

    companion object {
        fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration {
            return with(windowSizeClass) {
                when {
                    minWidthDp < WIDTH_DP_MEDIUM_LOWER_BOUND &&
                            minHeightDp >= HEIGHT_DP_MEDIUM_LOWER_BOUND -> MOBILE_PORTRAIT
                    minWidthDp >= WIDTH_DP_EXPANDED_LOWER_BOUND &&
                            minHeightDp < HEIGHT_DP_MEDIUM_LOWER_BOUND -> MOBILE_LANDSCAPE
                    minWidthDp in WIDTH_DP_MEDIUM_LOWER_BOUND..WIDTH_DP_EXPANDED_LOWER_BOUND &&
                            minHeightDp >= HEIGHT_DP_EXPANDED_LOWER_BOUND -> TABLET_PORTRAIT
                    minWidthDp >= WIDTH_DP_EXPANDED_LOWER_BOUND &&
                            minHeightDp in HEIGHT_DP_MEDIUM_LOWER_BOUND..HEIGHT_DP_EXPANDED_LOWER_BOUND -> TABLET_LANDSCAPE
                    else -> DESKTOP
                }
            }
        }
    }
}
```

## Usage Patterns

### Pattern 1: When Expression for Different Layouts

The most common pattern — switch between entirely different layouts per configuration:

```kotlin
@Composable
fun AdaptiveFormLayout(
    logo: @Composable () -> Unit,
    formContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = currentDeviceConfiguration()

    when (configuration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> {
            // Full-screen vertical layout
            Column(modifier = modifier.fillMaxSize()) {
                logo()
                Spacer(modifier = Modifier.height(32.dp))
                formContent()
            }
        }
        DeviceConfiguration.MOBILE_LANDSCAPE -> {
            // Side-by-side: logo/header left, scrollable form right
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    logo()
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    formContent()
                }
            }
        }
        DeviceConfiguration.TABLET_PORTRAIT,
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            // Centered card with constrained width
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                logo()
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    formContent()
                }
            }
        }
    }
}
```

### Pattern 2: Helper Property Checks

Use `isMobile` / `isWideScreen` for simple conditional logic:

```kotlin
@Composable
fun AdaptiveDialogSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val configuration = currentDeviceConfiguration()
    if (configuration.isMobile) {
        // Bottom sheet on mobile
        ModalBottomSheet(onDismissRequest = onDismiss) {
            content()
        }
    } else {
        // Dialog on tablet/desktop
        Dialog(onDismissRequest = onDismiss) {
            content()
        }
    }
}
```

### Pattern 3: Conditional Visibility

Hide/show elements based on configuration:

```kotlin
@Composable
fun MyScreen(state: MyState) {
    val configuration = currentDeviceConfiguration()

    Column {
        // Hide header in landscape when keyboard is visible
        val shouldHideHeader = configuration == DeviceConfiguration.MOBILE_LANDSCAPE
                && isKeyboardVisible

        AnimatedVisibility(visible = !shouldHideHeader) {
            HeaderSection()
        }

        // Content
        FormContent()
    }
}
```

### Pattern 4: Dynamic Sizing

Adjust sizes based on configuration:

```kotlin
@Composable
fun IllustrationSection() {
    val configuration = currentDeviceConfiguration()
    Image(
        painter = painterResource(R.drawable.illustration),
        contentDescription = null,
        modifier = Modifier.size(
            if (configuration == DeviceConfiguration.MOBILE_LANDSCAPE) 125.dp else 200.dp
        )
    )
}
```

### Pattern 5: Adaptive Height Constraints

Different height strategies per form factor:

```kotlin
@Composable
fun ColumnScope.ParticipantsList() {
    val configuration = currentDeviceConfiguration()
    val heightModifier = when (configuration) {
        DeviceConfiguration.TABLET_PORTRAIT,
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            Modifier.heightIn(min = 200.dp, max = 300.dp)
        }
        else -> Modifier.weight(1f)
    }

    Box(modifier = heightModifier) {
        // List content
    }
}
```

### Pattern 6: Adaptive Section Layout (Column vs Row)

Switch between vertical and horizontal section layouts:

```kotlin
@Composable
fun ProfileSectionLayout(
    headerText: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val configuration = currentDeviceConfiguration()
    when (configuration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(text = headerText)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    content()
                }
            }
        }
        else -> {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(text = headerText, modifier = Modifier.weight(1f))
                Column(modifier = Modifier.weight(3f)) {
                    content()
                }
            }
        }
    }
}
```

## Notes

- Always call `currentDeviceConfiguration()` at the **composable level**, not in ViewModels
- The enum is derived from `WindowSizeClass` dimension breakpoints — no manual screen size checks
- Use `when` expressions for major layout changes, helper properties for simple conditionals
- Group tablet landscape + desktop together when they share the same layout
- `AnimatedVisibility` provides smooth transitions when configuration changes (e.g., rotation)
- The `isMobile` and `isWideScreen` helpers cover the most common branching needs
