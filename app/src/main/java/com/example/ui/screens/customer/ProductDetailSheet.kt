package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailSheet(
    product: Product,
    reviews: List<Review>,
    onAddToCart: () -> Unit,
    onContactSeller: () -> Unit,
    onSubmitReview: (rating: Int, comment: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reviewRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var isSubmittingReview by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.testTag("product_detail_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Image/Emoji
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BackgroundLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(product.iconEmoji, fontSize = 72.sp)

                    Surface(
                        color = BrandNavy.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            product.condition,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Title and Price
            item {
                Column {
                    Text(
                        text = product.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val displayPrice = product.discountPrice ?: product.price
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₦" + String.format(Locale.US, "%,.0f", displayPrice),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandOrangeDark
                            )
                            if (product.discountPrice != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₦" + String.format(Locale.US, "%,.0f", product.price),
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (product.quantity > 0) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (product.quantity > 0) "${product.quantity} in stock" else "Out of stock",
                                color = if (product.quantity > 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Store Info Card
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏪", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.storeName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy)
                            Text("Campus: ${product.campus}", fontSize = 11.sp, color = TextSecondary)
                        }
                        IconButton(onClick = onContactSeller) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = BrandNavy)
                        }
                    }
                }
            }

            // Description
            item {
                Column {
                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        product.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onContactSeller,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Seller", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    }

                    Button(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Reviews Section
            item {
                Text("Verified Student Reviews (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandNavy)
            }

            items(reviews, key = { it.id }) { rev ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BackgroundLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(rev.customerName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BrandNavy)
                            Text(rev.dateText, fontSize = 10.sp, color = TextMuted)
                        }
                        Row {
                            repeat(rev.rating) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(12.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(rev.comment, fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            // Add Review
            item {
                Column(modifier = Modifier.padding(bottom = 20.dp)) {
                    Text("Leave a Review", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandNavy)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        placeholder = { Text("How was the product and hostel delivery?", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (reviewComment.isNotBlank()) {
                                onSubmitReview(reviewRating, reviewComment)
                                reviewComment = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Submit Review", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
