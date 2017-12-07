package com.tatuas.android.rxaltbeaconsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tatuas.android.rxaltbeacon.RxAltBeacon
import com.tatuas.android.rxaltbeacon.RxAltBeaconParser
import com.tatuas.android.rxaltbeacon.RxAltBeaconRegion
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()
        setContentView(R.layout.activity_main)

        disposables.add(RxAltBeacon.Builder(this)
                .apply {
                    beaconParsers.add(RxAltBeaconParser.iBeacon)
                    intervalSeconds = 60
                }
                .build()
                .range(RxAltBeaconRegion.all)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { Log.d("log", it.toString()) },
                        { Log.d("log", it.toString()) }))
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}
