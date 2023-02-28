package com.st.BlueSTSDK.gui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase

class TestCatalogDebugDB : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_catalog_debug_db)

        val tvTestValidity = findViewById<TextView>(R.id.test_catalog_tv_validity)
        val tvListFws = findViewById<TextView>(R.id.test_catalog_tv_list_fws)

        ReadBoardFirmwareDataBase(applicationContext).readDebugDB(tvTestValidity, tvListFws)

    }
}