package com.yesleaf.yesdigiboard

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.VideoView

class PlayVideoActivity: Activity() {
    lateinit private var videoView: VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("PlayVideoActivity", intent.getStringExtra("VIDEO" ).toString())
        val path = filesDir.absolutePath + "/" +  intent.getStringExtra("VIDEO" )
        Log.d("PlayVideoActivity", path)

        videoView = VideoView(this)
        videoView.setOnPreparedListener { it.isLooping = true }
        videoView.setVideoURI(Uri.parse(path))
        setContentView(videoView)
        videoView.start()
    }

    override fun onResume() {
        super.onResume()
        if (videoView != null) {
            videoView.start()
        }
    }
}