package originator.ailin.com.smartgraph.icon;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import originator.ailin.com.smartgraph.R;
import originator.ailin.com.smartgraph.obj.PieObj;

/**
 * Created by Kim_Bai on 2/15/2015.
 */
public abstract class IconAbs {
    protected PieObj pieObj;
    protected float radius;
    protected float[] radiusDealArray;
    protected float[] data;
    protected Bitmap[] bitmaps;
    protected float pieRoseLegendValueMargin;

    /**
     * Init Params
     */
    protected void init(Resources resources, Paint paint) {
        paint.setTextSize(34);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        pieRoseLegendValueMargin = resources.getDimension(R.dimen.pie_rose_legend_value_margin);
    }

    /**
     *
     * @param resources
     * @param canvas
     * @param paint
     */
    public abstract void drawIcon(Resources resources, Canvas canvas, Paint paint);

}
