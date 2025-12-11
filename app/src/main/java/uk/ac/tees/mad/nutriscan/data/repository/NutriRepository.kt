package uk.ac.tees.mad.nutriscan.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

import uk.ac.tees.mad.nutriscan.data.local.ProductDao
import uk.ac.tees.mad.nutriscan.data.local.ProductLocal
import uk.ac.tees.mad.nutriscan.data.remote.api.ApiService
import uk.ac.tees.mad.nutriscan.data.remote.model.Product
import uk.ac.tees.mad.nutriscan.data.remote.model.ProductRemote

class NutriRepository @Inject constructor(
    private val apiService: ApiService,
    private val firestore: FirebaseFirestore,
    private val authentication: FirebaseAuth,
    private val productDao: ProductDao
) {

    suspend fun getProductAndSave(
        barcode: String,
        onSuccess: (Product) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val res = apiService.getProduct(barcode)

            if (res.product != null) {
                firestore.collection("users").document(authentication.uid!!)
                    .collection("products").document(barcode).set(res.product).await()

                fetchAndStore()

                onSuccess(res.product)

            } else {
                onFailure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun fetchAndStore(){
        try{
            val result = firestore.collection("users").document(authentication.uid!!)
                .collection("products").get().await()

            val products = result.documents.mapNotNull {
                it.toObject(ProductRemote::class.java)
            }

            productDao.deleteProduct()
            val mappedProduct = products.map { product ->
                product.toProductLocal()
            }
            productDao.insertAllProducts(mappedProduct)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun getProductFromRoom(): Flow<List<ProductLocal>> {
        return productDao.getAllProducts()
    }

    private fun ProductRemote.toProductLocal(): ProductLocal {
        return ProductLocal(
            product_name = product_name,
            brands = brands,
            image_url = image_url,
            ingredients_text = ingredients_text,
            nutriscore_grade = nutriscore_grade,
            nutriscore_score = nutriscore_score,
            categories = categories,
            countries = countries,
            quantity = quantity,
            ecoscore_grade = ecoscore_grade
        )
    }

}