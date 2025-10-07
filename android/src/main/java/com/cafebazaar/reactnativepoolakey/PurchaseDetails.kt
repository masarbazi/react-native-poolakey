package com.cafebazaar.reactnativepoolakey


data class User(val name: String, val age: Int) {}
data class PurchaseDetails(val purchasedBy: String, val user: User) {}
