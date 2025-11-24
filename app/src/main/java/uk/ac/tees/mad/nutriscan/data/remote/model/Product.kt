package uk.ac.tees.mad.nutriscan.data.remote.model


data class Product(
    val product_name: String?,
    val brands: String?,
    val image_url: String?,
    val ingredients_text: String?,
    val nutriments: Nutriments?,
    val nutriscore_grade: String?,       // a–e rating
    val nutriscore_score: Int?,
    val categories: String?,
    val allergens_tags: List<String>?,
    val countries: String?,
    val quantity: String?,
    val ecoscore_grade: String?          // environmental rating
)

data class Nutriments(
    val energy_kcal_100g: Double?,
    val fat_100g: Double?,
    val saturated_fat_100g: Double?,
    val sugars_100g: Double?,
    val carbohydrates_100g: Double?,
    val fiber_100g: Double?,
    val proteins_100g: Double?,
    val salt_100g: Double?,
    val sodium_100g: Double?
)
