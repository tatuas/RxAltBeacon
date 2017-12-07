package com.tatuas.android.rxaltbeacon

import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region

object BeaconParserConstants {
    val I_BEACON: BeaconParser = BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
}

object RegionConstants {
    val ALL = Region("", null, null, null)
}
