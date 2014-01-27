package com.tanukiteam.camera.widget;

import com.tanukiteam.camera.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;


public class ConfirmButton extends Button{
	public ConfirmButton(Context context) {
		super(context);

		setBackgroundResource(R.drawable.btn_confirm);
    	this.setTextAppearance(context, R.style.ConfirmButton);

	}

	public ConfirmButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.setBackgroundResource(R.drawable.btn_confirm);
    	this.setTextAppearance(context, R.style.ConfirmButton);

	}

	public ConfirmButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.setBackgroundResource(R.drawable.btn_confirm);
    	this.setTextAppearance(context, R.style.ConfirmButton);

	}
}
