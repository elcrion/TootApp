package xyz.gsora.toot

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

import com.bumptech.glide.Glide

class AttachmentAdapter(private val parentCtx: Context, private val attachments: List<Uri>) : BaseAdapter() {

    init {
        if (BuildConfig.DEBUG) {
            Log.d(this.javaClass.simpleName, "MediaAttachmentsAdapter: elements to display -> " + attachments.size)
        }
    }

    override fun getCount(): Int {
        return attachments.size
    }

    override fun getItem(position: Int): Uri {
        return attachments[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        // 1
        val m = attachments[position]

        // 2
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parentCtx)
            convertView = layoutInflater.inflate(R.layout.media_layout, null)
        }

        // 3
        val imageView = convertView!!.findViewById<ImageView>(R.id.mediaPreviewImage)

        // 4
        Glide
                .with(parentCtx)
                .load(m)
                .placeholder(R.mipmap.missing_avatar)
                .crossFade()
                .into(imageView)

        return convertView
    }

}