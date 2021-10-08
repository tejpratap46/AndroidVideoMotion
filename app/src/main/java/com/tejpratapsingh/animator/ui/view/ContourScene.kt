package com.tejpratapsingh.animator.ui.view

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.contour.ContourLayout
import com.squareup.picasso.Picasso

class ContourScene(context: Context) : ContourLayout(context) {

    private val containerLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    private val avatar = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        Picasso.get().load("https://i.imgur.com/ajdangY.jpg").into(this)
        layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        setOnClickListener {
            translationX += 1F
        }
    }

    private val bio = TextView(context).apply {
        textSize = 16f
        text = "TAP TO MOVE"
        layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    init {
        x = 0F
        y = 0F
        containerLayout.addView(avatar)
        containerLayout.addView(bio)

        containerLayout.layoutBy(
            x = leftTo { parent.left() },
            y = topTo { parent.top() }
        )
    }
}