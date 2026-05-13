package com.example.nammamela

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nammamela.data.MelaDatabase
import com.example.nammamela.data.PlayEntity
import com.example.nammamela.data.FanWallEntity
import com.example.nammamela.data.BookingEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var database: MelaDatabase
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var playAdapter: PlayAdapter
    private lateinit var bookingAdapter: BookingAdapter
    private var currentPlay: PlayEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = MelaDatabase.getDatabase(this)

        setupUI()
        observeData()
        seedInitialData()
    }

    private fun setupUI() {
        findViewById<Button>(R.id.btnBook).setOnClickListener {
            currentPlay?.let { play ->
                val intent = Intent(this, SeatActivity::class.java)
                intent.putExtra("PLAY_ID", play.id)
                intent.putExtra("PLAY_NAME", play.name)
                intent.putExtra("PLAY_DATE", play.date)
                intent.putExtra("PLAY_TIME", play.time)
                intent.putExtra("PLAY_PRICE", play.pricePerSeat)
                startActivity(intent)
            }
        }

        val rvBookings = findViewById<RecyclerView>(R.id.rvBookings)
        bookingAdapter = BookingAdapter(emptyList()) { booking ->
            val intent = Intent(this, TicketDetailActivity::class.java)
            intent.putExtra("BOOKING_ID", booking.id)
            startActivity(intent)
        }
        rvBookings.layoutManager = LinearLayoutManager(this)
        rvBookings.adapter = bookingAdapter

        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        commentAdapter = CommentAdapter(emptyList())
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        val rvPlays = findViewById<RecyclerView>(R.id.rvPlays)
        playAdapter = PlayAdapter(emptyList()) { play ->
            currentPlay = play
            updatePlayUI(play)
        }
        rvPlays.adapter = playAdapter

        val etComment = findViewById<EditText>(R.id.etComment)
        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                sendComment(text)
                etComment.text.clear()
            }
        }
    }

    private fun sendComment(text: String) {
        lifecycleScope.launch {
            val newComment = FanWallEntity(userName = "Fan", comment = text)
            database.melaDao().insertComment(newComment)
            Toast.makeText(this@MainActivity, "Applause sent! 👏", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            database.melaDao().getAllPlays().collectLatest { plays ->
                playAdapter.updateData(plays)
                if (plays.isNotEmpty()) {
                    if (currentPlay == null) {
                        currentPlay = plays[0]
                        updatePlayUI(plays[0])
                    } else {
                        val updated = plays.find { it.id == currentPlay?.id }
                        updated?.let { updatePlayUI(it) }
                    }
                }
            }
        }

        lifecycleScope.launch {
            database.melaDao().getAllComments().collectLatest { comments ->
                commentAdapter.updateData(comments)
            }
        }

        lifecycleScope.launch {
            database.melaDao().getAllBookings().collectLatest { bookings ->
                val bookingHeader = findViewById<TextView>(R.id.tvBookingHeader)
                val rvBookings = findViewById<RecyclerView>(R.id.rvBookings)
                
                if (bookings.isNotEmpty()) {
                    bookingHeader.visibility = View.VISIBLE
                    rvBookings.visibility = View.VISIBLE
                    bookingAdapter.updateData(bookings)
                } else {
                    bookingHeader.visibility = View.GONE
                    rvBookings.visibility = View.GONE
                }
            }
        }
    }

    private fun updatePlayUI(play: PlayEntity) {
        findViewById<TextView>(R.id.txtPlayName).text = play.name
        findViewById<TextView>(R.id.txtPlayDateTime).text = "${play.date} | ${play.time}"
        findViewById<TextView>(R.id.txtDuration).text = "Duration: ${play.duration} | ₹${play.pricePerSeat} per seat"
        findViewById<TextView>(R.id.txtLeadName).text = play.leadActorName
        findViewById<TextView>(R.id.txtComedianName).text = play.comedianName
        findViewById<TextView>(R.id.txtSingerName).text = play.singerName
        findViewById<TextView>(R.id.txtSupportingName).text = play.villainName
        findViewById<TextView>(R.id.txtDancerName).text = play.dancerName

        val glide = Glide.with(this)
        
        glide.load(play.posterUrl)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(findViewById<ImageView>(R.id.imgPoster))
            
        glide.load(play.leadActorPhotoUrl).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.stat_notify_error).into(findViewById<ImageView>(R.id.imgLead))
        glide.load(play.comedianPhotoUrl).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.stat_notify_error).into(findViewById<ImageView>(R.id.imgComedian))
        glide.load(play.singerPhotoUrl).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.stat_notify_error).into(findViewById<ImageView>(R.id.imgSinger))
        glide.load(play.villainPhotoUrl).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.stat_notify_error).into(findViewById<ImageView>(R.id.imgSupporting))
        glide.load(play.dancerPhotoUrl).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.stat_notify_error).into(findViewById<ImageView>(R.id.imgDancer))
    }

    private fun seedInitialData() {
        lifecycleScope.launch {
            // ALWAYS RE-SEED to ensure correct historical Indian play images reflect
            database.melaDao().deleteAllPlays()

            val plays = listOf(
                PlayEntity(
                    id = 1,
                    name = "Sathya Harishchandra",
                    posterUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/BABHRUVAHANA_-_MYTHOLOGICAL_PLAY.JPG/960px-BABHRUVAHANA_-_MYTHOLOGICAL_PLAY.JPG",
                    duration = "3h 30m",
                    date = "Jan 10, 2025",
                    time = "6:00 PM",
                    pricePerSeat = 150.0,
                    leadActorName = "Dr. Rajkumar",
                    leadActorPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Rajkumar_2009_stamp_of_India_%28cropped%29.jpg/960px-Rajkumar_2009_stamp_of_India_%28cropped%29.jpg",
                    comedianName = "Narasimharaju",
                    comedianPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Visit_of_Narasimha_Rao%2C_Indian_Minister_for_Foreign_Affairs%2C_to_the_CEC_%28cropped%29%282%29.jpg/960px-Visit_of_Narasimha_Rao%2C_Indian_Minister_for_Foreign_Affairs%2C_to_the_CEC_%28cropped%29%282%29.jpg",
                    singerName = "P. B. Sreenivas",
                    singerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/A_portrait_of_Shri_Sreenivas_G._Kappanna_who_will_be_presented_with_the_Sangeet_Natak_Akademi_Award_for_Allied_Theatre_Arts_-_Lighting_%26_Stage_Design_by_the_President_Dr._A.P.J_Abdul_Kalam_in_New_Delhi_on_October_26%2C_2004.jpg/960px-thumbnail.jpg",
                    villainName = "M. P. Shankar",
                    villainPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/P._Ravishankar_%2802%29.jpg/960px-P._Ravishankar_%2802%29.jpg",
                    dancerName = "Supporting Cast",
                    dancerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/The_Garden_Left_Behind_world_premiere_with_cast_and_crew%2C_March_9%2C_2019.jpg/960px-The_Garden_Left_Behind_world_premiere_with_cast_and_crew%2C_March_9%2C_2019.jpg"
                ),
                PlayEntity(
                    id = 2,
                    name = "Sangolli Rayanna",
                    posterUrl = "https://upload.wikimedia.org/wikipedia/commons/5/51/Sangolli_Rayanna_Bengaluru.jpg",
                    duration = "3h 00m",
                    date = "Jan 11, 2025",
                    time = "7:00 PM",
                    pricePerSeat = 120.0,
                    leadActorName = "Darshan",
                    leadActorPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/84/Darshan_Thoogudeepa_%281%29.jpg/960px-Darshan_Thoogudeepa_%281%29.jpg",
                    comedianName = "Sadhu Kokila",
                    comedianPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/28/Sadhu_Kokila.jpg/960px-Sadhu_Kokila.jpg",
                    singerName = "K. J. Yesudas",
                    singerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Kj-yesudas-indian-playback-singer-2011.jpg/960px-Kj-yesudas-indian-playback-singer-2011.jpg",
                    villainName = "British Officer",
                    villainPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/Portrait_of_an_officer_of_a_British_Hussar_Regiment.jpg/960px-Portrait_of_an_officer_of_a_British_Hussar_Regiment.jpg",
                    dancerName = "Kittur Warriors",
                    dancerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6a/Nez_Perce_warrior_on_horse.jpg/960px-Nez_Perce_warrior_on_horse.jpg"
                ),
                PlayEntity(
                    id = 3,
                    name = "Babruvahana",
                    posterUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/BABHRUVAHANA_MYTHOLOGICAL_PLAY.JPG/960px-BABHRUVAHANA_MYTHOLOGICAL_PLAY.JPG",
                    duration = "3h 15m",
                    date = "Jan 12, 2025",
                    time = "6:30 PM",
                    pricePerSeat = 180.0,
                    leadActorName = "Dr. Rajkumar",
                    leadActorPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Rajkumar_2009_stamp_of_India_%28cropped%29.jpg/960px-Rajkumar_2009_stamp_of_India_%28cropped%29.jpg",
                    comedianName = "Balakrishna",
                    comedianPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f7/Actor_Nandamuri_Balkrishna_presents_the_Golden_Peacock_award_to_Gurvinder_Singh_for_Anhey_Ghorhey_Da_Daan_%28cropped%29.jpg/960px-Actor_Nandamuri_Balkrishna_presents_the_Golden_Peacock_award_to_Gurvinder_Singh_for_Anhey_Ghorhey_Da_Daan_%28cropped%29.jpg",
                    singerName = "Rajkumar",
                    singerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Rajkumar_Hirani_2014.jpg/960px-Rajkumar_Hirani_2014.jpg",
                    villainName = "Arjuna",
                    villainPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/61/Arjuna_drama_attire_on_stage_01.jpg/960px-Arjuna_drama_attire_on_stage_01.jpg",
                    dancerName = "Palace Dancers",
                    dancerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/64/Dancers_in_Grand_Peterhof_Palace_Petrodvorets_04.jpg/960px-Dancers_in_Grand_Peterhof_Palace_Petrodvorets_04.jpg"
                ),
                PlayEntity(
                    id = 4,
                    name = "Bedara Kannappa",
                    posterUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Assamese_mythological_play_%22Surjya_Mandirot_Surjyasta%22.jpg/960px-Assamese_mythological_play_%22Surjya_Mandirot_Surjyasta%22.jpg",
                    duration = "2h 50m",
                    date = "Jan 13, 2025",
                    time = "6:00 PM",
                    pricePerSeat = 110.0,
                    leadActorName = "Dr. Rajkumar",
                    leadActorPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Rajkumar_2009_stamp_of_India_%28cropped%29.jpg/960px-Rajkumar_2009_stamp_of_India_%28cropped%29.jpg",
                    comedianName = "Balakrishna",
                    comedianPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f7/Actor_Nandamuri_Balkrishna_presents_the_Golden_Peacock_award_to_Gurvinder_Singh_for_Anhey_Ghorhey_Da_Daan_%28cropped%29.jpg/960px-Actor_Nandamuri_Balkrishna_presents_the_Golden_Peacock_award_to_Gurvinder_Singh_for_Anhey_Ghorhey_Da_Daan_%28cropped%29.jpg",
                    singerName = "G. K. Venkatesh",
                    singerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Road_Inauguration.jpg/960px-Road_Inauguration.jpg",
                    villainName = "Shiva",
                    villainPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/92/2017-12-01_Shiva_Keshavan_by_Sandro_Halank%E2%80%9302.jpg/960px-2017-12-01_Shiva_Keshavan_by_Sandro_Halank%E2%80%9302.jpg",
                    dancerName = "Tribal Dancers",
                    dancerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e2/Bison_Horn_Maria_Tribal_Dance_Bastar.jpg/960px-Bison_Horn_Maria_Tribal_Dance_Bastar.jpg"
                ),
                PlayEntity(
                    id = 5,
                    name = "Bhakta Kumbara",
                    posterUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/65/Lord_Shiva_idol.jpg/960px-Lord_Shiva_idol.jpg",
                    duration = "3h 10m",
                    date = "Jan 14, 2025",
                    time = "6:45 PM",
                    pricePerSeat = 130.0,
                    leadActorName = "Dr. Rajkumar",
                    leadActorPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Rajkumar_2009_stamp_of_India_%28cropped%29.jpg/960px-Rajkumar_2009_stamp_of_India_%28cropped%29.jpg",
                    comedianName = "Leelavathi",
                    comedianPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/Leelavathi.jpg/960px-Leelavathi.jpg",
                    singerName = "Rajkumar",
                    singerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Rajkumar_Hirani_2014.jpg/960px-Rajkumar_Hirani_2014.jpg",
                    villainName = "Panduranga",
                    villainPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Panduranga_Rao_at_his_eloquent_best.jpg/960px-Panduranga_Rao_at_his_eloquent_best.jpg",
                    dancerName = "Devotional Singers",
                    dancerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Pannkaj_Kattaria_singer_Pankaj_Kataria.jpg/960px-Pannkaj_Kattaria_singer_Pankaj_Kataria.jpg"
                ),
                PlayEntity(
                    id = 6,
                    name = "Sri Krishnadevaraya",
                    posterUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/SriKrishnadevaraya.jpg/960px-SriKrishnadevaraya.jpg",
                    duration = "3h 20m",
                    date = "Jan 15, 2025",
                    time = "7:15 PM",
                    pricePerSeat = 160.0,
                    leadActorName = "Dr. Rajkumar",
                    leadActorPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/22/Dr._Rajkumar_%284%29.jpg/960px-Dr._Rajkumar_%284%29.jpg",
                    comedianName = "Narasimharaju",
                    comedianPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Visit_of_Narasimha_Rao%2C_Indian_Minister_for_Foreign_Affairs%2C_to_the_CEC_%28cropped%29%282%29.jpg/960px-Visit_of_Narasimha_Rao%2C_Indian_Minister_for_Foreign_Affairs%2C_to_the_CEC_%28cropped%29%282%29.jpg",
                    singerName = "S. P. B.",
                    singerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/61/SP_Balasubrahmanyam_Felicitates_KJ_Yesudas-1.jpg/960px-SP_Balasubrahmanyam_Felicitates_KJ_Yesudas-1.jpg",
                    villainName = "M. P. Shankar",
                    villainPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/P._Ravishankar_%2801%29.jpg/960px-P._Ravishankar_%2801%29.jpg",
                    dancerName = "Classical Dancers",
                    dancerPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/35/Bharatanatyam_is_a_major_form_of_Indian_classical_dance_that_originated_in_the_state_of_Tamil_Nadu.jpg/960px-Bharatanatyam_is_a_major_form_of_Indian_classical_dance_that_originated_in_the_state_of_Tamil_Nadu.jpg"
                )
            )
            plays.forEach { database.melaDao().insertPlayInfo(it) }
        }
    }
}
