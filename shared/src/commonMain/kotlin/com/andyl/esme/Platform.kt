package com.andyl.esme

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform