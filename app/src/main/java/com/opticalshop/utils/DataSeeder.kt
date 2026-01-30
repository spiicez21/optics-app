package com.opticalshop.utils

import com.opticalshop.data.model.Product
import com.opticalshop.data.remote.FirestoreService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val firestoreService: FirestoreService
) {
    fun seedProducts() {
        val products = listOf(
            Product(
                id = "vc-cat-eye-01",
                name = "Vincent Chase Sleek Steel",
                brand = "Vincent Chase",
                description = "Black Gold Full Rim Cat Eye eyeglasses from the Sleek Steel collection. Perfect for a sharp, modern look.",
                price = 2500.0,
                originalPrice = 5000.0,
                category = "cat-women",
                frameShape = "Cat Eye",
                frameMaterial = "Stainless Steel",
                frameColor = "Black Gold",
                lensType = "Blue Block",
                gender = "Women",
                images = listOf("https://i.ibb.co/RTC6zzjR/41ea0a6c1b6e.jpg"),
                stock = 30,
                rating = 4.7,
                reviewCount = 56,
                featured = true
            ),
            Product(
                id = "la-aviator-01",
                name = "Lenskart Air Essentials",
                brand = "Lenskart Air",
                description = "Black Grey Full Rim Aviator eyeglasses. Ultra-lightweight TR90 material for all-day comfort.",
                price = 1500.0,
                originalPrice = 3000.0,
                category = "cat-men",
                frameShape = "Aviator",
                frameMaterial = "TR90",
                frameColor = "Black Grey",
                lensType = "Standard",
                gender = "Men",
                images = listOf("https://i.ibb.co/60T5vHpt/57890b044c53.jpg"),
                stock = 45,
                rating = 4.5,
                reviewCount = 120,
                featured = true
            ),
            Product(
                id = "jj-titanium-01",
                name = "John Jacobs Pro Titanium",
                brand = "John Jacobs",
                description = "Black Rimless Square eyeglasses crafted from premium Pro Titanium. Minimalist and light.",
                price = 4500.0,
                originalPrice = 6000.0,
                category = "cat-luxury",
                frameShape = "Square",
                frameMaterial = "Titanium",
                frameColor = "Black",
                lensType = "Anti-Glare",
                gender = "Men",
                images = listOf("https://i.ibb.co/r2kD17tB/e7018527b146.jpg"),
                stock = 20,
                rating = 4.9,
                reviewCount = 34
            ),
            Product(
                id = "vc-geometric-01",
                name = "Vincent Chase Blend Edit",
                brand = "Vincent Chase",
                description = "Black Silver Full Rim Geometric eyeglasses. A unique blend of style and structural design.",
                price = 2200.0,
                originalPrice = 4000.0,
                category = "cat-women",
                frameShape = "Geometric",
                frameMaterial = "Metal",
                frameColor = "Black Silver",
                lensType = "Screen Guard",
                gender = "Women",
                images = listOf("https://i.ibb.co/RkrjH66G/e05ae585b67b.jpg"),
                stock = 15,
                rating = 4.6,
                reviewCount = 28
            ),
            Product(
                id = "la-geometric-01",
                name = "Lenskart Air Gunmetal",
                brand = "Lenskart Air",
                description = "Gunmetal Black Rimless Geometric eyeglasses. Futuristic design with weightless feel.",
                price = 1800.0,
                originalPrice = 3500.0,
                category = "cat-men",
                frameShape = "Geometric",
                frameMaterial = "Stainless Steel",
                frameColor = "Gunmetal",
                lensType = "Blue Cut",
                gender = "Men",
                images = listOf("https://i.ibb.co/Y4jgjsxy/1aeaad26acbe.jpg"),
                stock = 25,
                rating = 4.8,
                reviewCount = 42
            ),
            Product(
                id = "vc-sleek-02",
                name = "Vincent Chase Sleek Steel Blue",
                brand = "Vincent Chase",
                description = "Gunmetal Blue Half Rim Geometric eyeglasses. Professional and stylish.",
                price = 2600.0,
                originalPrice = 4500.0,
                category = "cat-men",
                frameShape = "Geometric",
                frameMaterial = "Steel",
                frameColor = "Gunmetal Blue",
                lensType = "Standard",
                gender = "Men",
                images = listOf("https://i.ibb.co/CKg7wFYj/0be7d9b5277a.jpg"),
                stock = 12,
                rating = 4.7,
                reviewCount = 15
            ),
            Product(
                id = "vc-sleek-square-01",
                name = "Vincent Chase Sleek Square",
                brand = "Vincent Chase",
                description = "Gunmetal Blue Half Rim Square eyeglasses. Classic square shape with a touch of blue.",
                price = 2400.0,
                originalPrice = 4200.0,
                category = "cat-men",
                frameShape = "Square",
                frameMaterial = "Steel",
                frameColor = "Gunmetal Blue",
                lensType = "Anti-Reflective",
                images = listOf("https://i.ibb.co/1t1JXm7d/1912665a409a.jpg"),
                stock = 18,
                rating = 4.6,
                reviewCount = 25
            ),
            Product(
                id = "jj-full-rim-01",
                name = "John Jacobs Full Rim Classic",
                brand = "John Jacobs",
                description = "Timeless black full rim eyeglasses. Durable and comfortable for everyday use.",
                price = 3200.0,
                originalPrice = 4800.0,
                category = "cat-men",
                frameShape = "Rectangle",
                frameMaterial = "Acetate",
                images = listOf("https://i.ibb.co/9FQNySH/3398373111a4.jpg"),
                stock = 22,
                rating = 4.8,
                reviewCount = 42
            ),
            Product(
                id = "jj-classic-black-01",
                name = "John Jacobs Urban Black",
                brand = "John Jacobs",
                description = "Sleek and professional black frames for the urban professional.",
                price = 3000.0,
                originalPrice = 4500.0,
                category = "cat-men",
                frameShape = "Square",
                frameMaterial = "Steel",
                images = listOf("https://i.ibb.co/v4nySR7Q/9f5495045e99.jpg"),
                stock = 15,
                rating = 4.7,
                reviewCount = 18
            ),
            Product(
                id = "jj-tortoise-01",
                name = "John Jacobs Tortoise Bloom",
                brand = "John Jacobs",
                description = "Elegant tortoise shell patterned frames with a modern rectangular silhouette.",
                price = 3500.0,
                originalPrice = 5200.0,
                category = "cat-women",
                frameShape = "Rectangle",
                images = listOf("https://i.ibb.co/B5576yM2/6f019c98a50b.jpg"),
                stock = 10,
                rating = 4.9,
                reviewCount = 31
            ),
            Product(
                id = "jj-sleek-metal-01",
                name = "John Jacobs Sleek Metal",
                brand = "John Jacobs",
                description = "Minimalist silver metal frames. Ultra-thin temples and lightweight design.",
                price = 3800.0,
                originalPrice = 5500.0,
                category = "cat-luxury",
                frameShape = "Rectangle",
                images = listOf("https://i.ibb.co/Fkdkpmvx/f7132dfbf99d.jpg"),
                stock = 8,
                rating = 4.8,
                reviewCount = 12
            ),
            Product(
                id = "jj-round-01",
                name = "John Jacobs Round Horizon",
                brand = "John Jacobs",
                description = "Classic round frames inspired by vintage aesthetics, updated with modern materials.",
                price = 3300.0,
                originalPrice = 4900.0,
                category = "cat-women",
                frameShape = "Round",
                images = listOf("https://i.ibb.co/KpFZ7zqZ/a1cee33d9a49.jpg"),
                stock = 14,
                rating = 4.7,
                reviewCount = 20
            ),
            Product(
                id = "jj-rect-01",
                name = "John Jacobs Studio Rect",
                brand = "John Jacobs",
                description = "Bold rectangular frames that make a statement. High-quality acetate construction.",
                price = 3100.0,
                originalPrice = 4600.0,
                category = "cat-men",
                frameShape = "Rectangle",
                images = listOf("https://i.ibb.co/KZ5zskS/b972d5c3698b.jpg"),
                stock = 20,
                rating = 4.6,
                reviewCount = 15
            ),
            Product(
                id = "jj-indigo-01",
                name = "John Jacobs Indigo Night",
                brand = "John Jacobs",
                description = "Deep indigo blue full rim frames. Stylish and unique.",
                price = 3400.0,
                originalPrice = 5100.0,
                category = "cat-men",
                images = listOf("https://i.ibb.co/W467ktxn/3c57365a0868.jpg"),
                stock = 12,
                rating = 4.8,
                reviewCount = 27
            ),
            Product(
                id = "jj-rimless-pro-01",
                name = "John Jacobs Rimless Pro",
                brand = "John Jacobs",
                description = "Advanced rimless design with pro-titanium temples. Barely-there feel.",
                price = 4800.0,
                originalPrice = 6500.0,
                category = "cat-luxury",
                images = listOf("https://i.ibb.co/xqQ3zXFY/157f53f0b090.jpg"),
                stock = 5,
                rating = 5.0,
                reviewCount = 9
            ),
            Product(
                id = "jj-brown-01",
                name = "John Jacobs Earthy Brown",
                brand = "John Jacobs",
                description = "Warm brown tones in a comfortable full rim design.",
                price = 2900.0,
                originalPrice = 4300.0,
                category = "cat-men",
                images = listOf("https://i.ibb.co/sJC3MqQB/de23021be738.jpg"),
                stock = 16,
                rating = 4.5,
                reviewCount = 22
            ),
            Product(
                id = "jj-silver-01",
                name = "John Jacobs Silver Lining",
                brand = "John Jacobs",
                description = "Bright silver metal frames that catch the light.",
                price = 3600.0,
                originalPrice = 5300.0,
                category = "cat-women",
                images = listOf("https://i.ibb.co/21dXqSzm/f62077aa8ea7.jpg"),
                stock = 11,
                rating = 4.7,
                reviewCount = 16
            ),
            Product(
                id = "jj-rimless-metal-01",
                name = "John Jacobs Metal Rimless",
                brand = "John Jacobs",
                description = "Simplified rimless look with sturdy metal bridges.",
                price = 4200.0,
                originalPrice = 5800.0,
                category = "cat-luxury",
                images = listOf("https://i.ibb.co/gb7rhg2V/80d9f07f3f82.jpg"),
                stock = 7,
                rating = 4.8,
                reviewCount = 14
            ),
            Product(
                id = "la-round-01",
                name = "Lenskart Air Breeze",
                brand = "Lenskart Air",
                description = "Feather-light round frames for a breezy, youthful look.",
                price = 1700.0,
                originalPrice = 3200.0,
                category = "cat-computer",
                images = listOf("https://i.ibb.co/5XHCpzGD/b178598f108a.jpg"),
                stock = 30,
                rating = 4.6,
                reviewCount = 48
            ),
            Product(
                id = "jj-rimless-titanium-01",
                name = "John Jacobs Titanium Elite",
                brand = "John Jacobs",
                description = "The pinnacle of titanium rimless eyewear. Silver black aviator silhouette.",
                price = 5200.0,
                originalPrice = 7800.0,
                category = "cat-luxury",
                images = listOf("https://i.ibb.co/wr8G2tWV/2e8049d0b26f.jpg"),
                stock = 6,
                rating = 4.9,
                reviewCount = 19
            ),
            Product(
                id = "vc-blend-silver-01",
                name = "Vincent Chase Silver Geometric",
                brand = "Vincent Chase",
                description = "Silver transparent geometric frames. Modernity meet transparency.",
                price = 2300.0,
                originalPrice = 4100.0,
                category = "cat-women",
                images = listOf("https://i.ibb.co/0jz5rDqH/9bf46b42eeab.jpg"),
                stock = 20,
                rating = 4.7,
                reviewCount = 33
            ),
            Product(
                id = "la-signia-01",
                name = "Lenskart Air Signia",
                brand = "Lenskart Air",
                description = "Transparent Gold Full Rim Rectangle eyeglasses. Elegant transparency meet premium gold accents.",
                price = 2800.0,
                originalPrice = 5000.0,
                category = "cat-women",
                frameShape = "Rectangle",
                frameMaterial = "Acetate",
                frameColor = "Transparent Gold",
                lensType = "Blue Block",
                gender = "Women",
                images = listOf("https://i.ibb.co/KxWpgcpJ/405844994bfb.jpg"),
                stock = 18,
                rating = 4.8,
                reviewCount = 67,
                featured = true
            ),
            Product(
                id = "vc-round-tort-01",
                name = "Vincent Chase Tortoise Round",
                brand = "Vincent Chase",
                description = "Fun and playful tortoise shell round frames.",
                price = 2100.0,
                originalPrice = 3900.0,
                category = "cat-women",
                images = listOf("https://i.ibb.co/5XJ9rc8D/8185665b5aca.jpg"),
                stock = 25,
                rating = 4.6,
                reviewCount = 39
            ),
            Product(
                id = "vc-sleek-grey-01",
                name = "Vincent Chase Sleek Grey",
                brand = "Vincent Chase",
                description = "Steel grey frames with a professional rectangular cut.",
                price = 2500.0,
                originalPrice = 4400.0,
                category = "cat-men",
                images = listOf("https://i.ibb.co/rTWRnCk/e620f5465a6a.jpg"),
                stock = 14,
                rating = 4.7,
                reviewCount = 23
            ),
            Product(
                id = "vc-classic-matte-01",
                name = "Vincent Chase Matte Pro",
                brand = "Vincent Chase",
                description = "Classic matte black frames. Durable, stylish, and versatile.",
                price = 2200.0,
                originalPrice = 3800.0,
                category = "cat-men",
                images = listOf("https://i.ibb.co/qFCbbVG9/449beb5f589d.jpg"),
                stock = 26,
                rating = 4.8,
                reviewCount = 52
            )
        )

        val categories = listOf(
            com.opticalshop.data.model.Category(id = "cat-men", name = "Men", order = 1),
            com.opticalshop.data.model.Category(id = "cat-women", name = "Women", order = 2),
            com.opticalshop.data.model.Category(id = "cat-luxury", name = "Luxury", order = 3),
            com.opticalshop.data.model.Category(id = "cat-computer", name = "Computer", order = 4)
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Clear existing data first as requested
                firestoreService.clearCategories()
                firestoreService.clearProducts()
                
                categories.forEach { category ->
                    firestoreService.addCategory(category)
                }
                products.forEach { product ->
                    firestoreService.addProduct(product)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
