package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.order.OrderStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerOrdersScreen(
    orders: List<Order>,
    onConfirmDelivery: (String) -> Unit,
    onCancelOrder: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 14.dp)
            .testTag("orders_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
    ) {
        item {
            Text(
                "My Campus Orders (${orders.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = BrandNavy
            )
        }

        if (orders.isEmpty()) {
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
                        Text("📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No orders placed yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandNavy)
                        Text("Your purchases with hostel delivery will appear here", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(orders, key = { it.id }) { order ->
                val statusEnum = OrderStatus.fromString(order.status)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                    modifier = Modifier.fillMaxWidth().testTag("order_item_${order.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Order #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy)
                                Text("Store: ${order.storeName}", fontSize = 11.sp, color = TextSecondary)
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(statusEnum.badgeColorHex)
                            ) {
                                Text(
                                    text = statusEnum.displayName,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorderLight)

                        // 4-Step Order Progress Tracker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OrderStepDot("Paid", statusEnum.ordinal >= OrderStatus.PAID.ordinal)
                            OrderStepDot("Processing", statusEnum.ordinal >= OrderStatus.PROCESSING.ordinal)
                            OrderStepDot("Dispatched", statusEnum.ordinal >= OrderStatus.DISPATCHED.ordinal)
                            OrderStepDot("Delivered", statusEnum.ordinal >= OrderStatus.DELIVERED.ordinal)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery To:", fontSize = 11.sp, color = TextMuted)
                            Text("${order.deliveryHostel}, ${order.deliveryCampus}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = BrandNavy)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Paystack Ref:", fontSize = 11.sp, color = TextMuted)
                            Text(order.paystackRef, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Paid:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                            Text(
                                "₦" + String.format(Locale.US, "%,.0f", order.totalAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandOrangeDark
                            )
                        }

                        if (statusEnum == OrderStatus.DISPATCHED) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onConfirmDelivery(order.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(38.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirm I Have Received Order", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (statusEnum == OrderStatus.PENDING) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onCancelOrder(order.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text("Cancel Order", color = Color(0xFFEF4444), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStepDot(title: String, isPassed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    if (isPassed) Color(0xFF10B981) else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(6.dp)
                )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(title, fontSize = 9.sp, color = if (isPassed) BrandNavy else TextMuted, fontWeight = FontWeight.Medium)
    }
}
