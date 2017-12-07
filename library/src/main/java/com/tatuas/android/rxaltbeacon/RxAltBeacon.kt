package com.tatuas.android.rxaltbeacon

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import java.util.concurrent.TimeUnit

class RxAltBeacon private constructor(private val appContext: Context,
                                      private val parsers: List<BeaconParser>,
                                      private val intervalSeconds: Long) {

    private val beaconManager: BeaconManager = BeaconManager.getInstanceForApplication(appContext)
            .apply {
                beaconParsers.addAll(parsers)
                foregroundBetweenScanPeriod = TimeUnit.SECONDS.toMillis(intervalSeconds)
            }

    private val setupBeaconManagerBehavior = PublishSubject.create<Boolean>()

    private val rangeBehavior = PublishSubject.create<RxAltBeaconRange>()

    private fun setupBeaconManager(): Flowable<Boolean> {
        val beaconConsumer = object : BeaconConsumer {
            override fun getApplicationContext() = appContext

            override fun unbindService(conn: ServiceConnection?) {
                applicationContext.unbindService(conn)
            }

            override fun bindService(intent: Intent?, conn: ServiceConnection?, flag: Int) =
                    applicationContext.bindService(intent, conn, flag)

            override fun onBeaconServiceConnect() {
                setupBeaconManagerBehavior.onNext(true)
                setupBeaconManagerBehavior.onComplete()
            }
        }

        beaconManager.bind(beaconConsumer)

        return setupBeaconManagerBehavior.toFlowable(BackpressureStrategy.LATEST)
                .doOnCancel {
                    beaconManager.removeAllRangeNotifiers()
                    beaconManager.removeAllMonitorNotifiers()
                    beaconManager.unbind(beaconConsumer)
                }
    }

    fun range(region: Region, strategy: BackpressureStrategy = BackpressureStrategy.LATEST)
            : Flowable<RxAltBeaconRange> = setupBeaconManager()
            .doOnCancel {
                beaconManager.stopRangingBeaconsInRegion(region)
            }
            .flatMap {
                beaconManager.addRangeNotifier { beacons, region ->
                    rangeBehavior.onNext(RxAltBeaconRange(beacons.toList(), region))
                }
                beaconManager.startRangingBeaconsInRegion(region)

                rangeBehavior.toFlowable(strategy)
            }

    class Builder(private val context: Context) {

        var intervalSeconds = 1

        val beaconParsers = mutableListOf<BeaconParser>()

        fun build() = RxAltBeacon(context, beaconParsers.toList(), intervalSeconds.toLong())
    }
}
