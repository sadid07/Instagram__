package com.example.instagramclone

import android.app.Application
import com.parse.Parse;
import com.parse.ParseObject

class ParseInstagramApplication : Application() {
    /*
    told and android app that
     */
    override fun onCreate() {
        super.onCreate()
        // Register your parse models
        ParseObject.registerSubclass(Post::class.java)
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());
    }
}