package com.asus.wellness.ui.InspireAsus;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 显示跑马灯效果
 *
 * @author: noah zhang
 * @date: 2014-12-23
 * @remark
 */
public class AlwaysMarqueeTextView extends TextView {

    /**
     * 默认聚焦
     */
    private boolean focused=true;

    /**
     * constructor
     *
     * @param context Context
     */
    public AlwaysMarqueeTextView(Context context) {
        super(context);
        setMarqueeRepeatLimit(-1);
    }

    /**
     * constructor
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * constructor
     *
     * @param context  Context
     * @param attrs    AttributeSet
     * @param defStyle int
     */
    public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused=focused;
        
        if(this.focused)
        {
        	setEllipsize(TextUtils.TruncateAt.MARQUEE);
        	setMarqueeRepeatLimit(-1);
        }
        else
        {
        	setEllipsize(TextUtils.TruncateAt.END);
        }
    }
}