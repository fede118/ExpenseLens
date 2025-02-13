package com.section11.expenselens.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SuggestedExpenseInformation(
    val total: String?,
    val estimatedCategory: Category?
) : Parcelable {
    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SuggestedExpenseInformation

        if (total != other.total) return false
        if (estimatedCategory != other.estimatedCategory) return false

        return true
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CategoryDescription(val description: String)

/**
 * Enum class representing different categories of expenses.
 *
 * IMPORTANT: ADD [@CategoryDescription] since that description is what is used to estimate the
 * category of the expense.
 */
enum class Category(val displayName: String) {

    @CategoryDescription("""
        HOME includes: 
        -Rent or mortgage payments, property taxes, homeowner's/renter's insurance.
        -Utilities: Electricity, gas, water, sewer, trash/recycling, internet, phone (landline or mobile).
        - Home Maintenance & Repairs: Costs for fixing appliances, plumbing issues, landscaping, etc.
        - Cleaning Supplies & Household Goods: Detergent, cleaning tools, light bulbs, batteries, etc.
    """)
    HOME("Home"),

    @CategoryDescription("""
        TRANSPORTATION includes Car payments, gas, public transportation fares, car insurance, vehicle maintenance, parking fees.
    """)
    TRANSPORTATION("Transportation"),

    @CategoryDescription("""
        GROCERIES includes: Includes super market purchases, veggies, bakeries, bread, dairy, frozen, etc.
    """)
    GROCERIES("Groceries"),

    @CategoryDescription("""
       HEALTH_CARE includes: Doctor visits, prescriptions, health insurance premiums, dental care, vision care. 
    """)
    HEALTH_CARE("Health Care"),

    @CategoryDescription("""
        FINANCIAL includes:  Debt Payments: Credit cards, student loans, personal loans, other loan repayments.
        - Savings & Investments: Contributions to retirement accounts, emergency funds, or other investment vehicles.
    """)
    FINANCIAL("Financial"),

    @CategoryDescription("""
        ENTERTAINMENT includes: Movies, concerts, streaming subscriptions, dining out, hobbies.
        - Travel & Vacations: Costs associated with trips, including transportation, accommodation, and activities.
        - Dine out, Take out, restaurant meals, drinks at a bar, etc
    """)
    ENTERTAINMENT("Entertainment"),

    @CategoryDescription("""
        PERSONAL includes: Purchases of clothes, shoes, Haircuts, cosmetics, other personal grooming expenses.
        - Gym memberships
    """)
    PERSONAL("Personal"),

    @CategoryDescription("""
        EDUCATION includes: Tuition, books, school supplies, tutoring, etc.
    """)
    EDUCATION("Education"),

    @CategoryDescription("""
        PET_CARE includes: Food, vet visits, grooming, pet toys.
    """)
    PET_CARE("Pet Care"),

    @CategoryDescription("""
        MISCELLANEOUS includes: Unexpected expenses, small purchases, gifts, etc.
    """)
    MISCELLANEOUS("Miscellaneous");

    companion object {
        fun getCategoryDescriptions(): String {
            return Category.entries.joinToString("\n\n") { category ->
                val description = category::class.java
                    .getField(category.name)
                    .getAnnotation(CategoryDescription::class.java)
                    ?.description ?: "No description available"

                "**[${category.name}] **: ${description.trimIndent()}"
            }
        }

        fun fromDisplayName(displayName: String): Category? {
            return entries.find { it.displayName == displayName }
        }
    }
}
