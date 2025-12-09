package uk.ac.tees.mad.nutriscan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import uk.ac.tees.mad.nutriscan.data.remote.model.ApiResponse
import uk.ac.tees.mad.nutriscan.data.remote.model.Product
import uk.ac.tees.mad.nutriscan.data.remote.model.RecipeSearchResponse
import uk.ac.tees.mad.nutriscan.ui.theme.*
import uk.ac.tees.mad.nutriscan.ui.viewmodels.MainVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    homeVm: MainVM,
    barcode: String,
    onSaveFavorite: () -> Unit = {},
    onAddNote: (String) -> Unit = {}
) {
    LaunchedEffect(barcode) {
        homeVm.fetchApiData(barcode)
        homeVm.fetchRecipe()
    }

    val recipee by homeVm.recipe.collectAsState()
    val productState = homeVm.product.collectAsState().value
    var noteText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Product Details", color = White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = GreenPrimary
                )
            )
        },
        containerColor = GrayLight
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(GrayLight)
        ) {
            when (productState) {
                is ApiResponse.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }

                is ApiResponse.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Error: ${(productState as ApiResponse.Error).message}",
                            color = RedError
                        )
                    }
                }

                is ApiResponse.Success -> {
                    val product = (productState as ApiResponse.Success<Product>).data
                    ProductDetailsContent(product, recipee, noteText, onNoteChange = { noteText = it }, onSaveFavorite, onAddNote)
                }

                else -> {}
            }
        }
    }
}

@Composable
fun ProductDetailsContent(
    product: Product,
    recipe: ApiResponse<RecipeSearchResponse>,
    noteText: String,
    onNoteChange: (String) -> Unit,
    onSaveFavorite: () -> Unit,
    onAddNote: (String) -> Unit
) {
    val nutriments = product.nutriments

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        product.image_url?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = product.product_name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.height(16.dp))
        }

        Text(
            text = product.product_name ?: "Unknown Product",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GreenDark
        )

        Text(
            text = product.brands ?: "Unknown Brand",
            color = GrayMedium,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            product.nutriscore_grade?.let {
                BadgeBox("NutriScore", it.uppercase(), GreenPrimary)
            }
            product.ecoscore_grade?.let {
                BadgeBox("EcoScore", it.uppercase(), BlueInfo)
            }
        }

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Nutritional Info (per 100g)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreenPrimary)
                Spacer(Modifier.height(10.dp))

                nutriments?.let {
                    NutrientRow("Calories", it.energy_kcal_100g ?: 0.0, "kcal", 2500.0)
                    NutrientRow("Fat", it.fat_100g ?: 0.0, "g", 70.0)
                    NutrientRow("Saturated Fat", it.saturated_fat_100g ?: 0.0, "g", 20.0)
                    NutrientRow("Sugar", it.sugars_100g ?: 0.0, "g", 50.0)
                    NutrientRow("Protein", it.proteins_100g ?: 0.0, "g", 55.0)
                    NutrientRow("Salt", it.salt_100g ?: 0.0, "g", 6.0)
                } ?: Text("Nutritional data not available", color = GrayMedium)
            }
        }

        Spacer(Modifier.height(20.dp))

        val warnings = generateWarnings(nutriments)
        if (warnings.isNotEmpty()) {
            WarningCard("Warnings", warnings)
            Spacer(Modifier.height(20.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GreenLight.copy(alpha = 0.4f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Healthier Alternatives", fontWeight = FontWeight.Bold, color = GreenPrimary)
                Spacer(Modifier.height(10.dp))
                when(recipe){
                    is ApiResponse.Initial -> {
                        AlternativeItem("Low-fat Greek Yogurt")
                        AlternativeItem("Organic Oat Cereal")
                        AlternativeItem("Unsweetened Almond Milk")
                    }
                    is ApiResponse.Loading -> {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                    is ApiResponse.Success -> {
                        val size = recipe.data.hits.size
                        val randomise = (0..size).random()
                        val randomise2 = (0..size).random()
                        val recipe2 = recipe.data.hits[randomise2]
                        val recipe = recipe.data.hits[randomise]
                        recipe2.recipe.label.let {
                            AlternativeItem(it)
                            Spacer(Modifier.height(10.dp))
                        }
                        recipe.recipe.label.let {
                            AlternativeItem(it)
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    is ApiResponse.Error -> {
                        Text("Error: ${(recipe as ApiResponse.Error).message}", color = RedError)
                    }
                }

            }
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Notes", fontWeight = FontWeight.Bold, color = GreenPrimary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = onNoteChange,
                    placeholder = { Text("Write something...") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { onAddNote(noteText) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(GreenPrimary)
                ) {
                    Text("Save Note", color = White)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onSaveFavorite,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(GreenPrimary)
        ) {
            Text("Save to Favorites", color = White, fontSize = 16.sp)
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun NutrientRow(name: String, value: Double, unit: String, recommended: Double) {
    val color = when {
        value > recommended * 0.8 -> RedError
        value > recommended * 0.5 -> OrangeAccent
        else -> GreenPrimary
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, color = GrayDark, fontSize = 15.sp)
        Text(String.format("%.1f %s", value, unit), color = color, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
fun BadgeBox(title: String, grade: String, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
            Text(grade, fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
        }
    }
}

@Composable
fun WarningCard(title: String, warnings: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = RedError)
            Spacer(Modifier.height(8.dp))
            warnings.forEach { warn ->
                Text("• $warn", color = RedError, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AlternativeItem(name: String) {
    Text("• $name", color = GreenDark, fontSize = 14.sp)
}

fun generateWarnings(n: uk.ac.tees.mad.nutriscan.data.remote.model.Nutriments?): List<String> {
    val warnings = mutableListOf<String>()
    if (n == null) return warnings

    if ((n.sugars_100g ?: 0.0) > 15) warnings.add("High sugar content – may not be suitable for diabetics.")
    if ((n.fat_100g ?: 0.0) > 20) warnings.add("High fat content – consume in moderation.")
    if ((n.salt_100g ?: 0.0) > 1.5) warnings.add("High salt content – limit intake.")
    if ((n.saturated_fat_100g ?: 0.0) > 5) warnings.add("High saturated fat content detected.")
    return warnings
}
