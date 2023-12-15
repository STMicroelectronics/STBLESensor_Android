package com.st.flow_demo

interface FlowDemoNavigation {
    val route: String
}

object DestinationFlowDemoFlowCategoriesExampleScreen: FlowDemoNavigation {
    override val route: String
        get() = "categoriesExample"
}

object DestinationFlowDemoSensorsScree: FlowDemoNavigation {
    override val route: String
        get() = "boardSensors"
}

object DestinationFlowDemoPnPLControlScreen: FlowDemoNavigation {
    override val route: String
        get() = "pnpLControl"
}

object DestinationFlowDemoFlowsExpertScreen: FlowDemoNavigation {
    override val route: String
        get() = "flowsExpert"
}

object DestinationFlowDemoMoreInfoScreen: FlowDemoNavigation {
    override val route: String
        get() = "moreInfo"
}

object DestinationFlowDemoFlowUploadScreen: FlowDemoNavigation {
    override val route: String
        get() = "flowUpload"
}

object DestinationFlowDemoFlowExpertEditingScreen: FlowDemoNavigation {
    override val route: String
        get() = "flowEditing"
}

object DestinationFlowDemoFlowDetailScreen: FlowDemoNavigation {
    override val route: String
        get() = "flowDetails"
}

object DestinationFlowDemoFlowIfApplicationCreationScreen: FlowDemoNavigation {
    override val route: String
        get() = "flowIfEditing"
}

object DestinationFlowDemoFlowSaveScreen: FlowDemoNavigation {
    override val route: String
        get() = "flowSaving"
}

object DestinationFlowDemoSensorConfigurationScreen: FlowDemoNavigation {
    override val route: String
        get() = "sensorConfiguration"
}

object DestinationFlowDemoFunctionConfigurationScreen: FlowDemoNavigation {
    override val route: String
        get() = "functionConfiguration"
}

object DestinationFlowDemoOutputConfigurationScreen: FlowDemoNavigation {
    override val route: String
        get() = "outputConfiguration"
}

object DestinationFlowDemoSensorDetailScreen: FlowDemoNavigation {
    const val sensorId: String = "sensorId"
    override val route: String
        get() = "detail/sensor/"
}

object DestinationFlowDemoFlowCategoryExampleScreen: FlowDemoNavigation {
    const val categoryType: String = "categoryType"
    override val route: String
        get() = "detail/category/"
}













