package com.hcm.imagepuzzlemaker;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.hcm.imagepuzzlemaker.util.AdmobUtil;

public class StartActivity extends Activity implements OnClickListener {

	/****************************************************************************************************
	 * Constant member
	 ***************************************************************************************************/
	private final int 	CAMERA_REQUEST 			= 9999;
	private final int 	SELECT_SDCARD_REQUEST 	= 8888;
	private final int 	PIC_CROP 				= 2;
	private final int	IMAGE_SIZE				= 640;	//px

	// Global mutable variables
	private ImageView 	imgGameContent;
	private ImageButton btnGuide;
	private ImageButton btnSelectImage;
	private ImageButton btnCapture;
	private ImageButton btnStartGame;
	private Bitmap 		bmGame;
	private Button 		btnRepick;
	private Button 		btnContinue;

	private Spinner 	spNumber;
	private Button 		btnStart;
	private String[] 	number = { "2", "3", "4", "5" };

	private boolean isCaptured = false;
	private Uri picUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_start);

		AdmobUtil.initAdView(this);

		imgGameContent = (ImageView) findViewById(R.id.imgGameContent);

		spNumber 		= (Spinner) findViewById(R.id.spNumber);
		spNumber.setAdapter(new ArrayAdapter(getApplicationContext(), R.layout.item_number, number));

		btnGuide 		= (ImageButton) findViewById(R.id.btnGuide);
		btnGuide.setOnClickListener(this);
		
		btnSelectImage 	= (ImageButton) findViewById(R.id.btnSelectImage);
		btnSelectImage.setOnClickListener(this);

		btnCapture 		= (ImageButton) findViewById(R.id.btnCapture);
		btnCapture.setOnClickListener(this);

		btnRepick 		= (Button) findViewById(R.id.btnRepick);
		btnRepick.setOnClickListener(this);

		btnContinue 	= (Button) findViewById(R.id.btnContinue);
		btnContinue.setOnClickListener(this);

		btnStartGame 	= (ImageButton) findViewById(R.id.btnStartGame);
		btnStartGame.setOnClickListener(this);

		btnStart 		= (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!isCaptured) {

			Random r = new Random();
			int position = r.nextInt(49) + 1;

			int resId = getApplicationContext().getResources().getIdentifier("image_" + position, "drawable",
					getApplicationContext().getPackageName());

			bmGame = BitmapFactory.decodeResource(getApplicationContext().getResources(), resId);

			imgGameContent.setImageBitmap(bmGame);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAMERA_REQUEST) {
			if (resultCode == RESULT_OK) {
				picUri = data.getData();
				Bitmap temp = (Bitmap) data.getExtras().get("data");
				if (temp.getHeight() >= 640 && temp.getWidth() >= 640) {
					cropImage();
				} else {
					findViewById(R.id.llPopupRepickImage).setVisibility(View.VISIBLE);
				}
			}
		} else if (requestCode == SELECT_SDCARD_REQUEST) {
			if (resultCode == RESULT_OK) {
				picUri = data.getData();
				cropImage();
			}
		} else if (requestCode == PIC_CROP) {
			if (resultCode == RESULT_OK) {
				isCaptured = true;
				bmGame = (Bitmap) data.getExtras().get("data");
				imgGameContent.setImageBitmap(bmGame);
			}
		}
	}

	@Override
	public void onBackPressed() {
		LinearLayout llPopupGuide = (LinearLayout) findViewById(R.id.llPopupGuide);
		if(llPopupGuide.getVisibility() == View.VISIBLE) {
			llPopupGuide.setVisibility(View.GONE);
		} else {
			finish();
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == btnStartGame) {
			
			LinearLayout llPopupSetting = (LinearLayout) findViewById(R.id.llPopupSetting);
			llPopupSetting.setVisibility(View.VISIBLE);
			
		} else if (v == btnStart) {
			
			isCaptured = false;
			LinearLayout llPopupSetting = (LinearLayout) findViewById(R.id.llPopupSetting);
			llPopupSetting.setVisibility(View.GONE);
			ArrayList<Bitmap> chunkedImages = splitImage(Integer.parseInt(number[spNumber.getSelectedItemPosition()]), Integer.parseInt(number[spNumber.getSelectedItemPosition()]));

			// Start a new activity to show these chunks into a grid
			Intent intent = new Intent(StartActivity.this, GameActivity.class);
			intent.putParcelableArrayListExtra("image chunks", chunkedImages);
			startActivity(intent);
			
		} else if(v == btnGuide) {
			LinearLayout llPopupGuide = (LinearLayout) findViewById(R.id.llPopupGuide);
			if(llPopupGuide.getVisibility() == View.VISIBLE) {
				llPopupGuide.setVisibility(View.GONE);
			} else {
				llPopupGuide.setVisibility(View.VISIBLE);
			}
		} else if (v == btnCapture) {
			
			try {
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
			} catch (ActivityNotFoundException e) {
				String errorMessage = getResources().getString(R.string.Camera_Doesnt_Exist);
				Toast.makeText(StartActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
			}
			
		} else if (v == btnSelectImage) {
			
			Intent mediaChooser = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
			// comma-separated MIME types
			mediaChooser.setType("image/*");
			startActivityForResult(mediaChooser, SELECT_SDCARD_REQUEST);
			
		} else if (v == btnRepick) {
			
			findViewById(R.id.llPopupRepickImage).setVisibility(View.GONE);
			Intent mediaChooser = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
			mediaChooser.setType("image/*");
			startActivityForResult(mediaChooser, SELECT_SDCARD_REQUEST);
			
		} else if (v == btnContinue) {
			
			findViewById(R.id.llPopupRepickImage).setVisibility(View.GONE);
			cropImage();
			
		}
	}

	private void cropImage() {
		try {
			// call the standard crop action intent (the user device may not support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(picUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", IMAGE_SIZE);
			cropIntent.putExtra("outputY", IMAGE_SIZE);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PIC_CROP);
		} catch (ActivityNotFoundException anfe) {
			String errorMessage = getResources().getString(R.string.Camera_Doesnt_Exist);
			Toast.makeText(StartActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Splits the source image and show them all into a grid in a new activity
	 * 
	 * @param image
	 *            The source image to split
	 * @param chunkNumbers
	 *            The target number of small image chunks to be formed from the source image
	 */
	private ArrayList<Bitmap> splitImage(int column, int row) {

		// For height and width of the small image chunks
		int chunkHeight, chunkWidth;

		// To store all the small image chunks in bitmap format in this list
		ArrayList<Bitmap> chunkedImages = new ArrayList<Bitmap>(column * row);

		Bitmap scaledBitmap;
		
		// Scale bitmap to avoid OutofMemoryException
		if(bmGame.getWidth() >= IMAGE_SIZE) {
			scaledBitmap = Bitmap.createScaledBitmap(bmGame, bmGame.getWidth() / 4, bmGame.getHeight() / 4, true);
		} else if(bmGame.getWidth() >= IMAGE_SIZE/2){
			scaledBitmap = Bitmap.createScaledBitmap(bmGame, bmGame.getWidth() / 2, bmGame.getHeight() / 2, true);
		} else { 
			scaledBitmap = bmGame;
		}
		
		chunkHeight = scaledBitmap.getHeight() / row;
		chunkWidth = scaledBitmap.getWidth() / column;

		// xCoord and yCoord are the pixel positions of the image chunks
		int yCoord = 0;
		for (int x = 0; x < row; x++) {
			int xCoord = 0;
			for (int y = 0; y < column; y++) {
				chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
				xCoord += chunkWidth;
			}
			yCoord += chunkHeight;
		}

		return chunkedImages;
	}

}