// Supabase Edge Function: verify-paystack-payment
// Description: Server-side Paystack transaction verification for Student Market Hub.
// Keeps Paystack Secret Key strictly server-side.
// Verifies reference, amount, currency, and transaction status before transitioning orders to PAID.

import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.21.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Content-Type": "application/json",
};

interface VerifyRequest {
  order_id: string;
  reference: string;
  expected_amount: number;
  expected_currency?: string;
}

serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return new Response(
      JSON.stringify({ success: false, error: "Method not allowed. Use POST." }),
      { status: 405, headers: corsHeaders }
    );
  }

  try {
    const body: VerifyRequest = await req.json();
    const { order_id, reference, expected_amount, expected_currency = "NGN" } = body;

    if (!order_id || !reference || typeof expected_amount !== "number" || expected_amount <= 0) {
      return new Response(
        JSON.stringify({
          success: false,
          error: "Missing or invalid parameters: order_id, reference, and positive expected_amount are required.",
          code: "INVALID_PARAMETERS",
        }),
        { status: 400, headers: corsHeaders }
      );
    }

    const paystackSecretKey = Deno.env.get("PAYSTACK_SECRET_KEY");
    if (!paystackSecretKey) {
      return new Response(
        JSON.stringify({
          success: false,
          error: "PAYSTACK_SECRET_KEY is not configured in server environment.",
          code: "SERVER_CONFIG_MISSING",
        }),
        { status: 500, headers: corsHeaders }
      );
    }

    const paystackUrl = `https://api.paystack.co/transaction/verify/${encodeURIComponent(reference)}`;
    const paystackRes = await fetch(paystackUrl, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${paystackSecretKey}`,
        "Content-Type": "application/json",
      },
    });

    if (!paystackRes.ok) {
      const errText = await paystackRes.text();
      return new Response(
        JSON.stringify({
          success: false,
          error: `Paystack verification request failed with HTTP ${paystackRes.status}`,
          details: errText,
          code: "PAYSTACK_HTTP_ERROR",
        }),
        { status: 400, headers: corsHeaders }
      );
    }

    const paystackData = await paystackRes.json();
    if (!paystackData.status || !paystackData.data) {
      return new Response(
        JSON.stringify({
          success: false,
          error: paystackData.message || "Invalid transaction reference",
          code: "INVALID_REFERENCE",
        }),
        { status: 400, headers: corsHeaders }
      );
    }

    const tx = paystackData.data;

    if (tx.status !== "success") {
      return new Response(
        JSON.stringify({
          success: false,
          error: `Payment was not successful. Transaction status is: ${tx.status}`,
          status: tx.status,
          code: "TRANSACTION_NOT_SUCCESSFUL",
        }),
        { status: 400, headers: corsHeaders }
      );
    }

    const expectedKobo = Math.round(expected_amount * 100);
    if (tx.amount !== expectedKobo) {
      return new Response(
        JSON.stringify({
          success: false,
          error: `Amount mismatch: expected ₦${expected_amount} (${expectedKobo} kobo), received ₦${tx.amount / 100} (${tx.amount} kobo).`,
          expected_kobo: expectedKobo,
          received_kobo: tx.amount,
          code: "AMOUNT_MISMATCH",
        }),
        { status: 400, headers: corsHeaders }
      );
    }

    const receivedCurrency = (tx.currency || "").toUpperCase();
    if (receivedCurrency !== expected_currency.toUpperCase()) {
      return new Response(
        JSON.stringify({
          success: false,
          error: `Currency mismatch: expected ${expected_currency}, received ${receivedCurrency}.`,
          code: "CURRENCY_MISMATCH",
        }),
        { status: 400, headers: corsHeaders }
      );
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

    if (supabaseUrl && supabaseServiceKey) {
      const supabase = createClient(supabaseUrl, supabaseServiceKey);

      const { data: existingOrder, error: fetchError } = await supabase
        .from("orders")
        .select("id, status, total_amount, paystack_ref")
        .eq("id", order_id)
        .single();

      if (fetchError) {
        console.error("Order lookup error:", fetchError);
      } else if (existingOrder) {
        if (existingOrder.status === "PAID" || existingOrder.status === "DELIVERED") {
          return new Response(
            JSON.stringify({
              success: true,
              message: "Order was already verified and marked as PAID (idempotent).",
              order_id: existingOrder.id,
              reference: reference,
              status: existingOrder.status,
            }),
            { status: 200, headers: corsHeaders }
          );
        }

        const { error: updateError } = await supabase
          .from("orders")
          .update({
            status: "PAID",
            payment_status: "SUCCESSFUL",
            paystack_ref: reference,
            updated_at: new Date().toISOString(),
          })
          .eq("id", order_id);

        if (updateError) {
          console.error("Order status update error:", updateError);
        }
      }
    }

    return new Response(
      JSON.stringify({
        success: true,
        message: "Payment successfully verified by Paystack gateway.",
        order_id: order_id,
        reference: reference,
        status: "PAID",
        amount_paid: tx.amount / 100,
        currency: tx.currency,
        paid_at: tx.paid_at,
      }),
      { status: 200, headers: corsHeaders }
    );
  } catch (error) {
    return new Response(
      JSON.stringify({
        success: false,
        error: error instanceof Error ? error.message : "Internal verification error",
        code: "INTERNAL_ERROR",
      }),
      { status: 500, headers: corsHeaders }
    );
  }
});
