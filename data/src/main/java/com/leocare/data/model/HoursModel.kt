package com.leocare.data.model

data class HoursModel(
        val isOpen: Boolean,
        val lastStatus: String,
        val renderedTimes: List<String>
)