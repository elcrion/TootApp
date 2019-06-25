package xyz.gsora.toot

import MastodonTypes.MediaAttachment
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.PopupMenu
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import permissions.dispatcher.NeedsPermission
import retrofit2.Response
import xyz.gsora.toot.Mastodon.Mastodon
import xyz.gsora.toot.Mastodon.ToastMaker
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class SendToot : AppCompatActivity() {

    @BindView(R.id.toot_content)
    lateinit var toot_content: EditText
    @BindView(R.id.characters_remaining)
    lateinit var characters_remaining: TextView
    private var oldColors: ColorStateList? = null
    private var send_toot_menu: MenuItem? = null
    private var replyToId: String? = null
    private var photoUri: Uri? = null
    private val REQUEST_PICK_MEDIA = 100
    @BindView(R.id.attachment_grid)
    lateinit var  attachment_grid:GridView
    private var attachments:ArrayList<Uri> ?= ArrayList()
    private var attachment_adapter: AttachmentAdapter ?=null
    private  var attachmentIds:ArrayList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_toot)
        title = "Send toot"
        replyToId = null
        ButterKnife.bind(this)
        setupCharacterCounter()

        ActivityCompat.requestPermissions(this,
                                          arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE ),
                                          123)

        attachment_adapter= AttachmentAdapter(this,attachments)
        attachment_grid.adapter = attachment_adapter

        attachment_grid.onItemClickListener = AdapterView.OnItemClickListener {parent,view,position,id ->

            var  selected:Uri = parent.getItemAtPosition(position) as Uri
            val imagePopup = ImagePopup(this@SendToot)
            imagePopup.initiatePopupWithGlide(selected.toString(), ImagePopup.Type.image)
            imagePopup.viewPopup()


        }


        attachment_grid.onItemLongClickListener = AdapterView.OnItemLongClickListener{parent,view,position,id ->

            var  selected:Uri = parent.getItemAtPosition(position) as Uri
            removeItem(selected)

        }


        oldColors = characters_remaining.textColors

        val reply = intent
        if (reply != null) {
            if (reply.action != null && reply.action == REPLY_ACTION) {
                replyToId = reply.getStringExtra(REPLY_TO_ID)
                val handlesString = StringBuilder()
                val handles = reply.getStringArrayListExtra(REPLY_TO)
                for (s in handles) {
                    handlesString.append("@").append(s).append(" ")

                }
                toot_content.append(handlesString.toString())
            }
        }
    }



    private fun attachmentSuccess(response: Response<MediaAttachment> ){

        Log.d("Tag", "Post attachment Successful: post ok!")
        attachmentIds.add(response.body().id.toString())

        if(attachmentIds.size == attachments?.size){

                postStatus()
        }

    }



    fun generateFileName(mimeType:String): String {
        val randomNo = Random().nextInt(1000)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

        return buildString {
            append("Troutoss_${Calendar.getInstance().time}_$randomNo")
            extension?.run { append(".$extension") }
        }
    }


    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun sendAttachments() {

        attachments?.forEach{ attachment ->

            var mimeType = this.contentResolver.getType(attachment)
            val bytes = contentResolver.openInputStream(attachment).use { it.readBytes() }
            val requestFile = RequestBody.create(MediaType.parse(mimeType), bytes)
            val part = MultipartBody.Part.createFormData("file", generateFileName(mimeType), requestFile)

            var postAttachment = Mastodon.instance.postAttachments(part)

            postAttachment.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(

                            Consumer<Response<MediaAttachment>> { this.attachmentSuccess(it) },
                            Consumer<Throwable> {

                                AlertDialog.Builder(this@SendToot)
                                        .setTitle("Toot")
                                        .setMessage(it.message)
                                        .setCancelable(true)
                                        .setPositiveButton("Ok",null)
                                        .show()
                            }

                    )

        }
    }




    /*
    Enable the "send" button
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.send_toot_menu, menu)
        send_toot_menu = menu.findItem(R.id.send_toot_button)

        send_toot_menu!!.setOnMenuItemClickListener { m: MenuItem ->
            sendAttachments()
            true
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun postStatus() {
        val sendStatus = Intent(applicationContext, PostStatus::class.java)
        sendStatus.putExtra(PostStatus.STATUS, toot_content.text.toString())
        sendStatus.putExtra(PostStatus.REPLYID, replyToId)
        sendStatus.putExtra(PostStatus.MEDIAIDS, attachmentIds)
        applicationContext.startService(sendStatus)
        finish()
    }

    private fun getTextColor(context: Context, chars: Int): Int? {
        if (chars <= 50) {
            return ContextCompat.getColor(context, R.color.colorPrimary)
        }

        return if (chars <= 200) {
            ContextCompat.getColor(context, R.color.colorAccent)
        } else null

    }



    private fun removeItem(item:Uri): Boolean {


        AlertDialog.Builder(this@SendToot)
                .setTitle("Delete Item")
                .setMessage("Do you want to delete this item ?")
                .setCancelable(true)
                .setPositiveButton("yes", { dialog, which ->
                    attachments?.remove(item)
                    updateAdapter()
                })
                .show()
        return true
    }


    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showMediaPicker(v: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                type = "image/* video/mp4"
            } else {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/mp4"))
            }
        }

        startActivityForResult(intent, REQUEST_PICK_MEDIA)
    }


    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun takePhoto(v: View) {
        try {
            photoUri = TakePhotoHelper.createImageFile(this)
        } catch (e: IOException) {
            Log.e("createImageFile failed",e.message)
        }

        photoUri?.let { uri ->
            TakePhotoHelper.getTakePictureIntent(uri, this)?.let {
                startActivityForResult(it, REQUEST_TAKE_PHOTO)
            }
        }

    }



    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putParcelable(SAVE_PHOTO_URI, photoUri)
        }
        super.onSaveInstanceState(outState)
    }

    private fun setupCharacterCounter() {
        val characterWatcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val len = 500 - toot_content.length()
                characters_remaining.text = String.format(Locale.getDefault(), "%d", len)

                val color = getTextColor(applicationContext, len)

                if (color != null) {
                    characters_remaining.setTextColor(color)
                } else {
                    characters_remaining.setTextColor(oldColors)
                }

                if (send_toot_menu != null) {
                    send_toot_menu!!.isEnabled = len > 0
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        }

        toot_content.addTextChangedListener(characterWatcher)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == REQUEST_PICK_MEDIA && resultCode == Activity.RESULT_OK && data?.data != null -> {

                    attachments?.add(data.data)

            }
            requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK -> {
                photoUri?.let {
                   attachments?.add(it)
                }
            }
        }

        updateAdapter()

    }

    private fun updateAdapter() {
        attachment_adapter?.notifyDataSetChanged()
        attachment_grid.adapter = attachment_adapter
    }


    fun showVisibilityMenu(v: View) {
        val rV = v as ImageButton
        val popup = PopupMenu(this@SendToot, v)
        popup.menuInflater.inflate(R.menu.set_visibility_menu, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            Log.d(TAG, item.itemId.toString())
            rV.setImageDrawable(item.icon)
            when (item.itemId) {
                R.id.visibility_public -> ToastMaker.buildToasty(this, "Your post will appear in public timelines").show()
                R.id.visibility_unlisted -> ToastMaker.buildToasty(this, "Your post will not appear public timelines").show()
                R.id.visibility_private -> ToastMaker.buildToasty(this, "Your post will appear to followers only").show()
                R.id.visibility_direct -> ToastMaker.buildToasty(this, "Your post will appear to mentioned user only").show()
                else -> Log.d(TAG, "memes")
            }
            true
        }

        val menuHelper = MenuPopupHelper(this@SendToot, popup.menu as MenuBuilder, v)
        menuHelper.setForceShowIcon(true)
        menuHelper.show()
    }




    companion object {
        private val SAVE_PHOTO_URI = "save_photo_uri"
        val REPLY_ACTION = "xyz.gsora.toot.ReplyToStatus"
        val REPLY_TO = "xyz.gsora.toot.ReplyTo"
        val REPLY_TO_ID = "xyz.gsora.toot.ReplyToId"
        val  MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = "xyz.gsora.toot.READ_EXTERNAL_STORAGE"
        private val REQUEST_TAKE_PHOTO = 100
        private val TAG = SendToot::class.java.simpleName
    }

}
