package xyz.gsora.toot;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AttachmentAdapter extends BaseAdapter {

    private List<Uri> attachments;
    private Context parentCtx;

    public AttachmentAdapter(Context context, List<Uri>  attachments) {
        parentCtx = context;
        this.attachments = attachments;
        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getSimpleName(), "MediaAttachmentsAdapter: elements to display -> " + attachments.size());
        }
    }

    @Override
    public int getCount() {
        return attachments.size();
    }

    @Override
    public Uri getItem(int position) {
        return attachments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1
        final Uri m = attachments.get(position);

        // 2
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(parentCtx);
            convertView = layoutInflater.inflate(R.layout.media_layout, null);
        }

        // 3
        final ImageView imageView = convertView.findViewById(R.id.mediaPreviewImage);

        // 4
        Glide
                .with(parentCtx)
                .load(m)
                .placeholder(R.mipmap.missing_avatar)
                .crossFade()
                .into(imageView);

        return convertView;
    }

}