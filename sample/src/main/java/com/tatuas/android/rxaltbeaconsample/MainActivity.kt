package com.tatuas.android.rxaltbeaconsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.tatuas.android.rxaltbeacon.BeaconParserConstants
import com.tatuas.android.rxaltbeacon.RxAltBeacon
import com.tatuas.android.rxaltbeacon.range
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var disposables: CompositeDisposable

    private lateinit var mainAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        disposables = CompositeDisposable()

        mainAdapter = MainAdapter(this)

        recyclerView.apply {
            swapAdapter(mainAdapter, true)
            setHasFixedSize(true)
            addItemDecoration(
                    DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(
                    this@MainActivity, LinearLayoutManager.VERTICAL, false)
        }

        RxAltBeacon.Builder(this)
                .addBeaconParsers(BeaconParserConstants.I_BEACON)
                .intervalSeconds(5)
                .build()
                .range()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            mainAdapter.addAll(
                                    it.beacons.joinToString(separator = "\n") { "{$it}" })
                        },
                        onError = {
                            Toast.makeText(this@MainActivity, it.toString(), Toast.LENGTH_LONG).show()
                        })
                .addTo(disposables)
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}
