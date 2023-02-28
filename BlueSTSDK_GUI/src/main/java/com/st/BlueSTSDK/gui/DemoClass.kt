package com.st.BlueSTSDK.gui

data class DemoClass  (
    //Demo Name
    var name: String,
    //Demo Icon
    val icon: Int,
    //Demo Number inside the ViewPager
    val number: Int,
    //Demo visible or not visible
    //For the moment non used because we use only the visible demos
    val isVisible: Boolean
)