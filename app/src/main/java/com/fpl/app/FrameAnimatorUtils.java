package com.fpl.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * 对所有View快速添加 帧动画
 * 可以自定义帧动画属性
 * int frames_number;//总共多少帧
 * int frames_rate;//帧率 毫秒
 * int h_frames;//高有几帧
 * int w_frames;//宽有几帧
 * int loop_from = 1;//从第几帧开始
 * int loop_to;//到第几帧结束
 * View view;//显示控件
 * int resId;//资源id
 * boolean isAutoAnimation;//是否自动播放动画
 * <p>
 * 使用方法
 * FrameAnimatorUtils build = new FrameAnimatorUtils.Builder()
 * .setFrames_number(20)
 * .setFrames_rate(120)
 * .setH_frames(4)
 * .setW_frames(5)
 * .setLoop_from(1)
 * .setLoop_to(20)
 * .setAutoAnimation(false)
 * .setView(imgView)
 * .setResId(R.drawable.pao)
 * .build()
 * .display();
 * <p>
 * 帧动画只需要这一个类就可以了 剩下的就看UI切图了
 */
public class FrameAnimatorUtils {

    public int currentIndex = 1;
    private Builder builder;

    private final Rect mRect = new Rect();
    private BitmapRegionDecoder mDecoder;
    private int[] bitmapSize;
    private SparseArray<WeakReference<Bitmap>> cacheMap = new SparseArray();

    public FrameAnimatorUtils(Builder builder) {
        this.builder = builder;
    }

    private Handler handler = new FrameHandler(this);

    static class FrameHandler extends Handler {

        WeakReference<FrameAnimatorUtils> utilsWeakReference;

        FrameHandler(FrameAnimatorUtils animatorUtils) {
            super(Looper.getMainLooper());
            utilsWeakReference = new WeakReference<>(animatorUtils);
        }

        @Override
        public void handleMessage(Message msg) {
            final FrameAnimatorUtils frameAnimatorUtils = utilsWeakReference.get();
            if (null != frameAnimatorUtils) {
                frameAnimatorUtils.setImageRegion();
            }
        }
    }

    /**
     * 获取资源图片的 宽 和 高
     *
     * @return
     */
    private int[] getBitmapWHByResId() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，
         * 但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(builder.view.getContext().getResources(), builder.resId, options);
        /**
         *options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth, options.outHeight};
    }

    public FrameAnimatorUtils display() {
        imageLoad();
        return this;
    }

    private void setImageRegion() {
        if (currentIndex > builder.frames_number) {
            currentIndex = 1;
        }

        if (cacheMap.get(currentIndex) != null && cacheMap.get(currentIndex).get() != null) {
            Bitmap bitmap = cacheMap.get(currentIndex).get();
            setIntoView(bitmap);
            return;
        }

        int currentRow = currentIndex % builder.w_frames == 0 ? currentIndex / builder.w_frames : currentIndex / builder.w_frames + 1;
        int rowIndex = currentIndex - (currentRow - 1) * builder.w_frames;
        int left = (rowIndex - 1) * bitmapSize[0] / builder.w_frames;
        int top = (currentRow - 1) * bitmapSize[1] / builder.h_frames;
        int right = left + bitmapSize[0] / builder.w_frames;
        int bottom = top + bitmapSize[1] / builder.h_frames;

        mRect.set(left, top, right, bottom);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = mDecoder.decodeRegion(mRect, opts);
        cacheMap.put(currentIndex, new WeakReference(bm));
        setIntoView(bm);
    }

    private void setIntoView(Bitmap bitmap) {
        if (builder.view instanceof ImageView) {
            ((ImageView) builder.view).setImageBitmap(bitmap);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                builder.view.setBackground(new BitmapDrawable(builder.view.getContext().getResources(), bitmap));
            } else {
                builder.view.setBackgroundDrawable(new BitmapDrawable(builder.view.getContext().getResources(), bitmap));
            }
        }

        if (builder.isAutoAnimation) {
            currentIndex++;
            handler.sendEmptyMessageDelayed(0, builder.frames_rate);
        }
    }

    public void toggle() {
        if (builder.isAutoAnimation) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    public void startAnimation() {
        builder.isAutoAnimation = true;
        currentIndex = 1;
        setImageRegion();
    }

    public void stopAnimation() {
        builder.isAutoAnimation = false;
        currentIndex = builder.loop_from;
        setImageRegion();
    }

    public void imageLoad() {

        ThreadPoolFactory.getInstance().getExecutorPool().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bitmapSize = getBitmapWHByResId();
                            InputStream is = builder.view.getContext().getResources().openRawResource(builder.resId);
                            mDecoder = BitmapRegionDecoder.newInstance(is, true);
                            handler.sendEmptyMessageDelayed(1, 60);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

    }

    public static class Builder {
        private int frames_number;//总共多少帧
        private int frames_rate;//帧率 毫秒
        private int h_frames;//高有几帧
        private int w_frames;//宽有几帧
        private int loop_from = 1;//从第几帧开始
        private int loop_to;//到第几帧结束
        private View view;//显示控件
        private int resId;//资源id
        private boolean isAutoAnimation;//是否自动播放动画

        public Builder setFrames_number(int frames_number) {
            this.frames_number = frames_number;
            return this;
        }

        public Builder setFrames_rate(int frames_rate) {
            this.frames_rate = frames_rate;
            return this;
        }

        public Builder setH_frames(int h_frames) {
            this.h_frames = h_frames;
            return this;
        }

        public Builder setW_frames(int w_frames) {
            this.w_frames = w_frames;
            return this;
        }

        public Builder setLoop_from(int loop_from) {
            this.loop_from = loop_from;
            return this;
        }

        public Builder setLoop_to(int loop_to) {
            this.loop_to = loop_to;
            return this;
        }

        public Builder setView(View view) {
            this.view = view;
            return this;
        }

        public Builder setResId(int resId) {
            this.resId = resId;
            return this;
        }

        public Builder setAutoAnimation(boolean autoAnimation) {
            isAutoAnimation = autoAnimation;
            return this;
        }

        public FrameAnimatorUtils build() {
            return new FrameAnimatorUtils(this);
        }
    }
}

