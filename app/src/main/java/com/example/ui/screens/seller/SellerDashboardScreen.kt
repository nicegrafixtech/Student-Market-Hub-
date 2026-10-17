package com.example.ui.screens.seller

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
import com.example.data.order.OrderStatus
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SellerDashboardScreen(
    store: Store?,
    products: List<Product>,
    orders: List<Order>,
    subscription: Subscription?,
    onAddProductClick: () -> Unit,
    onEditProductClick: (Product) -> Unit,
    onDeleteProductClick: (Product) -> Unit,
    onUpdateOrderStatus: (orderId: String, newStatus: OrderStatus, reason: String?) -> Unit,
    onRenewSubscription: () -> Unit,
    onSaveStoreDetails: (Store) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Listings, 1: Orders, 2: Store Settings

    val grossRevenue = remember(orders) {
        orders.filter { it.status != OrderStatus.CANCELLED.name }.sumOf { it.totalAmount }
    }
    val totalOrders = remember(orders) { orders.size }
    val activeListingsCount = remember(products) { products.filter { it.status == "ACTIVE" }.size }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 14.dp)
            .testTag("seller_dashboard"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
    ) {
        // Store Header Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(store?.logoEmoji ?: "🏪", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    store?.name ?: "Campus Seller Hub",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    store?.campus ?: "UNILAG",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                "VERIFIED SELLER",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        store?.description ?: "Sell top tech gadgets, fashion, and study materials to students.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Seller Subscription Card
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Seller Plan: ₦1,000/month", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandNavy)
                                Text("Unlimited campus listings & store verification", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (subscription?.status == "ACTIVE") Color(0xFF10B981) else BrandOrange
                        ) {
                            Text(
                                text = if (subscription?.status == "ACTIVE") "ACTIVE" else "RENEW",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (subscription != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Paystack Reference:", fontSize = 11.sp, color = TextSecondary)
                            Text(subscription.paystackRef, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onRenewSubscription,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (subscription?.status == "ACTIVE") "Manage Subscription (Paystack)" else "Pay ₦1,000 Monthly Sub (Paystack)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Gross Sales",
                    value = "₦" + String.format(Locale.US, "%,.0f", grossRevenue),
                    color = BrandOrangeDark,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Orders",
                    value = totalOrders.toString(),
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Listings",
                    value = activeListingsCount.toString(),
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BrandOrange,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Listings (${products.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Orders (${orders.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Products Tab
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Catalog", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandNavy)
                        Button(
                            onClick = onAddProductClick,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp).testTag("seller_add_product_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Listing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(products, key = { it.id }) { product ->
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
                            Text(product.iconEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy, maxLines = 1)
                                Text(
                                    "₦" + String.format(Locale.US, "%,.0f", product.discountPrice ?: product.price) + " • Qty: ${product.quantity}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            IconButton(onClick = { onEditProductClick(product) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandNavy, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeleteProductClick(product) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            1 -> {
                // Orders Dispatch Workflow
                items(orders, key = { it.id }) { order ->
                    val statusEnum = OrderStatus.fromString(order.status)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Order #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy)
                                Text(
                                    "₦" + String.format(Locale.US, "%,.0f", order.totalAmount),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = BrandOrangeDark
                                )
                            }
                            Text("Buyer: ${order.customerName} (${order.contactPhone})", fontSize = 11.sp, color = TextSecondary)
                            Text("Hostel: ${order.deliveryHostel}, ${order.deliveryCampus}", fontSize = 11.sp, color = TextSecondary)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorderLight)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(statusEnum.badgeColorHex)
                                ) {
                                    Text(
                                        statusEnum.displayName,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    when (statusEnum) {
                                        OrderStatus.PAID -> {
                                            Button(
                                                onClick = { onUpdateOrderStatus(order.id, OrderStatus.PROCESSING, null) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Process", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        OrderStatus.PROCESSING -> {
                                            Button(
                                                onClick = { onUpdateOrderStatus(order.id, OrderStatus.DISPATCHED, null) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Dispatch", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        OrderStatus.DISPATCHED -> {
                                            Button(
                                                onClick = { onUpdateOrderStatus(order.id, OrderStatus.DELIVERED, null) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Mark Delivered", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Store Settings Tab
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Store Profile Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandNavy)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Store Name: ${store?.name ?: "Femzy Gadgets"}", fontSize = 12.sp, color = TextSecondary)
                            Text("Hostel Pickup: ${store?.hostelLocation ?: "Jaja Hall"}", fontSize = 12.sp, color = TextSecondary)
                            Text("WhatsApp Contact: ${store?.whatsappNumber ?: "+2348012345678"}", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
