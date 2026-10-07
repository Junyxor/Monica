package takagi.ru.monica.credentialexchange

import androidx.credentials.providerevents.exception.*
import org.junit.Assert.*
import org.junit.Test
import takagi.ru.monica.R

class CredentialExchangeErrorsTest {
    @Test fun decodeDiagnosticsContainOnlyCountsAndFixedReasonNames() {
        val sensitive = "private-key account@example.invalid payload-secret"
        val decoded = CxfCredentialCodec.Decoded(listOf(CxfCredentialCodec.Item(sensitive, sensitive,
            logins = listOf(CxfCredentialCodec.Login(sensitive, sensitive)), createdAt = 0, notes = sensitive)),
            mapOf(CxfCredentialCodec.SkipReason.PASSKEY_BLOB to 4, CxfCredentialCodec.SkipReason.UNSUPPORTED_TOTP to 2))
        assertEquals(" decodedPasswords=1 decodedPasskeys=0 skipped_UNSUPPORTED_TOTP=2 skipped_PASSKEY_BLOB=4",
            CredentialExchangeErrors.decodeDiagnostic(decoded))
        assertFalse(CredentialExchangeErrors.decodeDiagnostic(decoded).contains(sensitive))
    }

    @Test fun missingSourceAndSystemErrorsDoNotClaimTheDeviceIsUnsupported() {
        assertEquals(R.string.exchange_no_sources,
            CredentialExchangeErrors.message(ImportCredentialsNoExportOptionException()))
        assertEquals(R.string.exchange_system_error,
            CredentialExchangeErrors.message(ImportCredentialsUnknownErrorException()))
        assertEquals(R.string.exchange_system_error,
            CredentialExchangeErrors.message(ImportCredentialsSystemErrorException()))
        assertEquals(R.string.exchange_unavailable,
            CredentialExchangeErrors.message(ImportCredentialsProviderConfigurationException()))
        assertEquals(R.string.exchange_failed,
            CredentialExchangeErrors.message(IllegalStateException()))
    }

    @Test fun invalidDataAndRejectedCallersRemainDistinctFromPlatformAvailability() {
        assertEquals(R.string.exchange_invalid_data,
            CredentialExchangeErrors.message(CxfCredentialCodec.InvalidDocument()))
        assertEquals(R.string.exchange_invalid_data,
            CredentialExchangeErrors.message(ImportCredentialsInvalidJsonException()))
        assertEquals(R.string.exchange_unverified_app,
            CredentialExchangeErrors.message(ImportCredentialsUnknownCallerException()))
        assertEquals(R.string.exchange_unverified_app,
            CredentialExchangeErrors.message(SecurityException()))
    }

    @Test fun diagnosticsNeverIncludeExceptionMessagesOrCauses() {
        val sensitive = "registration-secret private-key CXF-json account@example.com"
        val errors = listOf(ImportCredentialsUnknownErrorException(sensitive),
            RegisterExportUnknownErrorException(sensitive), SecurityException(sensitive, Exception(sensitive)))
        for (error in errors) {
            val record = CredentialExchangeErrors.diagnostic(CredentialExchangeErrors.Operation.REGISTER, error)
            assertTrue(record.contains("result=FAILURE"))
            assertFalse(record.contains(sensitive))
            assertFalse(record.contains("private-key"))
            assertFalse(record.contains("account@example.com"))
        }
    }
}
