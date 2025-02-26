### TODO list
- get gemini prompt from firebase instead of having it hardcoded, for flexibility
- Create household expense in firebase/integration:
    households (collection)
    |
    |-- householdId (document)
        |
        |-- name: "Smith Family"
        |-- users: ["userId1", "userId2"] // Array of user IDs
        |
        |-- expenses (sub-collection)
                |
                |-- expenseId (document)
                        |
                        |-- description: "Groceries"
                        |-- amount: 50.00
                        |-- date: timestamp
                        |-- userId: "userId1" // User who added the expense
                        |-- note: String? = user notes
                        - Distributed expense? (50% jon, 25% karen, 25% ken)
                        - Notes:
- Finish the expense preview screen and submit to firebase