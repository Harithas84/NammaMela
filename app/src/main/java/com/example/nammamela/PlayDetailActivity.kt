package com.example.nammamela

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nammamela.data.MelaDatabase
import com.example.nammamela.data.PlayEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlayDetailActivity : AppCompatActivity() {

    private lateinit var database: MelaDatabase
    private var playId: Int = -1
    private var selectedPlay: PlayEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_detail)

        playId = intent.getIntExtra("PLAY_ID", -1)
        if (playId == -1) {
            finish()
            return
        }

        database = MelaDatabase.getDatabase(this)

        setupUI()
        observePlayData()
    }

    private fun setupUI() {
        findViewById<ImageView>(R.id.btnDetailBack).setOnClickListener { finish() }
        
        findViewById<Button>(R.id.detailBtnBook).setOnClickListener {
            selectedPlay?.let { play ->
                val intent = Intent(this, SeatActivity::class.java)
                intent.putExtra("PLAY_ID", play.id)
                intent.putExtra("PLAY_NAME", play.name)
                intent.putExtra("PLAY_DATE", play.date)
                intent.putExtra("PLAY_TIME", play.time)
                intent.putExtra("PLAY_PRICE", play.pricePerSeat)
                startActivity(intent)
            }
        }
    }

    private fun observePlayData() {
        lifecycleScope.launch {
            database.melaDao().getPlayById(playId).collectLatest { play ->
                play?.let {
                    selectedPlay = it
                    updateUI(it)
                }
            }
        }
    }

    private fun updateUI(play: PlayEntity) {
        findViewById<TextView>(R.id.detailTxtPlayName).text = play.name
        findViewById<TextView>(R.id.detailTxtPlayDateTime).text = "${play.date} | ${play.time}"
        findViewById<TextView>(R.id.detailTxtDuration).text = "Duration: ${play.duration}"
        
        findViewById<TextView>(R.id.detailTxtLeadName).text = play.leadActorName
        findViewById<TextView>(R.id.detailTxtComedianName).text = play.comedianName
        findViewById<TextView>(R.id.detailTxtSingerName).text = play.singerName
        findViewById<TextView>(R.id.detailTxtSupportingName).text = play.villainName
        findViewById<TextView>(R.id.detailTxtDancerName).text = play.dancerName

        val glide = Glide.with(this)
        
        glide.load(play.posterUrl)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(findViewById<ImageView>(R.id.detailImgPoster))
            
        glide.load(play.leadActorPhotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(findViewById<ImageView>(R.id.detailImgLead))
        glide.load(play.comedianPhotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(findViewById<ImageView>(R.id.detailImgComedian))
        glide.load(play.singerPhotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(findViewById<ImageView>(R.id.detailImgSinger))
        glide.load(play.villainPhotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(findViewById<ImageView>(R.id.detailImgSupporting))
        glide.load(play.dancerPhotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(findViewById<ImageView>(R.id.detailImgDancer))
        
        // Dynamic "About" text based on play
        findViewById<TextView>(R.id.detailTxtAbout).text = when(play.id) {
            1 -> "The timeless tale of King Harishchandra, known for his absolute adherence to truth despite losing his kingdom and family. A cornerstone of Indian theatrical tradition."
            2 -> "The heroic saga of Kranti Veera Sangolli Rayanna, the legendary warrior who fought against British rule in the 19th century. A story of extreme bravery and patriotism."
            3 -> "The mythological encounter between Arjuna and his son Babruvahana. Filled with grand dialogue and dramatic fight sequences, it's a spectacle of rural stagecraft."
            else -> "A masterpiece of traditional Indian drama, bringing legendary stories to life with powerful performances, classical songs, and grand costumes."
        }
    }
}
