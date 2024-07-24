package com.st.catalog

import com.st.blue_sdk.models.Boards

object StCatalogConfig {

    var showDemoList: Boolean = true

    var boardModelFilter: List<Boards.Model> = emptyList()

    var firmwareFamilyFilter: String? = null
}
