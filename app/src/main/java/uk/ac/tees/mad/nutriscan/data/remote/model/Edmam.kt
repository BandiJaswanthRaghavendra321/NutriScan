package uk.ac.tees.mad.nutriscan.data.remote.model

data class RecipeSearchResponse(
    val hits: List<Hit>
)

data class Hit(
    val recipe: RecipeDto
)

data class RecipeDto(
    val uri: String,
    val label: String,
    val image: String?,
    val ingredientLines: List<String>,
    val calories: Double,
    val totalNutrients: Map<String, NutrientDto>?
)

data class NutrientDto(
    val label: String,
    val quantity: Double,
    val unit: String
)
