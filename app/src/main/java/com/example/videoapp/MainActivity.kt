package com.example.videoapp

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.net.URL
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    // كل كم فيديو تطلع الإعلانة؟ 1 = قبل كل فيديو
    private val showAdEveryNVideos = 1

    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var detector: GestureDetector

    private var videos: List<String> = emptyList()
    private var index = 0
    private var videosSinceAd = 0
    private var interstitial: InterstitialAd? = null
    private var adIsShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSystemBars()

        playerView = findViewById(R.id.playerView)

        MobileAds.initialize(this) {}
        loadAd()

        player = ExoPlayer.Builder(this).build().also {
            playerView.player = it
            it.repeatMode = Player.REPEAT_MODE_OFF
            it.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    // كي يخلص الفيديو (15 ثانية) نمشيو للي بعدو
                    if (state == Player.STATE_ENDED && !adIsShowing) goTo(index + 1)
                }
            })
        }

        // سحب فوق = الفيديو التالي، سحب تحت = الفيديو السابق
        detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent,
                velocityX: Float, velocityY: Float
            ): Boolean {
                if (e1 == null || adIsShowing) return false
                val dy = e1.y - e2.y
                if (abs(dy) > 150 && abs(dy) > abs(e1.x - e2.x)) {
                    goTo(if (dy > 0) index + 1 else index - 1)
                    return true
                }
                return false
            }
        })
        playerView.setOnTouchListener { _, ev ->
            detector.onTouchEvent(ev)
            true
        }

        // نجيبو قائمة الفيديوهات من الأنترنت
        fetchVideoList()
    }

    /** نقرا ملف list.txt من الأنترنت: كل سطر رابط فيديو */
    private fun fetchVideoList() {
        Thread {
            val list = try {
                val text = URL(getString(R.string.videos_list_url)).readText()
                text.lines().map { it.trim() }.filter { it.startsWith("http") }
            } catch (e: Exception) {
                emptyList()
            }
            runOnUiThread {
                if (list.isEmpty()) {
                    Toast.makeText(this, R.string.no_videos, Toast.LENGTH_LONG).show()
                } else {
                    videos = list
                    goTo(0, forceAd = true) // أول فيديو: نبدا بالإعلانة
                }
            }
        }.start()
    }

    /** نروح للفيديو رقم i (مع الإعلانة قبلو) */
    private fun goTo(i: Int, forceAd: Boolean = false) {
        val n = videos.size
        if (n == 0) return
        index = ((i % n) + n) % n
        player.pause()

        videosSinceAd++
        val shouldShowAd = forceAd || videosSinceAd >= showAdEveryNVideos
        val ad = interstitial

        if (shouldShowAd && ad != null) {
            videosSinceAd = 0
            adIsShowing = true
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitial = null
                    adIsShowing = false
                    loadAd()
                    playCurrent()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitial = null
                    adIsShowing = false
                    loadAd()
                    playCurrent()
                }
            }
            ad.show(this)
        } else {
            // ما كاينش إعلانة جاهزة: نشغلو الفيديو مباشرة ونحضرو وحدة
            if (interstitial == null) loadAd()
            playCurrent()
        }
    }

    private fun playCurrent() {
        player.setMediaItem(MediaItem.fromUri(videos[index]))
        player.prepare()
        player.playWhenReady = true
    }

    private fun loadAd() {
        InterstitialAd.load(
            this,
            getString(R.string.admob_interstitial_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            }
        )
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onStart() {
        super.onStart()
        if (::player.isInitialized && !adIsShowing) player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        if (::player.isInitialized) player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) player.release()
    }
}
