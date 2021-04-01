package br.ucs.android.jogo_velha_selfies.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class ResizableButton extends androidx.appcompat.widget.AppCompatButton {

    public ResizableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

}
