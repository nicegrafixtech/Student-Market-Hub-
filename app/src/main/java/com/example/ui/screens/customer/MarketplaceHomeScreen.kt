package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceHomeScreen(
    products: List<Product>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    selectedCampus: String,
    onCampusSelect: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (String) -> Unit,
    onOpenServices: () -> Unit
) {
    val categories = listOf(
        "All",
        "Phones & Accessories",
        "Books & Stationery",
        "Shoes & Bags",
        "Fashion",
        "Electronics",
        "Beauty & Care",
        "Hostel Essentials"
    )

    val campuses = listOf(
        "All Campuses",
        "University of Lagos (UNILAG)",
        "Lagos State University (LASU)",
        "University of Ibadan (UI)",
        "Obafemi Awolowo University (OAU)",
        "University of Benin (UNIBEN)"
    )

    var campusDropdownExpanded by remember { mutableStateOf(false) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 14.dp)
            .testTag("marketplace_grid"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
    ) {
        // 1. Campus Location Selector
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable { campusDropdownExpanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedCampus,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TextSecondary
                )

                DropdownMenu(
                    expanded = campusDropdownExpanded,
                    onDismissRequest = { campusDropdownExpanded = false }
                ) {
                    campuses.forEach { campus ->
                        DropdownMenuItem(
                            text = { Text(campus, fontSize = 12.sp) },
                            onClick = {
                                onCampusSelect(campus)
                                campusDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 2. Top Search Bar for instant filtering
        item(span = { GridItemSpan(2) }) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search products, books, gadgets...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandOrange,
                    unfocusedBorderColor = CardBorderLight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar_input")
            )
        }

        // 3. Hero Campus Marketplace Banner
        item(span = { GridItemSpan(2) }) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BrandNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BrandOrange
                        ) {
                            Text(
                                "NIGERIAN CAMPUS HUB",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Smart Deals, Bigger Dreams",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Doorstep hostel delivery & student escrow payments.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    }
                    Text("🛍️", fontSize = 42.sp)
                }
            }
        }

        // 4. Category Filter Chips
        item(span = { GridItemSpan(2) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) BrandOrange else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) BrandOrange else CardBorderLight
                        ),
                        modifier = Modifier.clickable { onCategorySelect(cat) }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else BrandNavy,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5. Section Header
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "Campus Listings (${products.size})" else "Search Results (${products.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
                TextButton(onClick = onOpenServices) {
                    Text("Campus Services ⚡", fontSize = 12.sp, color = BrandOrangeDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. Products Grid Items
        if (products.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No matching products found",
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        fontSize = 14.sp
                    )
                    Text(
                        "Try a different search query or select 'All' categories",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product) },
                    onAddToCart = { onAddToCart(product.id) }
                )
            }
        }
    }
}
