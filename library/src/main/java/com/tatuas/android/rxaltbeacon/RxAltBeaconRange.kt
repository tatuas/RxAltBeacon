package com.tatuas.android.rxaltbeacon

import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.Region

data class RxAltBeaconRange(val beacons: List<Beacon>, val region: Region)
