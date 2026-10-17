package com.example.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.ui.theme.*

@Composable
fun AddEditProductDialog(
    initialProduct: Product? = null,
    defaultCampus: String = "University of Lagos (UNILAG)",
    onSave: (
        name: String,
        description: String,
        price: Double,
        discountPrice: Double?,
        category: String,
        condition: String,
        quantity: Int,
        campus: String,
        iconEmoji: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var priceText by remember { mutableStateOf(initialProduct?.price?.toString() ?: "") }
    var discountPriceText by remember { mutableStateOf(initialProduct?.discountPrice?.toString() ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Phones & Accessories") }
    var condition by remember { mutableStateOf(initialProduct?.condition ?: "Brand New") }
    var quantityText by remember { mutableStateOf(initialProduct?.quantity?.toString() ?: "1") }
    var iconEmoji by remember { mutableStateOf(initialProduct?.iconEmoji ?: "📦") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Phones & Accessories", "Books & Stationery", "Shoes & Bags", "Fashion", "Electronics", "Beauty & Care", "Hostel Essentials")
    val conditions = listOf("Brand New", "Like New", "Fairly Used")
    val emojis = listOf("📦", "🎧", "📚", "👟", "🔋", "💻", "👗", "🎒", "⚡", "👕")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_edit_product_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (initialProduct == null) "New Campus Listing" else "Edit Listing",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = BrandNavy
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorderLight)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Title") },
                    placeholder = { Text("e.g. Calculus Textbook, Power Bank") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price (₦)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discountPriceText,
                        onValueChange = { discountPriceText = it },
                        label = { Text("Discount ₦") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Condition Details") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Emoji Picker Row
                Text("Select Icon / Motif", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    emojis.take(6).forEach { em ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (iconEmoji == em) BrandOrange.copy(alpha = 0.2f) else BackgroundLight,
                            border = if (iconEmoji == em) androidx.compose.foundation.BorderStroke(1.dp, BrandOrange) else null,
                            onClick = { iconEmoji = em },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(em, fontSize = 18.sp)
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val p = priceText.toDoubleOrNull()
                        val dp = discountPriceText.toDoubleOrNull()
                        val q = quantityText.toIntOrNull() ?: 1

                        if (name.trim().length < 3) {
                            errorMessage = "Please enter title (at least 3 characters)"
                        } else if (p == null || p <= 0) {
                            errorMessage = "Please enter a valid price in Naira"
                        } else {
                            onSave(name, description, p, dp, category, condition, q, defaultCampus, iconEmoji)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        if (initialProduct == null) "Publish to Marketplace" else "Save Changes",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
