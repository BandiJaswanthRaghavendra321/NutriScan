package uk.ac.tees.mad.nutriscan.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductLocal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductLocal)

    @Query("SELECT * FROM product")
    fun getAllProducts(): Flow<List<ProductLocal>>

    @Query("DELETE FROM product")
    suspend fun deleteProduct()

}