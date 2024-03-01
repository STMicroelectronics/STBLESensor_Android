package com.st.cloud_azure_iot_central

interface CloudAzureNavigation {
    val route: String
}

object CloudAzureNavigationApplicationSelection: CloudAzureNavigation {
    override val route: String
        get() = "cloudApplicationSelection"
}

object CloudAzureNavigationDeviceSelection: CloudAzureNavigation {
    override val route: String
        get() = "cloudDeviceSelection"
}

object CloudAzureNavigationDeviceConnection: CloudAzureNavigation {
    override val route: String
        get() = "cloudDeviceConnection"
}

object CloudAzureNavigationApplicationDetails: CloudAzureNavigation {
    const val appId: String = "appId"
    override val route: String
        get() = "cloudApplicationDetails/appId/"
}