package com.section11.expenselens.data.mapper

import com.section11.expenselens.data.dto.FirestoreHousehold
import org.junit.Test


class HouseholdMapperKtTest {

    @Test
    fun `test mapToHouseholdsList`() {
        val list = listOf(
            mapOf("id" to "1", "name" to "Household 1"),
            mapOf("id" to "2", "name" to "Household 2")
        )

        val households = list.mapToHouseholdsList()

        assert(households.size == 2)
        assert(households[0].id == "1")
        assert(households[0].name == "Household 1")
        assert(households[1].id == "2")
        assert(households[1].name == "Household 2")
    }

    @Test
    fun `test toHouseholdDetails`() {
        val firestoreHousehold = FirestoreHousehold("1", "Household 1", listOf("user1", "user2"))

        val householdDetails = firestoreHousehold.toHouseholdDetails()

        assert(householdDetails?.id == "1")
        assert(householdDetails?.name == "Household 1")
        assert(householdDetails?.usersIds == listOf("user1", "user2"))
    }
}
