package xyz.gsora.toot;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

public class ImagePopup extends android.support.v7.widget.AppCompatImageView {
    private Context context;
    private PopupWindow popupWindow;
    View layout;
    private ImageView imageView;
    private VideoView videoView;
    enum Type{video,image,gif}
    private int windowHeight = 0;
    private int windowWidth = 0;
    private boolean imageOnClickClose;
    private boolean hideCloseIcon;
    private boolean fullScreen;

    private int backgroundColor = Color.parseColor("#FFFFFF");


    public ImagePopup(Context context) {
        super(context);
        this.context = context;
    }

    public ImagePopup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


    /**
     * Close Options
     **/

    public void setImageOnClickClose(boolean imageOnClickClose) {
        this.imageOnClickClose = imageOnClickClose;
    }


    public boolean isImageOnClickClose() {
        return imageOnClickClose;
    }

    public boolean isHideCloseIcon() {
        return hideCloseIcon;
    }

    public void setHideCloseIcon(boolean hideCloseIcon) {
        this.hideCloseIcon = hideCloseIcon;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public void initiatePopup(Drawable drawable) {

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            layout = inflater.inflate(R.layout.popup, findViewById(R.id.popup));

            layout.setBackgroundColor(getBackgroundColor());

            imageView = layout.findViewById(R.id.imageView);
            imageView.setImageDrawable(drawable);

            /** Background dim part **/
//            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) layout.getLayoutParams();
//            layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//            layoutParams.dimAmount = 0.3f;
//            windowManager.updateViewLayout(layout, layoutParams);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     * optimize version
     * @param imageUrl
     */
    public void initiatePopupWithGlide(String imageUrl,Type type) {

        try {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            layout = inflater.inflate(R.layout.popup, findViewById(R.id.popup));

            layout.setBackgroundColor(getBackgroundColor());

            imageView = layout.findViewById(R.id.imageView);
            videoView = layout.findViewById(R.id.videoView);

            switch (type){

                case video:

                    imageView.setVisibility(GONE);
                    videoView.setVisibility(VISIBLE);
                    videoView.setVideoPath(imageUrl);

                    videoView.setOnPreparedListener(mediaPlayer -> {
                        videoView.start();
                        mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {
                            MediaController mediaController = new MediaController(context);
                            videoView.setMediaController(mediaController);
                            mediaController.setAnchorView(videoView);
                        });
                    });

                    break;

                case gif:

                    imageView.setVisibility(VISIBLE);
                    videoView.setVisibility(GONE);

                    Glide.with(context)
                            .load(imageUrl)
                            .into(imageView);
                    break;

                case image:
                    imageView.setVisibility(VISIBLE);
                    videoView.setVisibility(GONE);
                    Glide.with(context)
                            .load(imageUrl)
                            .into(imageView);
                    break;

            }



        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ImagePopup ", e.getMessage());
        }
    }




    public void setLayoutOnTouchListener(OnTouchListener onTouchListener) {
        layout.setOnTouchListener(onTouchListener);
    }

    public void viewPopup() {

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);


        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        if(isFullScreen()) {
            popupWindow = new PopupWindow(layout, (width), (height), true);
        }else {
            if (windowHeight != 0 || windowWidth != 0) {
                width = windowWidth;
                height = windowHeight;
                popupWindow = new PopupWindow(layout, (width), (height), true);
            } else {
                popupWindow = new PopupWindow(layout, (int) (width * .8), (int) (height * .6), true);
            }
        }


        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

        ImageView closeIcon = layout.findViewById(R.id.closeBtn);

        if (isHideCloseIcon()) {
            closeIcon.setVisibility(View.GONE);
        }
        closeIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        if (isImageOnClickClose()) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();
                }
            });
        }

    }

}
