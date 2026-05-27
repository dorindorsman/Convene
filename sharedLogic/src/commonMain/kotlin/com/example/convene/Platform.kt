package com.example.convene

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform