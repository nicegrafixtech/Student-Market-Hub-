package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun CartCheckoutScreen(
    cartItems: List<Pair<Product, Int>>,
    onIncreaseQty: (String) -> Unit,
    onDecreaseQty: (String) -> Unit,
    onCheckout: (deliveryHostel: String, deliveryCampus: String, phone: String) -> Unit,
    onBrowseProducts: () -> Unit
) {
    var deliveryHostel by remember { mutableStateOf("Jaja Hall, Room B12") }
    var deliveryCampus by remember { mutableStateOf("University of Lagos (UNILAG)") }
    var contactPhone by remember { mutableStateOf("+234 812 456 7890") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val subtotal = remember(cartItems) {
        cartItems.sumOf { (product, qty) ->
            (product.discountPrice ?: product.price) * qty
        }
    }
    val deliveryFee = 500.0
    val total = if (cartItems.isNotEmpty()) subtotal + deliveryFee else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 14.dp)
            .testTag("cart_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
    ) {
        item {
            Text(
                "My Shopping Cart (${cartItems.sumOf { it.second }})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = BrandNavy
            )
        }

        if (cartItems.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Your cart is empty", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandNavy)
                        Text("Find textbooks, gadgets, and shoes on campus", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBrowseProducts,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Browse Marketplace", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            items(cartItems, key = { it.first.id }) { (product, qty) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BackgroundLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(product.iconEmoji, fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy, maxLines = 1)
                            val price = product.discountPrice ?: product.price
                            Text(
                                "₦" + String.format(Locale.US, "%,.0f", price) + " each",
                                fontSize = 11.sp,
                                color = BrandOrangeDark,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onDecreaseQty(product.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = BrandNavy)
                            }
                            Text(
                                qty.toString(),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { onIncreaseQty(product.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandNavy)
                            }
                        }
                    }
                }
            }

            // Delivery Details Section
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Doorstep Hostel Delivery", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = deliveryHostel,
                            onValueChange = { deliveryHostel = it },
                            label = { Text("Hostel & Room Number") },
                            placeholder = { Text("e.g. Jaja Hall, Room B12") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("WhatsApp Contact Phone") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Order Price Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Items Subtotal", fontSize = 12.sp, color = TextSecondary)
                            Text("₦" + String.format(Locale.US, "%,.0f", subtotal), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hostel Doorstep Delivery", fontSize = 12.sp, color = TextSecondary)
                            Text("₦" + String.format(Locale.US, "%,.0f", deliveryFee), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorderLight)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payable", fontSize = 14.sp, fontWeight = FontWeight.Black, color = BrandNavy)
                            Text(
                                "₦" + String.format(Locale.US, "%,.0f", total),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandOrangeDark
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (deliveryHostel.trim().length < 3) {
                                    errorMessage = "Please enter your hostel name and room number"
                                } else {
                                    onCheckout(deliveryHostel, deliveryCampus, contactPhone)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3F7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("checkout_paystack_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pay with Paystack", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
