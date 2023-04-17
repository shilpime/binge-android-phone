package com.tatasky.binge.utils

class Properties {
    private val moProprieties = com.moengage.core.Properties()
    var ctMap: HashMap<String, Any?> = hashMapOf()

    fun addAttribute(attributeName: String, attributeValue: Any?): com.moengage.core.Properties {
        moProprieties.addAttribute(attributeName, attributeValue);
        ctMap[attributeName] = attributeValue
        return moProprieties
    }

    fun getProperties(): com.moengage.core.Properties {
        return moProprieties
    }

    fun getCtProperties(): HashMap<String, Any?> {
        return ctMap
    }
}
