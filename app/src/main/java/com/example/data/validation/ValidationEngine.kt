package com.example.data.validation

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val priceError: String? = null,
    val nameError: String? = null
)

object ValidationEngine {

    fun validatePhoneNumber(phone: String): ValidationResult {
        val clean = phone.replace(" ", "").replace("-", "")
        val isValid = (clean.startsWith("+234") && clean.length == 14) ||
                (clean.startsWith("0") && clean.length == 11)
        return if (isValid) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(isValid = false, errorMessage = "Please enter a valid Nigerian phone number (e.g. +234 812 345 6789 or 08012345678)")
        }
    }

    fun validateHostelLocation(hostel: String): ValidationResult {
        val clean = hostel.trim()
        return if (clean.length >= 3) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(isValid = false, errorMessage = "Hostel/Room location must be at least 3 characters")
        }
    }

    fun validateProfileForm(
        fullName: String,
        email: String,
        phone: String,
        matricNumber: String,
        campus: String
    ): ValidationResult {
        var isValid = true
        var emailErr: String? = null
        var phoneErr: String? = null
        var nameErr: String? = null

        if (fullName.trim().length < 3) {
            isValid = false
            nameErr = "Full name must be at least 3 characters"
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (!email.trim().matches(emailRegex)) {
            isValid = false
            emailErr = "Invalid university email format"
        }

        val phoneRes = validatePhoneNumber(phone)
        if (!phoneRes.isValid) {
            isValid = false
            phoneErr = phoneRes.errorMessage
        }

        return ValidationResult(
            isValid = isValid,
            nameError = nameErr,
            emailError = emailErr,
            phoneError = phoneErr
        )
    }

    fun validateProductForm(
        name: String,
        priceText: String,
        discountPriceText: String = "",
        quantityText: String = "1",
        category: String,
        condition: String,
        description: String,
        campus: String
    ): ValidationResult {
        if (name.trim().length < 3) {
            return ValidationResult(isValid = false, errorMessage = "Product title must be at least 3 characters")
        }
        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            return ValidationResult(isValid = false, errorMessage = "Price must be a positive number in Naira")
        }
        val discount = discountPriceText.toDoubleOrNull()
        if (discount != null && discount >= price) {
            return ValidationResult(isValid = false, errorMessage = "Discount price must be less than original price")
        }
        val qty = quantityText.toIntOrNull()
        if (qty == null || qty < 1) {
            return ValidationResult(isValid = false, errorMessage = "Quantity must be at least 1")
        }
        return ValidationResult(isValid = true)
    }
}
