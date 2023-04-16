package com.tejpratapsingh.motionlib.templates.views

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.ImageView
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionVideo
import com.tejpratapsingh.motionlib.templates.R

class DeviceFrameView(context: Context, bitmap: Bitmap? = null, resId: Int? = null) :
    ContourLayout(context) {

    private val deviceFrame = LayoutInflater.from(context).inflate(R.layout.layout_device_frame, null)
    val imageView: ImageView = deviceFrame.findViewById(R.id.imageView)

    init {
        deviceFrame.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = topTo {
                parent.top()
            }.bottomTo {
                parent.bottom()
            }
        )

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else if (resId != null) {
            imageView.setImageResource(resId)
        }

        contourHeightOf {
            MotionVideo.motionConfig.height.toYInt()
        }
        contourWidthOf {
            MotionVideo.motionConfig.width.toXInt()
        }
    }
}