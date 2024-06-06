package com.stream.baba

import android.app.Application

class Streambaba : Application() {
    override fun onCreate() {
        super.onCreate()
        strambaba = this
    }
    companion object{
        var strambaba: Streambaba? =null
            private set
    }
}