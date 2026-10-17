package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketHubTopBar(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    selectedCampus: String,
    onCampusSelected: (String) -> Unit,
    cartItemCount: Int,
    onCartClicked: () -> Unit
) {
    var showCampusMenu by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }

    val campuses = listOf(
        "All Campuses",
        "University of Lagos (UNILAG)",
        "University of Ibadan (UI)",
        "Obafemi Awolowo University (OAU)",
        "Federal University of Technology Akure (FUTA)",
        "University of Benin (UNIBEN)",
        "Lagos State University (LASU)",
        "Ahmadu Bello University (ABU)"
    )

    Surface(
        color = BrandNavy,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🛍️",
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STUDENT",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "MARKET HUB",
                                color = BrandOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Smart Deals, Bigger Dreams",
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Actions: Role Switcher Pill & Cart
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Role Badge Button
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = when (currentRole) {
                                UserRole.CUSTOMER -> BrandOrange.copy(alpha = 0.25f)
                                UserRole.SELLER -> Color(0xFF10B981).copy(alpha = 0.25f)
                                UserRole.ADMIN -> Color(0xFF6366F1).copy(alpha = 0.25f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (currentRole) {
                                    UserRole.CUSTOMER -> BrandOrange
                                    UserRole.SELLER -> Color(0xFF10B981)
                                    UserRole.ADMIN -> Color(0xFF6366F1)
                                }
                            ),
                            modifier = Modifier
                                .clickable { showRoleMenu = true }
                                .testTag("role_switcher_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (currentRole) {
                                        UserRole.CUSTOMER -> "🛒 Buyer"
                                        UserRole.SELLER -> "🏪 Seller"
                                        UserRole.ADMIN -> "🛡️ Admin"
                                    },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Role",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🛒 Customer / Student Buyer", fontWeight = FontWeight.Bold)
                                        Text("Browse, order & contact student sellers", fontSize = 11.sp, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    onRoleSelected(UserRole.CUSTOMER)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🏪 Student Seller (₦1,000/mo)", fontWeight = FontWeight.Bold, color = BrandOrangeDark)
                                        Text("Manage store, products & sales orders", fontSize = 11.sp, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    onRoleSelected(UserRole.SELLER)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🛡️ Marketplace Admin", fontWeight = FontWeight.Bold, color = BrandNavy)
                                        Text("Moderation, verify sellers & transactions", fontSize = 11.sp, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    onRoleSelected(UserRole.ADMIN)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }

                    // Cart Icon (For Customer)
                    if (currentRole == UserRole.CUSTOMER) {
                        IconButton(
                            onClick = onCartClicked,
                            modifier = Modifier.testTag("topbar_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge(
                                            containerColor = BrandOrange,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = cartItemCount.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Shopping Cart",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Campus Selector Bar (Sub-header)
            if (currentRole == UserRole.CUSTOMER) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandNavyLight)
                        .clickable { showCampusMenu = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("campus_picker_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Campus Location",
                            tint = BrandOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedCampus == "All Campuses") "Campus: All Nigerian Campuses" else "Campus: $selectedCampus",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "Change",
                        color = BrandOrangeLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = showCampusMenu,
                    onDismissRequest = { showCampusMenu = false }
                ) {
                    campuses.forEach { campus ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (campus == selectedCampus) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = BrandOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = campus,
                                        fontWeight = if (campus == selectedCampus) FontWeight.Bold else FontWeight.Normal,
                                        color = if (campus == selectedCampus) BrandOrange else TextPrimary
                                    )
                                }
                            },
                            onClick = {
                                onCampusSelected(campus)
                                showCampusMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}
