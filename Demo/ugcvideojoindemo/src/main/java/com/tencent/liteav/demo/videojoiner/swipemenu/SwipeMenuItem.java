package com.tencent.liteav.demo.videojoiner.swipemenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class SwipeMenuItem {

    private Context mContext;
    private Drawable background;
    private Drawable icon;
    private String title;
    private ColorStateList titleColor;
    private int titleSize;
    private Typeface textTypeface;
    private int textAppearance;
    private int width = -2;
    private int height = -2;
    private int weight = 0;

    public SwipeMenuItem(Context context) {
        mContext = context;
    }

    public SwipeMenuItem setBackgroundDrawable(Drawable background) {
        this.background = background;
        return this;
    }

    public SwipeMenuItem setBackgroundDrawable(int resId) {
        this.background = ResCompat.getDrawable(mContext, resId);
        return this;
    }

    public SwipeMenuItem setBackgroundColor(int color) {
        this.background = new ColorDrawable(color);
        return this;
    }

    public Drawable getBackground() {
        return background;
    }

    public SwipeMenuItem setText(String title) {
        this.title = title;
        return this;
    }

    public SwipeMenuItem setImage(Drawable icon) {
        this.icon = icon;
        return this;
    }

    public SwipeMenuItem setImage(int resId) {
        return setImage(ResCompat.getDrawable(mContext, resId));
    }

    public Drawable getImage() {
        return icon;
    }

    public SwipeMenuItem setText(int resId) {
        setText(mContext.getString(resId));
        return this;
    }

    public SwipeMenuItem setTextColor(int titleColor) {
        this.titleColor = ColorStateList.valueOf(titleColor);
        return this;
    }

    public ColorStateList getTitleColor() {
        return titleColor;
    }

    public SwipeMenuItem setTextSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public int getTextSize() {
        return titleSize;
    }

    public String getText() {
        return title;
    }

    public SwipeMenuItem setTextAppearance(int textAppearance) {
        this.textAppearance = textAppearance;
        return this;
    }

    public int getTextAppearance() {
        return textAppearance;
    }

    public SwipeMenuItem setTextTypeface(Typeface textTypeface) {
        this.textTypeface = textTypeface;
        return this;
    }

    public Typeface getTextTypeface() {
        return textTypeface;
    }

    public int getWidth() {
        return width;
    }

    public SwipeMenuItem setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public SwipeMenuItem setHeight(int height) {
        this.height = height;
        return this;
    }

    public SwipeMenuItem setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public int getWeight() {
        return weight;
    }
}
