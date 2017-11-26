package com.tatuas.android.rxaltbeacon

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Region

class RxAltBeacon private constructor(private val appContext: Context) {

    companion object {
        fun with(context: Context) = RxAltBeacon(context.applicationContext)
    }

    private val beaconManager: BeaconManager = BeaconManager.getInstanceForApplication(appContext).apply {
        beaconParsers.add(Option.LAYOUT)
        foregroundBetweenScanPeriod = 1024L * Option.INTERVAL_SECONDS
    }

    private val beaconBehavior = BehaviorSubject.create<Beacon>()

    private var emptyRangeRegion = Region("", null, null, null)

    private val setupBehavior = BehaviorSubject.create<Boolean>()

    private fun createConnectionObservable(): Observable<Boolean> {
        val beaconConsumer = object : BeaconConsumer {
            override fun getApplicationContext() = appContext

            override fun unbindService(conn: ServiceConnection?) {
                applicationContext.unbindService(conn)
            }

            override fun bindService(intent: Intent?, conn: ServiceConnection?, flag: Int) =
                    applicationContext.bindService(intent, conn, flag)

            override fun onBeaconServiceConnect() {
                setupBehavior.onNext(true)
                setupBehavior.onComplete()
            }
        }

        beaconManager.bind(beaconConsumer)

        return setupBehavior.toFlowable(BackpressureStrategy.LATEST)
                .toObservable()
                .doOnDispose {
                    Log.d("log", "dispose")
                    beaconManager.stopRangingBeaconsInRegion(emptyRangeRegion)
                    beaconManager.removeAllRangeNotifiers()
                    beaconManager.unbind(beaconConsumer)
                }
    }

    fun rangeFlowable() = rangeFlowable(BackpressureStrategy.LATEST)

    fun rangeFlowable(strategy: BackpressureStrategy): Flowable<Beacon> =
            createConnectionObservable()
                    .toFlowable(BackpressureStrategy.LATEST)
                    .flatMap {
                        beaconManager.addRangeNotifier { beacons, _ ->
                            for (beacon in beacons) {
                                beaconBehavior.onNext(beacon)
                            }
                        }
                        beaconManager.startRangingBeaconsInRegion(emptyRangeRegion)
                        beaconBehavior.toFlowable(strategy)
                    }
}
