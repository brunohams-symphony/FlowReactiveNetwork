package ru.beryukhov.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.beryukhov.reactivenetwork.ReactiveNetwork

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        ReactiveNetwork().observeInternetConnectivity().onEach {
            Log.i("MainActivity", "InternetConnectivity changed on $it")
        }.launchIn(CoroutineScope(Dispatchers.Default))

        ReactiveNetwork().observeNetworkConnectivity(applicationContext).onEach {
            Log.i("MainActivity", "NetworkConnectivity changed on $it")
        }.launchIn(CoroutineScope(Dispatchers.Default))
    }
}