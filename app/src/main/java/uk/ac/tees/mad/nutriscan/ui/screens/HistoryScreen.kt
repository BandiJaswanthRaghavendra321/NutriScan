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
import androidx.compose.ui.text.style.TextOverflow
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
