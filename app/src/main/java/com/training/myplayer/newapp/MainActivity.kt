package com.training.myplayer.newapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cuihai.framwork.utilv2.ExecUtil
import com.google.android.exoplayer2.PlaybackPreparer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.training.myplayer.PlayerBean
import com.training.player.AudioPlayer
import com.training.player.exception.PlaybackException
import com.training.player.listener.PlaybackListener
import com.training.player.listener.SimplePlaybackListener
import com.xpleemoon.sample.myexoplayer.playercontroller.PlaybackState
import java.io.*
import java.net.Socket
import java.util.*


class MainActivity : AppCompatActivity() {
    var play: View? = null
    var socket: Socket? = null
    private var currentPositionMS: Long = 0

    private val audioPlayer by lazy {

        AudioPlayer(applicationContext).also {
            it.startEventLogger()
        }
    }
    private var playerView: PlayerView? = null

    /**
     * flv貌似不能正常播
     */
    private val sources = mapOf(
        "1" to "http://vfx.mtime.cn/Video/2019/03/09/mp4/190309153658147087.mp4",
//        "2" to "http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4",
//        "卡农" to "https://m10.music.126.net/20220531164721/a297762af80ada3c9ef22e8f3357f581/ymusic/055a/535b/5359/206bffe62bd149b82531d34775e2eeb3.mp3",
//        "钢琴-故事的开始" to "http://img.tukuppt.com/preview_music/00/08/60/preview-5b835eb33f4f15003.mp3",
//        "克罗地亚狂想曲" to "https://m10.music.126.net/20220531165101/6d47cccb9bf207874eb80fb4c2f2c16c/ymusic/0e08/035f/040f/9bde0b60bd45d74ca0233ece3b2624e7.mp3",
//        "新闻联播本地raw" to RawResourceDataSource.buildRawResourceUri(R.raw.cctv).toString(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        play = findViewById(R.id.btn_play)
        playerView = findViewById(R.id.playview)
        audioPlayer.setPlaybackPreparer(PlaybackPreparer {
            playerView?.let {
                audioPlayer.bindPlayView(it)
            }
            val mediaSource = ConcatenatingMediaSource()
            sources.forEach {
                mediaSource.addMediaSource(audioPlayer.mediaSource(it.value))
            }
            audioPlayer.prepare(mediaSource)
        })

        // 启动播放调试
        audioPlayer.startPlaybackDebug {

        }
        // 启动播放事件打印
        audioPlayer.startEventLogger()

        val playbackListener: PlaybackListener = object : SimplePlaybackListener() {

            override fun onPlaybackProgressChanged(
                currentPositionMS: Long,
                bufferedPositionMS: Long,
                durationMS: Long
            ) {
                Log.i(
                    "onProgressChanged",
                    "currentPositionMS" + currentPositionMS + "bufferedPositionMS" + bufferedPositionMS + "durationMS" + durationMS
                )
                this@MainActivity.currentPositionMS = currentPositionMS
            }

            override fun onPlaybackStateChanged(playbackState: PlaybackState) {

            }

            override fun onPlayerError(error: PlaybackException) {

            }
        }
        audioPlayer.addPlaybackListener(playbackListener)
        timer.schedule(timerTask, 500, 500);

        play?.setOnClickListener {
            audioPlayer.pause()
            isSend = true
        }
    }
    fun cancelTimer(){
        timer.cancel()
    }

    var dos: ObjectOutputStream? = null
    var dis: ObjectInputStream? = null
    var isSend = false

    var timer: Timer = Timer()
    var timerTask: TimerTask = object : TimerTask() {
        override fun run() {
            val message = Message()
            message.what = 2
            handler.sendMessage(message)
        }
    }


    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                if (msg.obj != null) {
                    var bean = msg.obj as PlayerBean
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    intent.putExtra("play", bean)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    this@MainActivity.startActivity(intent)
                }
            } else if (msg.what == 2) {
                if (UdpServerSocket.ipv4 == "" || UdpServerSocket.ipv4.isEmpty()) return
                ExecUtil.execute {
                    try {
                        socket = SingleSocket.getInstance().socket
                        ExecUtil.executeUI {
                            if (socket != null) {
                                Toast.makeText(this@MainActivity, "已连接到服务端", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        Thread(Send(), "发送线程").start()
                        Thread(Receive(), "接受线程线程").start()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                cancelTimer()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.extras != null && intent.extras?.getSerializable("play") != null) {
            val dto = intent.extras?.getSerializable("play") as PlayerBean

            Toast.makeText(this, "onNewIntent" + dto.position.toString(), Toast.LENGTH_SHORT)
                .show()
            audioPlayer.seekTo(dto.position)
        }

    }

    inner class Receive : Runnable {
        override fun run() {
            var recMsg: PlayerBean? = null
            if (socket == null) return
            dis = ObjectInputStream(socket?.getInputStream());
            while (true) {
                try {
                    if (dis == null || dis?.readObject() != null) {
                        recMsg = dis?.readObject() as PlayerBean
                    }
                } catch (e: EOFException) {
                    e.printStackTrace()
                }
                if (recMsg != null) {
                    Log.d("-----", "inputStream:$dis")
                    val message = Message()
                    message.obj = recMsg
                    message.what = 1
                    handler.sendMessage(message)
                    recMsg = null
                }
            }
        }
    }

    inner class Send : Runnable {
        override fun run() {
            if (socket == null) return
            dos = ObjectOutputStream(socket?.getOutputStream());
            while (true) {
                if (dos == null) return
                if (isSend) {
                    try {
                        val bean = PlayerBean().apply {
                            position = currentPositionMS
                            url = ""
                        }
                        dos?.writeObject(bean)
                        dos?.flush()
                        Log.d("-----", "发送了一条消息" + bean)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    isSend = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        audioPlayer.play()
    }

    override fun onPause() {
        super.onPause()
        audioPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stopPlaybackDebug()
        audioPlayer.stopEventLogger()
        audioPlayer.stop()
        audioPlayer.release()
        playerView?.let {
            audioPlayer.unbindPlayView(it)
        }
        socket?.close()
    }
}