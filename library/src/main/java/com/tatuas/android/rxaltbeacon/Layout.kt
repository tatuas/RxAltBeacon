package com.tatuas.android.rxaltbeacon

import org.altbeacon.beacon.BeaconParser

private enum class Layout(val value: String) {
    IBEACON("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
}

val IBEACON = BeaconParser().setBeaconLayout(Layout.IBEACON.value)
