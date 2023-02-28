package com.st.BlueSTSDK

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun <T> Feature.waitData(request:Feature.()->Unit,extractData:(Feature.Sample)->T?):T =
    suspendCancellableCoroutine { continuation ->
    val listener = object : Feature.FeatureListener{
        override fun onUpdate(f: Feature, sample: Feature.Sample) {
            val data = extractData(sample)
            if(data!=null){
                f.removeFeatureListener(this)
                continuation.resume(data)
            }
        }
    }
    this.addFeatureListener(listener)
    this.request()
    continuation.invokeOnCancellation { this.removeFeatureListener(listener) }
}