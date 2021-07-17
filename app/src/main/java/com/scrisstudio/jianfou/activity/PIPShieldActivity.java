package com.scrisstudio.jianfou.activity;

import android.app.PictureInPictureParams;
import android.os.Bundle;
import android.os.Handler;
import android.util.Rational;

import androidx.appcompat.app.AppCompatActivity;

public class PIPShieldActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PictureInPictureParams.Builder mPictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
		Rational aspectRatio = new Rational(1, 1);
		mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio);
		enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());

		PIPShieldActivity activity = this;
		new Handler().postDelayed(activity::finish, 200);
	}
}