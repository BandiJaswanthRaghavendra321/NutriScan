package uk.ac.tees.mad.nutriscan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import uk.ac.tees.mad.nutriscan.data.local.ProductLocal
import uk.ac.tees.mad.nutriscan.ui.theme.*
import uk.ac.tees.mad.nutriscan.ui.viewmodels.MainVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    homeVm: MainVM = hiltViewModel(),
    navController: NavController,
    onItemClick: (ProductLocal) -> Unit,
    onDeleteItem: (ProductLocal) -> Unit
) {
    val history by homeVm.productRoom.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", color = White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        },
        bottomBar = {
            BottomBar(navController = navController)
        },
        containerColor = GreenLight
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No products scanned yet 😔\nScan something to get started!",
                    color = GreenDark,
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(GreenLight)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { item ->
                    HistoryCard(
                        product = item,
                        onClick = { onItemClick(item) },
                        onDelete = { onDeleteItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    product: ProductLocal,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(product.image_url),
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenLight),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = product.product_name ?: "Unknown Product",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.brands ?: "Unknown Brand",
                    fontSize = 14.sp,
                    color = GrayMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!product.categories.isNullOrEmpty()) {
                    Text(
                        text = product.categories,
                        fontSize = 13.sp,
                        color = GrayMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!product.nutriscore_grade.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (product.nutriscore_grade.lowercase()) {
                                "a" -> Color(0xFF2ECC71)
                                "b" -> Color(0xFF7DCEA0)
                                "c" -> Color(0xFFF4D03F)
                                "d" -> Color(0xFFE67E22)
                                "e" -> Color(0xFFE74C3C)
                                else -> GrayMedium
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = product.nutriscore_grade.uppercase(),
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "NutriScan – History Screen")
@Composable
fun HistoryScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)) // GreenLight
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4CAF50))
                .padding(16.dp)
        ) {
            Text(
                "History",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Sample History Items
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) { index ->
                val names = listOf(
                    "Lindt Dark Chocolate 70%",
                    "Alpro Oat Milk Unsweetened",
                    "Kellogg's Corn Flakes",
                    "Heinz Tomato Ketchup"
                )
                val brands = listOf("Lindt", "Alpro", "Kellogg's", "Heinz")
                val grades = listOf("B", "A", "C", "D")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Image Placeholder
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("IMG", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                names[index],
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(brands[index], fontSize = 14.sp, color = Color(0xFF757575))
                            Text("Snack, Chocolate", fontSize = 13.sp, color = Color(0xFF757575))
                        }

                        // NutriScore Badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (grades[index]) {
                                        "A" -> Color(0xFF2ECC71)
                                        "B" -> Color(0xFF7DCEA0)
                                        "C" -> Color(0xFFF4D03F)
                                        "D" -> Color(0xFFE67E22)
                                        "E" -> Color(0xFFE74C3C)
                                        else -> Color.Gray
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                grades[index],
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "NutriScan – History Empty State")
@Composable
fun HistoryEmptyPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4CAF50))
                .padding(16.dp)
        ) {
            Text(
                "History",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No products scanned yet", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "No products scanned yet\nScan something to get started!",
                    fontSize = 18.sp,
                    color = Color(0xFF1B5E20),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}