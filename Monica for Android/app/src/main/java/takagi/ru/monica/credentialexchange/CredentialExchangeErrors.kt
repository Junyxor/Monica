package takagi.ru.monica.credentialexchange

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import androidx.credentials.providerevents.exception.ImportCredentialsInvalidJsonException
import androidx.credentials.providerevents.exception.ImportCredentialsNoExportOptionException
import androidx.credentials.providerevents.exception.ImportCredentialsProviderConfigurationException
import androidx.credentials.providerevents.exception.ImportCredentialsSystemErrorException
import androidx.credentials.providerevents.exception.ImportCredentialsUnknownCallerException
import androidx.credentials.providerevents.exception.ImportCredentialsUnknownErrorException
import androidx.credentials.providerevents.exception.RegisterExportProviderConfigurationException
import androidx.credentials.providerevents.exception.RegisterExportUnknownErrorException
import takagi.ru.monica.R
import takagi.ru.monica.security.SecurityDiagLogger

/** Distinguish platform failures without logging exception messages or credential payloads. */
internal object CredentialExchangeErrors {
    enum class Operation { REGISTER, IMPORT, EXPORT }
    enum class Kind { PROVIDER_CONFIGURATION, NO_SOURCE, INVALID_DATA, UNVERIFIED_APP, SYSTEM, UNEXPECTED }

    fun classify(error: Exception): Kind = when (error) {
        is ImportCredentialsProviderConfigurationException, is RegisterExportProviderConfigurationException -> Kind.PROVIDER_CONFIGURATION
        is ImportCredentialsNoExportOptionException -> Kind.NO_SOURCE
        is ImportCredentialsInvalidJsonException, is CxfCredentialCodec.InvalidDocument -> Kind.INVALID_DATA
        is ImportCredentialsUnknownCallerException, is SecurityException -> Kind.UNVERIFIED_APP
        is ImportCredentialsSystemErrorException, is ImportCredentialsUnknownErrorException,
        is RegisterExportUnknownErrorException -> Kind.SYSTEM
        else -> Kind.UNEXPECTED
    }

    @StringRes fun message(error: Exception): Int = when (classify(error)) {
        Kind.PROVIDER_CONFIGURATION -> R.string.exchange_unavailable
        Kind.NO_SOURCE -> R.string.exchange_no_sources
        Kind.INVALID_DATA -> R.string.exchange_invalid_data
        Kind.UNVERIFIED_APP -> R.string.exchange_unverified_app
        Kind.SYSTEM -> R.string.exchange_system_error
        Kind.UNEXPECTED -> R.string.exchange_failed
    }

    fun diagnostic(operation: Operation, error: Exception?): String = buildString {
        append("[CredentialExchange] operation=").append(operation.name)
        append(" result=").append(if (error == null) "SUCCESS" else "FAILURE")
        if (error != null) {
            append(" kind=").append(classify(error).name)
            append(" exception=").append(error.javaClass.simpleName)
        }
    }

    fun decodeDiagnostic(decoded: CxfCredentialCodec.Decoded): String = buildString {
        append(" decodedPasswords=").append(decoded.passwordCount)
        append(" decodedPasskeys=").append(decoded.passkeyCount)
        for (reason in CxfCredentialCodec.SkipReason.entries) {
            val count = decoded.skipped[reason] ?: 0
            if (count > 0) append(" skipped_").append(reason.name).append('=').append(count)
        }
    }

    fun record(context: Context, operation: Operation, error: Exception? = null,
        decoded: CxfCredentialCodec.Decoded? = null) {
        // Diagnostics must never turn a completed transfer into an error.
        runCatching {
            val gmsVersion = runCatching {
                @Suppress("DEPRECATION")
                val info = context.packageManager.getPackageInfo("com.google.android.gms", 0)
                if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
            }.getOrNull()
            val line = "${diagnostic(operation, error)} api=${Build.VERSION.SDK_INT} gms=${gmsVersion ?: "unknown"}" +
                (decoded?.let(::decodeDiagnostic) ?: "")
            if (error == null) Log.i("CredentialExchange", line) else Log.w("CredentialExchange", line)
            SecurityDiagLogger.initialize(context.applicationContext)
            SecurityDiagLogger.append(line)
        }
    }
}
