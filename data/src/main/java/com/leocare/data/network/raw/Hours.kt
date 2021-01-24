package com.leocare.data.network.raw

data class Hours(
        val isLocalHoliday: Boolean,
        val isOpen: Boolean,
        val status: String,
        val timeframes: List<Timeframe>
)