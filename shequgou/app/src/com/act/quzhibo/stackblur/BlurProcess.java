package com.act.quzhibo.stackblur;

import android.graphics.Bitmap;

interface BlurProcess {
     Bitmap blur(Bitmap original, float radius);
	 void onDestory();
}
