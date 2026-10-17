# Student Market Hub 🇳🇬

**Student Market Hub** is a Nigerian campus marketplace Android application built with **Kotlin** and **Jetpack Compose (Material Design 3)**. It empowers university students across Nigerian campuses (UNILAG, LASU, UI, OAU, UNIBEN) to buy and sell textbooks, gadgets, hostel essentials, and campus services with doorstep hostel delivery and server-verified **Paystack** checkout.

---

## Key Features

### 1. Student Marketplace & Product Listing Screen
- **Room `Product` Entity (`com.example.data.model.Product`)**: Persists campus product listings locally in SQLite via Room (`@Entity(tableName = "products")`) with fields for `id`, `title`, `name`, `price`, `discountPrice`, `description`, `imageUrl`, `category`, `condition`, `campus`, `quantity`, and `iconEmoji`.
- **List New Item for Sale Screen (`ListNewProductScreen.kt`)**: Dedicated screen accessible via the **"Sell Item"** Floating Action Button on the Marketplace home screen or **"New Listing"** in the Seller Command Hub. Includes:
  - **Product Title** (`product_title_input`)
  - **Price in Naira (₦)** (`product_price_input`) & optional discount price
  - **Description & Condition Details** (`product_description_input`)
  - **Image URL** (`product_image_url_input`) with live image preview powered by **Coil (`AsyncImage`)**
  - Category, condition (`Brand New`, `Like New`, `Fairly Used`), and campus selectors
- **Search & Filtering**: Instant search bar filtering by product title, description, category, or store name, alongside campus and category chips.

### 2. Cloud Synchronization & Authentication (Firebase Firestore & Google Sign-In)
- **Google Sign-In (`AuthManager.kt`)**: Uses **Jetpack Credential Manager** (`GetSignInWithGoogleOption` for interactive sign-in and `GetGoogleIdOption` for silent auto sign-in) with `R.string.default_web_client_id`.
- **Cloud Firestore (`FirestoreProductRepository.kt`)**: Connects to the provisioned enterprise database ID (`R.string.firestore_database_id`) and enforces zero-trust security rules (`firestore.rules`) with per-user ownership isolation (`request.auth.uid == userId`), strict schema validation (`isValidProductListing`), and structured JSON error logging (`handleFirestoreError`).

### 3. Production-Safe Paystack Checkout & Order State Machine
- **Pending Order Creation**: Creates an order in `PENDING` state prior to initiating payment.
- **Server-Side Payment Verification (`PaystackPaymentVerifier.kt` & `supabase/functions/verify-paystack-payment/index.ts`)**: Never marks an order as `PAID` from client-only callbacks; verifies the Paystack transaction reference, amount (in kobo), and currency (`NGN`) before transitioning the order to `PAID`.
- **Role-Based Order Lifecycle (`OrderStatusStateMachine.kt` & `OrderSecurityPolicy.kt`)**: Enforces strict state transitions (`PENDING` → `PAID` → `PROCESSING` → `DISPATCHED` → `DELIVERED` / `CANCELLED`) across Customer, Seller, and Admin roles.

---

## Project Architecture

```
app/src/main/java/com/example/
├── MainActivity.kt                        # Root Compose navigation, FAB to ListNewProductScreen, sheets & dialogs
├── data/
│   ├── auth/AuthManager.kt                # Google Sign-In via Jetpack Credential Manager
│   ├── firestore/
│   │   ├── FirestoreErrorHandler.kt       # Structured JSON error logging (handleFirestoreError)
│   │   └── FirestoreProductRepository.kt  # Firestore CRUD & real-time snapshots Flow
│   ├── local/
│   │   ├── AppDatabase.kt                 # Room Database configuration
│   │   └── MarketplaceDao.kt              # Room DAO returning reactive Kotlin Flows
│   ├── model/
│   │   ├── Product.kt                     # Room @Entity(tableName = "products") with title, price, description, imageUrl
│   │   └── MarketplaceEntities.kt         # UserProfile, Store, ServiceItem, Order, OrderItem, Review, Message
│   ├── order/
│   │   ├── OrderStatus.kt                 # Order lifecycle enum
│   │   ├── OrderStatusStateMachine.kt     # Valid transition rules per role
│   │   └── OrderSecurityPolicy.kt         # Row-level security validation
│   ├── payment/PaystackPaymentVerifier.kt # Server-side Paystack verification client
│   ├── repository/MarketplaceRepository.kt# Single source of truth coordinating Room & payment verification
│   └── validation/ValidationEngine.kt     # Input validation for listings, profiles, and hostel delivery
└── ui/
    ├── components/                        # ProductCard (Coil AsyncImage), ServiceCard, PaystackPaymentSheet, StudentAuthDialog
    ├── screens/
    │   ├── customer/                      # MarketplaceHomeScreen, CartCheckoutScreen, CustomerOrdersScreen, ProductDetailSheet
    │   ├── seller/                        # ListNewProductScreen, AddEditProductDialog, SellerDashboardScreen
    │   └── admin/                         # AdminDashboardScreen
    ├── theme/                             # Material 3 color palette (BrandNavy, BrandOrange) & typography
    └── viewmodel/MarketplaceViewModel.kt  # StateFlow state holder for UI screens
```

---

## Configuration & Running Tests

### Environment Secrets (`.env` / `.env.example`)
Configure keys in the **Secrets panel in AI Studio** (never in `local.properties`):
- `PAYSTACK_PUBLIC_KEY`: Paystack public key (`pk_test_...` or `pk_live_...`)
- `PAYMENT_VERIFY_ENDPOINT`: Server-side verification URL (e.g., Supabase Edge Function endpoint)

### Running Unit & Security Rule Tests
1. **JavaScript Firestore Rules Tests**:
   ```bash
   FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
   ```
2. **Android JVM & Robolectric Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   ```
