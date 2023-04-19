package com.tatasky.binge.interfaces

interface SearchRailScrollListener {
    fun onSearchRailScrolled(railName : String, position : Int, railType: String, railCategory: String, provider: String)
}