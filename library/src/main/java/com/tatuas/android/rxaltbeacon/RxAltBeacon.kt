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
import java.util.concurrent.TimeUnit

class RxAltBeacon private constructor(private val appContext: Context,
                                      private val parsers: List<BeaconParser>,
                                      private val intervalSeconds: Long) {

    companion object {
        fun with(context: Context) = RxAltBeacon.Builder(context).build()
    }

    internal val beaconManager: BeaconManager = BeaconManager.getInstanceForApplication(appContext)
            .apply {
                beaconParsers.addAll(parsers)
                foregroundBetweenScanPeriod = TimeUnit.SECONDS.toMillis(intervalSeconds)
            }

    internal fun bindBeaconManager(): Flowable<Boolean> {
        val bindBeaconManagerSubject = PublishSubject.create<Boolean>()

        val beaconConsumer = object : BeaconConsumer {
            override fun getApplicationContext() = appContext

            override fun unbindService(conn: ServiceConnection?) {
                applicationContext.unbindService(conn)
            }

            override fun bindService(intent: Intent?, conn: ServiceConnection?, flag: Int) =
                    applicationContext.bindService(intent, conn, flag)

            override fun onBeaconServiceConnect() {
                bindBeaconManagerSubject.onNext(true)
                bindBeaconManagerSubject.onComplete()
            }
        }

        beaconManager.bind(beaconConsumer)

        return bindBeaconManagerSubject.toFlowable(BackpressureStrategy.LATEST)
                .doOnCancel {
                    beaconManager.removeAllRangeNotifiers()
                    beaconManager.removeAllMonitorNotifiers()
                    beaconManager.unbind(beaconConsumer)
                }
    }

    class Builder constructor(private val context: Context) {

        private var intervalSeconds = 1

        private val beaconParsers = mutableListOf<BeaconParser>()

        fun intervalSeconds(value: Int) = apply { intervalSeconds = value }

        fun addBeaconParsers(vararg value: BeaconParser) = apply { beaconParsers.addAll(value) }

        fun build() = RxAltBeacon(context, beaconParsers.toList(), intervalSeconds.toLong())
    }
}
