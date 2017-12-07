package com.tatuas.android.rxaltbeacon

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.Region

fun RxAltBeacon.range(region: Region = RegionConstants.ALL,
                      strategy: BackpressureStrategy = BackpressureStrategy.LATEST): Flowable<Range> {
    val rangeSubject = PublishSubject.create<Range>()

    return bindBeaconManager()
            .doOnCancel {
                beaconManager.stopRangingBeaconsInRegion(region)
                beaconManager.removeAllRangeNotifiers()
            }
            .flatMap {
                beaconManager.addRangeNotifier { beacons, region ->
                    rangeSubject.onNext(Range(beacons.toList(), region))
                }
                beaconManager.startRangingBeaconsInRegion(region)

                rangeSubject.toFlowable(strategy)
            }
}

data class Range(val beacons: List<Beacon>, val region: Region)
