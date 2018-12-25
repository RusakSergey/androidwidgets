package com.sarcosuchus.androidwidgets

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sarcosuchus.widgets.ColorRatingWidget.Companion.ColorRatingListener
import com.sarcosuchus.widgets.vectorpathimageview.VectorPath
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val nameArray: Array<String> by lazy {
        resources.getStringArray(R.array.pain_region)
    }

    private val descriptionArray: Array<String> by lazy {
        resources.getStringArray(R.array.pain_region_description)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pathNameArray: Array<Array<String>> = arrayOf(arrayOf("CERVICAL"), arrayOf("THORACIC"),
                arrayOf("SHOULDER"), arrayOf("SHOULDERBLADES"), arrayOf("DIAPHRAGM"), arrayOf("ELBOW"),
                arrayOf("LUMBAR", "PELVIS", "LUMBAR_AND_PELVIS"), arrayOf("PELVIS", "LUMBAR_AND_PELVIS"),
                arrayOf("HIPS"), arrayOf("WRISTSANDHANDS"), arrayOf("KNEES"), arrayOf("ANKLESANDFEET"))

        fragment_pain_questionnaire_man.vectorPathArray = Array(12) {
            VectorPath(pathNames = pathNameArray[it],
                    name = nameArray[it], description = descriptionArray[it])
        }
        fragment_pain_questionnaire_man.colorRegion = ratings.getCurrentColor()
        fragment_pain_questionnaire_man.colorStroke = ratings.getCurrentColor()

        ratings.setColorRatingListener(object : ColorRatingListener {
            override fun onChange(value: Int, intColor: Int) {
                fragment_pain_questionnaire_man.setSelectedColor(intColor, intColor)
            }
        })


        button.setOnClickListener {
            ratings.reset()
            fragment_pain_questionnaire_man.next(ratings.getCurrentColor(), ratings.getCurrentColor())
        }

        button2.setOnClickListener {
            ratings.selectItemPosition = 4
            fragment_pain_questionnaire_man.previous(ratings.getCurrentColor(), ratings.getCurrentColor())
        }
    }
}
