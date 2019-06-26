package xyz.gsora.toot

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.VideoView

import com.bumptech.glide.Glide

class ImagePopup : AppCompatImageView {

    private var popupWindow: PopupWindow? = null
    internal lateinit var layout: View
    private var imageView: ImageView? = null
    private var videoView: VideoView? = null
    var windowHeight = 0
    var windowWidth = 0
    /**
     * Close Options
     */

    var isImageOnClickClose: Boolean = false
    var isHideCloseIcon: Boolean = false
    var isFullScreen: Boolean = false

    private var backgroundColor = Color.parseColor("#FFFFFF")

    enum class Type {
        video, image, gif
    }


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun getBackgroundColor(): Int {
        return backgroundColor
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
    }



    fun initiatePopup(drawable: Drawable) {

        try {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            layout = inflater.inflate(R.layout.popup, findViewById<ViewGroup>(R.id.popup))

            layout.setBackgroundColor(getBackgroundColor())

            imageView = layout.findViewById(R.id.imageView)
            imageView!!.setImageDrawable(drawable)

            /** Background dim part  */
            //            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            //            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) layout.getLayoutParams();
            //            layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            //            layoutParams.dimAmount = 0.3f;
            //            windowManager.updateViewLayout(layout, layoutParams);

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }




    private fun getType(url:String):ImagePopup.Type{
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return   when {
            mime.startsWith("image") -> ImagePopup.Type.image
            mime.startsWith("video") -> Type.video
            else -> ImagePopup.Type.gif
        }

    }


    /**
     * optimize version
     * @param imageUrl
     */
    fun initiatePopupWithGlide(imageUrl: String) {

        try {

            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            layout = inflater.inflate(R.layout.popup, findViewById<ViewGroup>(R.id.popup))

            layout.setBackgroundColor(getBackgroundColor())

            imageView = layout.findViewById(R.id.imageView)
            videoView = layout.findViewById(R.id.videoView)

            val type  = getType(imageUrl)

            when (type) {

                Type.video -> {

                    imageView!!.visibility = View.GONE
                    videoView!!.visibility = View.VISIBLE
                    videoView!!.setVideoPath(imageUrl)
                    val uri = Uri.parse(imageUrl)
                    videoView!!.setVideoURI(uri)
                    videoView!!.start()
                }

                Type.gif -> {

                    imageView!!.visibility = View.VISIBLE
                    videoView!!.visibility = View.GONE

                    Glide.with(context)
                            .load(imageUrl)
                            .into(imageView!!)
                }

                Type.image -> {
                    imageView!!.visibility = View.VISIBLE
                    videoView!!.visibility = View.GONE
                    Glide.with(context)
                            .load(imageUrl)
                            .into(imageView!!)
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImagePopup ", e.message)
        }

    }


    fun setLayoutOnTouchListener(onTouchListener: View.OnTouchListener) {
        layout.setOnTouchListener(onTouchListener)
    }

    fun viewPopup() {

        val metrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(metrics)


        var width = metrics.widthPixels
        var height = metrics.heightPixels

        if (isFullScreen) {
            popupWindow = PopupWindow(layout, width, height, true)
        } else {
            if (windowHeight != 0 || windowWidth != 0) {
                width = windowWidth
                height = windowHeight
                popupWindow = PopupWindow(layout, width, height, true)
            } else {
                popupWindow = PopupWindow(layout, (width * .8).toInt(), (height * .6).toInt(), true)
            }
        }


        popupWindow!!.showAtLocation(layout, Gravity.CENTER, 0, 0)

        val closeIcon = layout.findViewById<ImageView>(R.id.closeBtn)

        if (isHideCloseIcon) {
            closeIcon.visibility = View.GONE
        }
        closeIcon.setOnClickListener { popupWindow!!.dismiss() }

        if (isImageOnClickClose) {
            imageView!!.setOnClickListener { popupWindow!!.dismiss() }
        }

    }

}
