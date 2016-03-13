package originator.ailin.com.smartgraph.icon;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import originator.ailin.com.smartgraph.obj.PieObj;

/**
 * Created by Kim_Bai on 2/15/2015.
 */
public class IconPie extends IconAbs {
    /**
     * Constructor
     * @param pieObj
     * @param radius
     * @param bitmaps
     */
    public IconPie(PieObj pieObj, float radius, float[] data, Bitmap[] bitmaps) {
        this.pieObj = pieObj;
        this.radius = radius;
        this.bitmaps = bitmaps;
        this.data = data;
    }

    /**
     *
     * @param resources
     * @param canvas
     * @param paint
     */
    @Override
    public void drawIcon(Resources resources, Canvas canvas, Paint paint) {
        init(resources, paint);
        float startAngleInit = pieObj.startAngle;

        float total = 0;
        for(float d : data) {
            total += d;
        }

        for(int i = 0; i < data.length; i++) {
            float swipeAngle = data[i] * 360 / total;

            double radian = Math.PI * (2 * startAngleInit + swipeAngle) / 360;
            canvas.drawBitmap(bitmaps[i], pieObj.center.x + (radius + pieObj.bias[i]) * (float) Math.cos(radian) - bitmaps[i].getWidth() / 2, pieObj.center.y + (radius + pieObj.bias[i]) * (float) Math.sin(radian) - bitmaps[i].getHeight() / 2, paint);

            // swipeAngle++
            startAngleInit += swipeAngle;
        }
    }
}
