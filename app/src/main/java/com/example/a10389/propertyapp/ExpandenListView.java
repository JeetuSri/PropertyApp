package com.example.a10389.propertyapp;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.*;

/**
 * Created by 10389 on 12/11/2017.
 */

public class ExpandenListView extends android.widget.ListView {

    private ViewGroup.LayoutParams params;
    private int oldCount=0;
    public ExpandenListView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(getCount() != oldCount){
            int height=getChildAt(0).getHeight();
            oldCount=getCount();
            params=getLayoutParams();
            params.height=getCount()*height;
            setLayoutParams(params);
        }
        super.onDraw(canvas);
    }
}
