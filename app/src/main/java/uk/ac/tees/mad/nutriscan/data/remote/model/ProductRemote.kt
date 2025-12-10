package uk.ac.tees.mad.nutriscan.data.remote.model

data class ProductRemote(
    val product_name: String= "",
    val brands: String = "",
    val image_url: String = "",
    val ingredients_text: String = "",
    val nutriscore_grade: String = "",       // a–e rating
    val nutriscore_score: Int? = null,
    val categories: String? = "",
    val allergens_tags: List<String>? = null,
    val countries: String? = "",
    val quantity: String? = "",
    val ecoscore_grade: String? = ""          // environmental rating
)