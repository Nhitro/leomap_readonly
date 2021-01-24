package com.leocare.data.network.raw

data class Category(
        val icon: Icon,
        val id: String,
        val name: String?,
        val pluralName: String?,
        val primary: Boolean?,
        val shortName: String?
)