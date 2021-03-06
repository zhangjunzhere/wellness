package originator.ailin.com.smartgraph.chart.doughnutchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import originator.ailin.com.smartgraph.R;
import originator.ailin.com.smartgraph.chart.BaseChart;
import originator.ailin.com.smartgraph.icon.IconPie;
import originator.ailin.com.smartgraph.label.Label;
import originator.ailin.com.smartgraph.legend.SimpleDoughnut;
import originator.ailin.com.smartgraph.title.Title;

public class SimpleDoughnutChart extends BaseChart {
    /**
     * Constructor 1
     * @param context
     */
    public SimpleDoughnutChart(Context context) {
        super(context);
    }

    /**
     * Constructor 2
     * @param context
     * @param attrs
     */
    public SimpleDoughnutChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void drawBackground(Canvas canvas) {
        Log.d("kim", "onDraw");

        if(data != null) {

            // Draw Legend Value
            /*value = new ValuePie(pieObj, (pieObj.radiusOuter + pieObj.radiusInner) / 2, data);
            showValue(getResources(), canvas, paint);*/

            // Draw Icon
            if(bitmaps != null) {
                icon = new IconPie(pieObj, (pieObj.radiusOuter + pieObj.radiusInner) / 2, data, bitmaps);
                showIcon(getResources(), canvas, paint);
            }

            // Draw Title
            left = pieObj.center.x - pieObj.radiusOuter;
            bottom = pieObj.center.y + pieObj.radiusOuter - getResources().getDimension(R.dimen.pie_title_margin);
            float maxWidth = pieObj.radiusOuter * 2;
            float maxHeight = pieObj.radiusOuter * 2;

            if(titleText != null) {
                title = new Title(left, bottom, titleText, titleSize, titleColor, maxWidth, maxHeight);
                showTitle(canvas, paint);
            }

            // Draw label
            if(labelsText != null) {
                label = new Label(left, bottom, data.length, labelsText, labelsTextColor, colors,  maxWidth, maxHeight);
                showLabel(getResources(), canvas, paint);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Thread drawThread = new Thread(new DrawRunnable(holder));
        drawThread.start();
    }

    class DrawRunnable implements Runnable {
        SurfaceHolder holder;

        public DrawRunnable(SurfaceHolder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            if(holder == null)
            {
                return;
            }
            synchronized (this) {
                for(int i = 1; i <= data.length; i++) {
                    Canvas canvas = holder.lockCanvas();
                    if(canvas ==null)
                    {
                        return;
                    }
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    // Draw Legend
                    legend = new SimpleDoughnut(pieObj, data, colors, i);
                    showLegend(canvas, paint);

                    // Draw Chart Background
                    drawBackground(canvas);

                    holder.unlockCanvasAndPost(canvas);

                    try {
                        wait(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
