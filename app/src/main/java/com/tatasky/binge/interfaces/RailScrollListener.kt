package com.tatasky.binge.interfaces

interface RailScrollListener {
    fun onRailScrolled(railName : String, position : Int, railType: String, railCategory: String)
}