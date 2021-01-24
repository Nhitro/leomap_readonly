package com.leocare.data.network.raw

data class Timeframe(
        val days: String,
        val includesToday: Boolean?,
        val open: List<Open>?
)