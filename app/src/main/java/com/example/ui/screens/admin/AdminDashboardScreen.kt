package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.Subscription
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    products: List<Product>,
    stores: List<Store>,
    orders: List<Order>,
    subscriptions: List<Subscription>,
    onApproveProduct: (String) -> Unit,
    onRejectProduct: (String) -> Unit,
    onToggleFeature: (String, Boolean) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onToggleStoreVerification: (String, Boolean) -> Unit
) {
    var adminTab by remember { mutableStateOf(0) }

    val totalGMV = remember(orders) { orders.sumOf { it.totalAmount } }
    val totalSubscriptionRevenue = remember(subscriptions) { subscriptions.sumOf { it.amount } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 14.dp)
            .testTag("admin_dashboard"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
    ) {
        // Admin Command Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BrandNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🛡️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Admin Control Hub",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                            }
                            Text(
                                "Student Market Hub • Nigeria",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF6366F1)
                        ) {
                            Text(
                                "Super Admin",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Marketplace GMV", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(
                                    "₦" + String.format(Locale.US, "%,.0f", totalGMV),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Subscription Rev", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(
                                    "₦" + String.format(Locale.US, "%,.0f", totalSubscriptionRevenue),
                                    color = AccentGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Navigation
        item {
            TabRow(
                selectedTabIndex = adminTab,
                containerColor = Color.White,
                contentColor = BrandNavy,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = adminTab == 0,
                    onClick = { adminTab = 0 },
                    text = { Text("Moderate (${products.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == 1,
                    onClick = { adminTab = 1 },
                    text = { Text("Stores (${stores.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == 2,
                    onClick = { adminTab = 2 },
                    text = { Text("Payments (${orders.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (adminTab) {
            0 -> {
                // Products Moderation List
                items(products, key = { it.id }) { product ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(product.iconEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy, maxLines = 1)
                                    Text(
                                        "₦" + String.format(Locale.US, "%,.0f", product.discountPrice ?: product.price) + " • Store: ${product.storeName}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text("Campus: ${product.campus}", fontSize = 10.sp, color = TextMuted)
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (product.status) {
                                        "ACTIVE" -> Color(0xFF10B981)
                                        "REJECTED" -> Color(0xFFEF4444)
                                        else -> Color(0xFFF59E0B)
                                    }
                                ) {
                                    Text(
                                        product.status,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onToggleFeature(product.id, product.isFeatured) }
                                ) {
                                    Icon(
                                        imageVector = if (product.isFeatured) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (product.isFeatured) AccentGold else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (product.isFeatured) "Featured" else "Feature",
                                        fontSize = 11.sp,
                                        color = if (product.isFeatured) BrandOrangeDark else TextSecondary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (product.status != "ACTIVE") {
                                        Button(
                                            onClick = { onApproveProduct(product.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Approve", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (product.status != "REJECTED") {
                                        OutlinedButton(
                                            onClick = { onRejectProduct(product.id) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Reject", fontSize = 10.sp, color = Color(0xFFEF4444))
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteProduct(product.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Stores Verification
                items(stores, key = { it.id }) { store ->
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(store.logoEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(store.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy)
                                    Text("Campus: ${store.campus}", fontSize = 11.sp, color = TextSecondary)
                                    Text("Hostel: ${store.hostelLocation}", fontSize = 10.sp, color = TextMuted)
                                }
                            }

                            Button(
                                onClick = { onToggleStoreVerification(store.id, store.isVerified) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (store.isVerified) Color(0xFF10B981) else BrandNavy
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(if (store.isVerified) "Verified" else "Verify", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            2 -> {
                // Payments & Paystack Orders
                items(orders, key = { it.id }) { order ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ref: ${order.paystackRef}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text(
                                    "₦" + String.format(Locale.US, "%,.0f", order.totalAmount),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = BrandOrangeDark
                                )
                            }
                            Text("Customer: ${order.customerName} • Store: ${order.storeName}", fontSize = 11.sp, color = TextSecondary)
                            Text("Status: ${order.status} • Payment: ${order.paymentStatus}", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
