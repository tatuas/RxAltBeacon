package com.tatuas.android.rxaltbeacon

import org.altbeacon.beacon.BeaconParser

object RxAltBeaconParser {

    val iBeacon: BeaconParser = BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
}
