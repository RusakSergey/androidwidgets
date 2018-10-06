package com.sarcosuchus.androidwidgets

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sarcosuchus.widgets.ColorRatingWidget.Companion.ColorRatingListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ratings.selectItemPosition = 5

        ratings.setColorRatingListener(object : ColorRatingListener {
            override fun onChange(value: Int, intColor: Int) {

            }
        })


        button.setOnClickListener {
            ratings.reset()
        }

        button2.setOnClickListener {
            ratings.selectItemPosition = 3
        }
    }
}
