package com.hcm.imagepuzzlemaker.util;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class Util {

	public static void changeTextViewColour(Context context, TextView txtText, String text) {
		
		SpannableString spannableString = new SpannableString(text);
		for (int i = 0; i < text.length(); i++) {
			switch (i % 5) {
			case 0:
				spannableString.setSpan(new ForegroundColorSpan(Color.RED), i, i+1, 0);
				break;
			case 1:
				spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), i, i+1, 0);
				break;
			case 2:
				spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW), i, i+1, 0);
				break;
			case 3:
				spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), i, i+1, 0);
				break;
			case 4:
				spannableString.setSpan(new ForegroundColorSpan(Color.CYAN), i, i+1, 0);
				break;
			default:
				break;
			}

		}
		txtText.setText(spannableString, BufferType.SPANNABLE);
	}
}
