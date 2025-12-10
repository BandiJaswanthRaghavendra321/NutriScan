package uk.ac.tees.mad.nutriscan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class ProductLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val product_name: String?,
    val brands: String?,
    val image_url: String?,
    val ingredients_text: String?,
    val nutriscore_grade: String?,
    val nutriscore_score: Int?,
    val categories: String?,
    val countries: String?,
    val quantity: String?,
    val ecoscore_grade: String?
)
