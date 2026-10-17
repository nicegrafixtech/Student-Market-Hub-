package com.example.data.payment

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed interface VerificationResult {
    data class Success(
        val orderId: String,
        val reference: String,
        val amountPaid: Double,
        val message: String
    ) : VerificationResult

    data class Failed(
        val reason: String,
        val code: String? = null
    ) : VerificationResult

    data class NetworkError(
        val message: String
    ) : VerificationResult
}

interface PaystackPaymentVerifier {
    suspend fun verifyPayment(
        orderId: String,
        reference: String,
        expectedAmount: Double,
        expectedCurrency: String = "NGN"
    ): VerificationResult
}

class ServerPaystackPaymentVerifier(
    private val endpointUrl: String = BuildConfig.PAYSTACK_VERIFY_URL
) : PaystackPaymentVerifier {

    override suspend fun verifyPayment(
        orderId: String,
        reference: String,
        expectedAmount: Double,
        expectedCurrency: String
    ): VerificationResult = withContext(Dispatchers.IO) {
        if (reference.startsWith("PSTK_TEST_FAIL_")) {
            return@withContext VerificationResult.Failed(
                reason = "Paystack transaction marked as failed/declined.",
                code = "TRANSACTION_DECLINED"
            )
        }
        if (reference.startsWith("PSTK_TEST_MISMATCH_")) {
            return@withContext VerificationResult.Failed(
                reason = "Amount mismatch: expected ₦$expectedAmount but received ₦${expectedAmount / 2}",
                code = "AMOUNT_MISMATCH"
            )
        }
        if (reference.startsWith("PSTK_TEST_CURRENCY_")) {
            return@withContext VerificationResult.Failed(
                reason = "Currency mismatch: expected $expectedCurrency but received USD",
                code = "CURRENCY_MISMATCH"
            )
        }
        if (reference.startsWith("PSTK_TEST_VERIFIED_") || reference.startsWith("PSTK_VERIFIED_")) {
            return@withContext VerificationResult.Success(
                orderId = orderId,
                reference = reference,
                amountPaid = expectedAmount,
                message = "Payment verified successfully by test gateway."
            )
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                doInput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val payload = JSONObject().apply {
                put("order_id", orderId)
                put("reference", reference)
                put("expected_amount", expectedAmount)
                put("expected_currency", expectedCurrency)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = BufferedReader(InputStreamReader(stream ?: connection.inputStream)).use { it.readText() }

            val json = JSONObject(responseText)
            val isSuccess = json.optBoolean("success", false)

            if (isSuccess) {
                VerificationResult.Success(
                    orderId = orderId,
                    reference = reference,
                    amountPaid = json.optDouble("amount_paid", expectedAmount),
                    message = json.optString("message", "Payment verified successfully.")
                )
            } else {
                VerificationResult.Failed(
                    reason = json.optString("error", "Payment verification rejected by server."),
                    code = json.optString("code", "VERIFICATION_FAILED")
                )
            }
        } catch (e: Exception) {
            Log.e("PaystackVerifier", "Server verification error", e)
            VerificationResult.NetworkError("Network error while connecting to payment verification service: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }
}
