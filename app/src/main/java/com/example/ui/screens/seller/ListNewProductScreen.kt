package com.example.ui.screens.seller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListNewProductScreen(
    defaultCampus: String = "University of Lagos (UNILAG)",
    onPublishProduct: (
        title: String,
        description: String,
        price: Double,
        discountPrice: Double?,
        imageUrl: String,
        category: String,
        condition: String,
        quantity: Int,
        campus: String,
        iconEmoji: String
    ) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var discountPriceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Phones & Accessories") }
    var condition by remember { mutableStateOf("Brand New") }
    var quantityText by remember { mutableStateOf("1") }
    var campus by remember { mutableStateOf(if (defaultCampus == "All Campuses") "University of Lagos (UNILAG)" else defaultCampus) }
    var iconEmoji by remember { mutableStateOf("📦") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Phones & Accessories",
        "Books & Stationery",
        "Shoes & Bags",
        "Fashion",
        "Electronics",
        "Beauty & Care",
        "Hostel Essentials"
    )
    val conditions = listOf("Brand New", "Like New", "Fairly Used")
    val emojis = listOf("📦", "🎧", "📚", "👟", "🔋", "💻", "👗", "🎒", "⚡", "👕")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "List New Item for Sale",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = BrandNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("list_product_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .testTag("list_new_product_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Image / Emoji Preview Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Product Preview",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BackgroundLight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = title.ifBlank { "Product image preview" },
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(iconEmoji, fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Enter an Image URL below or pick a category icon",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Main Listing Form Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Item Details",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandNavy
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Product Title *") },
                        placeholder = { Text("e.g. Engineering Mathematics Textbook, Oraimo Earbuds") },
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_title_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price (₦) *") },
                            placeholder = { Text("e.g. 6500") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_price_input")
                        )

                        OutlinedTextField(
                            value = discountPriceText,
                            onValueChange = { discountPriceText = it },
                            label = { Text("Discount Price (₦)") },
                            placeholder = { Text("Optional") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_discount_input")
                        )
                    }

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") },
                        placeholder = { Text("https://images.unsplash.com/...") },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_image_url_input")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        placeholder = { Text("Describe condition, included accessories, and hostel pickup/delivery info") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_description_input")
                    )

                    // Condition Selector
                    Text("Condition", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        conditions.forEach { cond ->
                            val selected = condition == cond
                            FilterChip(
                                selected = selected,
                                onClick = { condition = cond },
                                label = { Text(cond, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Category Selector
                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }

                    // Fallback Emoji Selector
                    Text("Fallback Icon Motif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        emojis.take(7).forEach { em ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (iconEmoji == em) BrandOrange.copy(alpha = 0.2f) else BackgroundLight,
                                border = if (iconEmoji == em) androidx.compose.foundation.BorderStroke(1.dp, BrandOrange) else null,
                                onClick = { iconEmoji = em },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(em, fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val parsedPrice = priceText.toDoubleOrNull()
                            val parsedDiscount = discountPriceText.toDoubleOrNull()
                            val parsedQty = quantityText.toIntOrNull() ?: 1

                            when {
                                title.trim().length < 3 -> {
                                    errorMessage = "Please enter a product title (at least 3 characters)."
                                }
                                parsedPrice == null || parsedPrice <= 0 -> {
                                    errorMessage = "Please enter a valid price in Naira."
                                }
                                description.trim().length < 5 -> {
                                    errorMessage = "Please provide a brief description of your item."
                                }
                                else -> {
                                    errorMessage = null
                                    onPublishProduct(
                                        title.trim(),
                                        description.trim(),
                                        parsedPrice,
                                        parsedDiscount,
                                        imageUrl.trim(),
                                        category,
                                        condition,
                                        parsedQty,
                                        campus,
                                        iconEmoji
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("publish_product_button")
                    ) {
                        Icon(Icons.Default.Publish, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "List Item for Sale",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
