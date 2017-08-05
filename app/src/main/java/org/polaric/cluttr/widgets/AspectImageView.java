package org.polaric.cluttr.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AspectImageView extends ImageView {
    private float aspect=1f;

    public AspectImageView(Context context) {
        super(context);
    }

    public AspectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public float getAspect() {
        return aspect;
    }

    public void setAspect(float aspect) {
        this.aspect = aspect;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        this.setMeasuredDimension(width, (int)(width*aspect));
    }
}
