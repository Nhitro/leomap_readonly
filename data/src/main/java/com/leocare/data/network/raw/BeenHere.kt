package com.leocare.data.network.raw

data class BeenHere(
        val count: Int?,
        val lastCheckinExpiredAt: Int?,
        val marked: Boolean?,
        val unconfirmedCount: Int?
)