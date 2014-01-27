package com.tanukiteam.camera.widget;

import com.tanukiteam.camera.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;


public class CancelButton extends Button {
	public CancelButton(Context context) {
		super(context);
    	this.setBackgroundResource(R.drawable.btn_cancel);
    	this.setTextAppearance(context, R.style.CancelButton);
	}
	
	public CancelButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    	this.setBackgroundResource(R.drawable.btn_cancel);
    	this.setTextAppearance(context, R.style.CancelButton);
    }

    public CancelButton(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	this.setBackgroundResource(R.drawable.btn_cancel);
    	this.setTextAppearance(context, R.style.CancelButton);
    }
}
