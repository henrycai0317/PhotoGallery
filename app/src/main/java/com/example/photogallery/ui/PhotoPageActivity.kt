package com.example.photogallery.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.photogallery.R

class PhotoPageActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_page)

        val fm = supportFragmentManager

        val currentFragment = fm.findFragmentById(R.id.fragment_container)

        if(currentFragment == null){
            val fragment = PhotoPageFragment.newInstance(intent.data!!)
            fm.beginTransaction()
                .add(R.id.fragment_container,fragment)
                .commit()
        }
    }

    companion object{
        fun newIntent(context: Context , photoPageUri: Uri) : Intent{
            return Intent(context,PhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
        }
    }
}