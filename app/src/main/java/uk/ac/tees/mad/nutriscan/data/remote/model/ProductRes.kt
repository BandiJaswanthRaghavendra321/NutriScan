package uk.ac.tees.mad.nutriscan.data.remote.model

data class ProductRes(
    val code: String?,
    val product: Product?,
    val status: Int?,
    val status_verbose: String?
)
