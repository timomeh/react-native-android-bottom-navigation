package de.timomeh.bottomnavigation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.drawee.view.MultiDraweeHolder;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.views.toolbar.DrawableWithIntrinsicSize;

import javax.annotation.Nullable;

/**
 * Created by @timomeh on 19/02/2017.
 */

public class RNBottomNavigation extends BottomNavigationView {

    private final MultiDraweeHolder<GenericDraweeHierarchy> mActionsHolder =
            new MultiDraweeHolder<>();

    private abstract class IconControllerListener extends BaseControllerListener<ImageInfo> {

        private final DraweeHolder mHolder;
        private IconImageInfo mIconImageInfo;

        public IconControllerListener(DraweeHolder holder) {
            mHolder = holder;
        }

        public void setIconImageInfo(IconImageInfo iconImageInfo) {
            mIconImageInfo = iconImageInfo;
        }

        @Override
        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
            super.onFinalImageSet(id, imageInfo, animatable);

            final ImageInfo info = mIconImageInfo != null ? mIconImageInfo : imageInfo;
            setDrawable(new DrawableWithIntrinsicSize(mHolder.getTopLevelDrawable(), info));
        }

        protected abstract void setDrawable(Drawable d);
    }

    private class ActionIconControllerListener extends IconControllerListener {
        private final MenuItem mItem;

        ActionIconControllerListener(MenuItem item, DraweeHolder holder) {
            super(holder);
            mItem = item;
        }

        @Override
        protected void setDrawable(Drawable d) {
            mItem.setIcon(d);
        }
    }

    private static class IconImageInfo implements ImageInfo {
        private int mWidth;
        private int mHeight;

        public IconImageInfo(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        @Override
        public int getWidth() {
            return mWidth;
        }

        @Override
        public int getHeight() {
            return mHeight;
        }

        @Override
        public QualityInfo getQualityInfo() {
            return null;
        }
    }

    public RNBottomNavigation(Context context) {
        super(context);
    }

    private final Runnable mLayoutRunnable = new Runnable() {
        @Override
        public void run() {
            measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(mLayoutRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        detachDraweeHolders();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        detachDraweeHolders();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachDraweeHolders();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        attachDraweeHolders();
    }

    private void detachDraweeHolders() {
        mActionsHolder.onDetach();
    }

    private void attachDraweeHolders() {
        mActionsHolder.onAttach();
    }

    public void setActions(@Nullable ReadableArray actions) {
        Menu menu = getMenu();
        menu.clear();
        mActionsHolder.clear();
        if (actions != null) {
            for (int i = 0; i < actions.size(); i++) {
                ReadableMap action = actions.getMap(i);

                MenuItem item = menu.add(Menu.NONE, Menu.NONE, i, action.getString("title"));
                setMenuItemIcon(item, action.getMap("icon"));
                if (action.getBoolean("disabled")) {
                    item.setEnabled(false);
                }
            }
        }
    }

    private void setMenuItemIcon(final MenuItem item, ReadableMap iconSource) {
        DraweeHolder<GenericDraweeHierarchy> holder =
                DraweeHolder.create(createDraweeHierarchy(), getContext());
        ActionIconControllerListener controllerListener =
                new ActionIconControllerListener(item, holder);
        controllerListener.setIconImageInfo(getIconImageInfo(iconSource));

        setIconSource(iconSource, controllerListener, holder);

        mActionsHolder.add(holder);
    }

    private void setIconSource(ReadableMap source, IconControllerListener controllerListener,
                               DraweeHolder holder) {
        String uri = source != null ? source.getString("uri") : null;

        if (uri == null) {
            controllerListener.setIconImageInfo(null);
            controllerListener.setDrawable(null);
        } else if (uri.startsWith("http://") || uri.startsWith("https://") ||
                uri.startsWith("file://")) {
            controllerListener.setIconImageInfo(getIconImageInfo(source));
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(uri))
                    .setControllerListener(controllerListener)
                    .setOldController(holder.getController())
                    .build();
            holder.setController(controller);
            holder.getTopLevelDrawable().setVisible(true, true);
        } else {
            controllerListener.setDrawable(getDrawableByName(uri));
        }
    }

    private GenericDraweeHierarchy createDraweeHierarchy() {
        return new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setFadeDuration(0)
                .build();
    }

    private int getDrawableResourceByName(String name) {
        return getResources().getIdentifier(
                name,
                "drawable",
                getContext().getPackageName());
    }

    private Drawable getDrawableByName(String name) {
        int drawableResId = getDrawableResourceByName(name);
        if (drawableResId != 0) {
            return getResources().getDrawable(getDrawableResourceByName(name));
        } else {
            return null;
        }
    }

    private IconImageInfo getIconImageInfo(ReadableMap source) {
        if (source.hasKey("width") && source.hasKey("height")) {
            final int width = Math.round(PixelUtil.toPixelFromDIP(source.getInt("width")));
            final int height = Math.round(PixelUtil.toPixelFromDIP(source.getInt("height")));
            return new IconImageInfo(width, height);
        } else {
            return null;
        }
    }
}
