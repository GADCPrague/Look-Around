package com.github.whereare;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 *
 * @author radim
 */
class FriendsView extends View { 
        public FriendsView(Context context) { 
                super(context); 
                // TODO Auto-generated constructor stub 
        } 
        @Override 
        protected void onDraw(Canvas canvas) { 
                // TODO Auto-generated method stub 
                Paint paint = new Paint(); 
                paint.setStyle(Paint.Style.FILL); 
                paint.setColor(Color.BLACK); 
                canvas.drawText("Test Text", 10, 10, paint); 
                super.onDraw(canvas); 
        } 
} 