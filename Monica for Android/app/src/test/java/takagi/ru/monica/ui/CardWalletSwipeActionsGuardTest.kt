package takagi.ru.monica.ui

import takagi.ru.monica.testing.readSourceText

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CardWalletSwipeActionsGuardTest {

    @Test
    fun walletCardsReuseSwipeDeleteAndSelectionWithoutConflictingWithReorder() {
        val source = projectFile(
            "app/src/main/java/takagi/ru/monica/ui/screens/CardWalletScreen.kt"
        ).readSourceText().replace("\r\n", "\n")
        val swipeActionsCall = source
            .substringAfter("SwipeActions(")
            .substringBefore("\n                                    ) {")

        assertTrue(swipeActionsCall.contains("onSwipeLeft = { itemToDelete = item }"))
        assertTrue(swipeActionsCall.contains("onSwipeRight = toggleSelection"))
        assertTrue(swipeActionsCall.contains("enabled = !isDragging"))
        assertTrue(swipeActionsCall.contains("allowSwipeLeft = !isSelectionMode"))
        assertTrue(swipeActionsCall.contains("allowSwipeRight = true"))
        assertTrue(swipeActionsCall.contains("cardShape = if (walletItem.type == WalletListItemType.BANK_CARD)"))
        assertTrue(swipeActionsCall.contains("BankCardShape else MonicaItemCardShape"))
    }

    @Test
    fun swipeDeleteKeepsExistingConfirmationAndIdentityVerificationPath() {
        val source = projectFile(
            "app/src/main/java/takagi/ru/monica/ui/screens/CardWalletScreen.kt"
        ).readSourceText().replace("\r\n", "\n")

        assertTrue(source.contains("itemToDelete?.let { item ->\n        AlertDialog("))
        assertTrue(source.contains("requestDeleteVerification(setOf(item.id))"))
        assertTrue(source.contains("performDelete(verifyDeleteIds)"))
    }

    @Test
    fun swipeContainerUsesTheSameShapeAsVaultCards() {
        val cardSource = projectFile(
            "app/src/main/java/takagi/ru/monica/ui/components/MonicaItemCard.kt"
        ).readSourceText().replace("\r\n", "\n")
        val shapeSource = projectFile(
            "app/src/main/java/takagi/ru/monica/ui/theme/Shape.kt"
        ).readSourceText().replace("\r\n", "\n")
        val swipeSource = projectFile(
            "app/src/main/java/takagi/ru/monica/ui/gestures/SwipeActions.kt"
        ).readSourceText().replace("\r\n", "\n")

        // Item cards take their corner from the design token, and the swipe
        // container's default corner is that same 16 dp, so callers that do
        // not pass a shape still get one radius instead of two.
        assertTrue(cardSource.contains("val MonicaItemCardShape = MonicaShapeTokens.Item"))
        assertTrue(shapeSource.contains("val ItemCornerRadius = 16.dp"))
        assertTrue(shapeSource.contains("val Item = RoundedCornerShape(ItemCornerRadius)"))
        assertTrue(swipeSource.contains("cardShape: Shape = RoundedCornerShape(16.dp)"))
        assertTrue(swipeSource.contains("val componentShape = cardShape"))
    }

    private fun projectFile(path: String): File {
        var dir = File(requireNotNull(System.getProperty("user.dir"))).canonicalFile
        while (
            dir.parentFile != null &&
            !File(dir, "settings.gradle").exists() &&
            !File(dir, "settings.gradle.kts").exists()
        ) {
            dir = dir.parentFile!!
        }
        return File(dir, path)
    }
}
