package com.ecommerce.mvp.common.seeder

import com.ecommerce.mvp.modules.category.entity.Category
import com.ecommerce.mvp.modules.category.repository.CategoryRepository
import com.ecommerce.mvp.modules.product.model.entity.Product
import com.ecommerce.mvp.modules.product.model.entity.Tag
import com.ecommerce.mvp.modules.product.repository.ProductRepository
import com.ecommerce.mvp.modules.product.repository.TagRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class CategoryTagProductDataSeederService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val tagRepository: TagRepository
) {

    private val log = LoggerFactory.getLogger(CategoryTagProductDataSeederService::class.java)

    @Transactional
    fun seedIfEmpty() {
        if (categoryRepository.count() > 0) {
            log.info("Seed data already present – skipping.")
            return
        }

        log.info("Seeding dummy categories and products …")

        // ── Tags ───────────────────────────────────────────────────────────────
        val tagNew        = tag("New Arrival")
        val tagBestseller = tag("Bestseller")
        val tagSale       = tag("Sale")
        val tagPremium    = tag("Premium")
        val tagEcoFriendly= tag("Eco-Friendly")

        // ── Root categories ────────────────────────────────────────────────────
        val electronics = category("Electronics",
            "Consumer electronics, gadgets and accessories")
        val clothing    = category("Clothing",
            "Men's and women's fashion apparel")
        val books       = category("Books",
            "Fiction, non-fiction, academic and more")
        val homeGarden  = category("Home & Garden",
            "Furniture, décor and outdoor products")
        val sports      = category("Sports & Outdoors",
            "Equipment and gear for every sport")

        // ── Sub-categories: Electronics ────────────────────────────────────────
        val mobiles      = category("Mobile Phones",    "Smartphones and feature phones",  electronics)
        val laptops      = category("Laptops",          "Notebooks and ultrabooks",         electronics)
        val accessories  = category("Accessories",      "Cables, cases, chargers and more", electronics)

        // ── Sub-categories: Clothing ───────────────────────────────────────────
        val mensWear     = category("Men's Wear",   "Shirts, trousers, jackets for men",   clothing)
        val womensWear   = category("Women's Wear", "Dresses, tops, bottoms for women",    clothing)

        // ── Sub-categories: Books ──────────────────────────────────────────────
        val fiction      = category("Fiction",     "Novels, short stories and poetry",     books)
        val nonFiction   = category("Non-Fiction", "Biographies, self-help and science",   books)

        // ── Sub-categories: Home & Garden ─────────────────────────────────────
        val furniture    = category("Furniture",   "Sofas, tables, chairs and beds",       homeGarden)
        val decor        = category("Décor",       "Wall art, lamps and decorative items", homeGarden)

        // ── Sub-categories: Sports ────────────────────────────────────────────
        val fitness      = category("Fitness",    "Gym equipment and accessories",  sports)
        val outdoor      = category("Outdoor",    "Camping, hiking and cycling gear", sports)

        // ── Products: Mobile Phones ────────────────────────────────────────────
        product("iPhone 15 Pro",           BigDecimal("999.99"),  50,  mobiles,
            "6.1\" Super Retina XDR display, A17 Pro chip, 48 MP camera system.",
            tagNew, tagPremium)
        product("Samsung Galaxy S24",      BigDecimal("849.99"),  35,  mobiles,
            "6.2\" Dynamic AMOLED 2X, Snapdragon 8 Gen 3, 50 MP triple camera.",
            tagNew, tagBestseller)
        product("Google Pixel 8",          BigDecimal("699.99"),  28,  mobiles,
            "6.2\" OLED display, Google Tensor G3 chip, advanced AI photography.",
            tagNew)
        product("OnePlus 12",              BigDecimal("599.99"),  40,  mobiles,
            "6.82\" LTPO AMOLED, Snapdragon 8 Gen 3, 50 W wireless charging.",
            tagSale)

        // ── Products: Laptops ──────────────────────────────────────────────────
        product("MacBook Pro 14\" M3",     BigDecimal("1999.99"), 20,  laptops,
            "Apple M3 Pro chip, 18 GB unified memory, 18-hour battery life.",
            tagPremium, tagBestseller)
        product("Dell XPS 15",             BigDecimal("1499.99"), 15,  laptops,
            "Intel Core i7-13700H, 16 GB DDR5 RAM, 512 GB NVMe SSD, OLED display.",
            tagBestseller)
        product("HP Spectre x360 14",      BigDecimal("1299.99"), 12,  laptops,
            "Intel Evo platform, 2-in-1 convertible, OLED touch display.",
            tagPremium)
        product("Lenovo ThinkPad X1 Carbon",BigDecimal("1399.99"),10, laptops,
            "Ultra-light business laptop, Intel Core Ultra 7, 32 GB LPDDR5.",
            tagNew)

        // ── Products: Accessories ──────────────────────────────────────────────
        product("AirPods Pro (2nd Gen)",   BigDecimal("249.99"), 100,  accessories,
            "Active Noise Cancellation, Adaptive Audio, MagSafe Charging Case.",
            tagPremium)
        product("Anker USB-C Hub 7-in-1",  BigDecimal("49.99"),  200,  accessories,
            "4K HDMI, 100 W PD, 3× USB-A 3.0, SD & MicroSD card reader.",
            tagBestseller, tagSale)
        product("Samsung 45 W Travel Adapter", BigDecimal("39.99"), 150, accessories,
            "Super Fast Charging 3.0, USB-C & USB-A dual ports, global voltage.",
            tagNew)

        // ── Products: Men's Wear ───────────────────────────────────────────────
        product("Levi's 501 Original Jeans", BigDecimal("59.99"),  75, mensWear,
            "Classic straight-fit, 100 % cotton denim, button fly.",
            tagBestseller)
        product("Nike Dri-FIT T-Shirt",      BigDecimal("34.99"), 120, mensWear,
            "Moisture-wicking fabric, slim fit, available in 8 colours.",
            tagBestseller, tagSale)
        product("Adidas Essential Hoodie",   BigDecimal("64.99"),  60, mensWear,
            "French terry cotton blend, kangaroo pocket, ribbed cuffs.",
            tagNew)
        product("Under Armour Joggers",      BigDecimal("54.99"),  80, mensWear,
            "UA Fleece fabric, tapered fit, side zip pockets.",
            tagSale)

        // ── Products: Women's Wear ─────────────────────────────────────────────
        product("Floral Maxi Dress",         BigDecimal("49.99"),  60, womensWear,
            "Lightweight chiffon, adjustable spaghetti straps, wrap-style.",
            tagNew, tagSale)
        product("High-Rise Yoga Pants",      BigDecimal("39.99"),  90, womensWear,
            "4-way stretch fabric, moisture-wicking, hidden waistband pocket.",
            tagBestseller)
        product("Classic Tailored Blazer",   BigDecimal("89.99"),  40, womensWear,
            "Slim fit, fully lined, notched lapel, available in 4 colours.",
            tagPremium)

        // ── Products: Fiction ──────────────────────────────────────────────────
        product("The Great Gatsby",          BigDecimal("12.99"), 200, fiction,
            "F. Scott Fitzgerald's timeless tale of wealth, love and the American Dream.",
            tagBestseller)
        product("1984",                      BigDecimal("10.99"), 180, fiction,
            "George Orwell's dystopian masterpiece about totalitarianism and surveillance.",
            tagBestseller)
        product("To Kill a Mockingbird",     BigDecimal("11.99"), 160, fiction,
            "Harper Lee's Pulitzer Prize-winning novel on racial injustice in the Deep South.",
            tagBestseller)

        // ── Products: Non-Fiction ──────────────────────────────────────────────
        product("Atomic Habits",             BigDecimal("16.99"), 180, nonFiction,
            "James Clear's guide to building good habits and breaking bad ones.",
            tagBestseller, tagNew)
        product("Sapiens: A Brief History",  BigDecimal("15.99"), 150, nonFiction,
            "Yuval Noah Harari's sweeping history of humankind.",
            tagBestseller)
        product("Deep Work",                 BigDecimal("14.99"), 130, nonFiction,
            "Cal Newport on focused success in a distracted world.",
            tagNew)

        // ── Products: Furniture ────────────────────────────────────────────────
        product("3-Seater Fabric Sofa",      BigDecimal("499.99"),  15, furniture,
            "Scandinavian design, stain-resistant fabric, solid wood legs.",
            tagNew)
        product("Ergonomic Office Chair",    BigDecimal("299.99"),  30, furniture,
            "Lumbar support, adjustable armrests, breathable mesh back.",
            tagBestseller, tagPremium)

        // ── Products: Décor ────────────────────────────────────────────────────
        product("Minimalist Ceramic Vase Set", BigDecimal("34.99"), 60, decor,
            "Set of 3 hand-painted ceramic vases, matte finish, various heights.",
            tagNew, tagEcoFriendly)
        product("LED Edison Bulb String Lights", BigDecimal("24.99"), 90, decor,
            "10 m, 50 warm-white LED bulbs, indoor/outdoor use, low energy.",
            tagSale)

        // ── Products: Fitness ──────────────────────────────────────────────────
        product("Adjustable Dumbbell Set",   BigDecimal("149.99"), 25, fitness,
            "5–52.5 lb per dumbbell, quick-change weight selector, space-saving.",
            tagBestseller)
        product("Resistance Bands Kit",      BigDecimal("29.99"),  80, fitness,
            "Set of 5 latex bands, 10–50 lb resistance levels, carry bag included.",
            tagSale, tagEcoFriendly)

        // ── Products: Outdoor ──────────────────────────────────────────────────
        product("4-Person Camping Tent",     BigDecimal("129.99"), 20, outdoor,
            "Waterproof 3000 mm HH rating, fibreglass poles, 3-season use.",
            tagNew)
        product("Trail Running Backpack 20L",BigDecimal("79.99"),  45, outdoor,
            "Ultralight, hydration bladder compatible, hip-belt pockets.",
            tagBestseller, tagEcoFriendly)

        log.info("Seeding complete.")
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun tag(name: String): Tag =
        tagRepository.findByName(name).orElseGet {
            tagRepository.save(Tag().apply { this.name = name })
        }

    private fun category(name: String, description: String, parent: Category? = null): Category {
        val c = Category().apply {
            this.name        = name
            this.description = description
            this.parent      = parent
        }
        return categoryRepository.save(c)
    }

    private fun product(
        name: String,
        price: BigDecimal,
        stock: Int,
        category: Category,
        description: String,
        vararg tags: Tag
    ): Product {
        val p = Product().apply {
            this.name        = name
            this.price       = price
            this.stock       = stock
            this.category    = category
            this.description = description
            this.tags        = tags.toMutableSet()
        }
        return productRepository.save(p)
    }
}

