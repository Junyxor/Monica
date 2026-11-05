package takagi.ru.monica.ui.designsystem

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import takagi.ru.monica.data.ColorScheme
import takagi.ru.monica.ui.theme.MonicaTheme

/**
 * Renders the main vault surfaces to PNG so design changes can be reviewed
 * without a device. Opt-in, see `monica.designPreviews` in app/build.gradle.
 *
 * Output: app/build/outputs/design-previews/<name>.png
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], qualifiers = "w411dp-h891dp-xxhdpi", application = Application::class)
class DesignPreviewTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun requireOptIn() {
        assumeTrue(
            "Design previews are opt-in: pass -PmonicaDesignPreviews",
            System.getProperty("monica.designPreviews") == "true"
        )
    }

    @Test
    fun passwordsPageDark() = capture("passwords_page_dark", darkTheme = true) {
        DesignPreviewScenes.PasswordsPage()
    }

    @Test
    fun passwordsPageLight() = capture("passwords_page_light", darkTheme = false) {
        DesignPreviewScenes.PasswordsPage()
    }

    @Test
    fun authenticatorPageDark() = capture("authenticator_page_dark", darkTheme = true) {
        DesignPreviewScenes.AuthenticatorPage()
    }

    @Test
    fun authenticatorPageLight() = capture("authenticator_page_light", darkTheme = false) {
        DesignPreviewScenes.AuthenticatorPage()
    }

    @Test
    @Config(qualifiers = "zh-rCN-w411dp-h891dp-xxhdpi")
    fun passwordsPageChineseDark() = capture("passwords_page_zh_dark", darkTheme = true) {
        DesignPreviewScenes.PasswordsPage()
    }

    @Test
    fun passwordsPagePureBlack() = capture("passwords_page_oled", darkTheme = true, pureBlack = true) {
        DesignPreviewScenes.PasswordsPage()
    }

    @Test
    fun componentsSheetDark() = capture("components_dark", darkTheme = true, scroll = true) {
        DesignPreviewScenes.ComponentsSheet()
    }

    @Test
    fun componentsSheetLight() = capture("components_light", darkTheme = false, scroll = true) {
        DesignPreviewScenes.ComponentsSheet()
    }

    private fun capture(
        name: String,
        darkTheme: Boolean,
        pureBlack: Boolean = false,
        scroll: Boolean = false,
        content: @Composable () -> Unit
    ) {
        compose.setContent {
            MonicaTheme(
                darkTheme = darkTheme,
                oledPureBlackEnabled = pureBlack,
                colorScheme = ColorScheme.DEFAULT
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (scroll) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) { content() }
                    } else {
                        content()
                    }
                }
            }
        }
        // Brand icons and favicons load on background dispatchers; give them
        // real time to finish so every capture shows the settled UI.
        repeat(4) {
            compose.waitForIdle()
            Thread.sleep(300)
        }
        compose.waitForIdle()
        // captureToImage() waits for a hardware redraw that Robolectric never
        // delivers, so the window's root view is drawn onto a bitmap instead.
        val root = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        compose.runOnUiThread { root.draw(Canvas(bitmap)) }
        val directory = File(
            System.getProperty("monica.designPreviews.dir") ?: "build/outputs/design-previews"
        ).apply { mkdirs() }
        FileOutputStream(File(directory, "$name.png")).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }
}
