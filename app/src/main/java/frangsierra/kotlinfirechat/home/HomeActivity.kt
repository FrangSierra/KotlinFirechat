package frangsierra.kotlinfirechat.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.core.flux.FluxActivity

class HomeActivity : FluxActivity() {
    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, HomeActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
    }
}