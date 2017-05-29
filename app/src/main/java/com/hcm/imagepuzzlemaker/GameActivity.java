package com.hcm.imagepuzzlemaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.FacebookDialog;
import com.hcm.imagepuzzlemaker.db.DBImagePuzzle;
import com.hcm.imagepuzzlemaker.util.AdmobUtil;
import com.hcm.imagepuzzlemaker.util.Util;

//This activity will display the small image chunks into a grid view
public class GameActivity extends Activity {

	// Global mutable variables
	private int 			passedSenconds;
	private Timer 			timer;
	private TimerTask 		timerTask;

	private RelativeLayout 	rlBgGame;
	private LinearLayout 	lnOrder;
	private TextView 		txtTimer;

	private int 			widthScreen = 0;
	private int 			heightScreen = 0;

	private int 			_xDelta = 0;
	private int 			_yDelta = 0;

	// No of column and row
	private int 			column;

	private ArrayList<ChunkedImage> arChunkedImage = new ArrayList<ChunkedImage>();
	private ArrayList<PuzzleResult> arPuzzleResult = new ArrayList<PuzzleResult>();

	private ArrayList<Bitmap> 		arImageChunks = new ArrayList<Bitmap>();
	
	private UiLifecycleHelper 		uiHelper;
	private boolean 				canPresentShareDialogWithPhotos;
	private PendingAction 			pendingAction = PendingAction.NONE;
	private enum PendingAction {
							        NONE,
							        POST_PHOTO,
							        POST_STATUS_UPDATE
								}
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d("HelloFacebook", "Success!");
        }
    };
    
	// ====================================================================
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.layout_game);

		AdmobUtil.initAdView(this);
		
		rlBgGame = (RelativeLayout) findViewById(R.id.rlBgGame);
		rlBgGame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {

				widthScreen = rlBgGame.getWidth();
				heightScreen = rlBgGame.getHeight();
				
				initImages();

				// Ensure you call it only once :
				rlBgGame.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		
		
		txtTimer = (TextView) findViewById(R.id.txtTimer);

		// Get list image from intent
		arImageChunks = getIntent().getParcelableArrayListExtra("image chunks");
		column = (int) Math.sqrt(arImageChunks.size());

		initTableResult();

		// Show timer
		reScheduleTimer();
		
		// Success Dialog
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(bundle);
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
    }

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
                new AlertDialog.Builder(GameActivity.this)
                    .setTitle(R.string.cancelled)
                    .setMessage(R.string.permission_not_granted)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
    }
	 
	private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }
	
	@SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case POST_PHOTO:
                postPhoto();
                break;
            case POST_STATUS_UPDATE:
                break;
        }
    }
	
	 private void postPhoto() {
		 Bitmap image = takeScreenshot();
        if (canPresentShareDialogWithPhotos) {
            FacebookDialog shareDialog = createShareDialogBuilderForPhoto(image).build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else if (hasPublishPermission()) {
            Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
                @Override
                public void onCompleted(Response response) {
                    showPublishResult(getString(R.string.photo_post), response.getGraphObject(), response.getError());
                }
            });
            request.executeAsync();
        } else {
            pendingAction = PendingAction.POST_PHOTO;
        }
    }
	 
	private FacebookDialog.PhotoShareDialogBuilder createShareDialogBuilderForPhoto(Bitmap... photos) {
	        return new FacebookDialog.PhotoShareDialogBuilder(this).setApplicationName("Image Puzzle").addPhotos(Arrays.asList(photos));
	}
	
	private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
        String title = null;
        String alertMessage = null;
        if (error == null) {
            title = getString(R.string.success);
            String id = result.cast(GraphObjectWithId.class).getId();
            alertMessage = getString(R.string.successfully_posted_post, message, id);
        } else {
            title = getString(R.string.error);
            alertMessage = error.getErrorMessage();
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(alertMessage)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
	
	private interface GraphObjectWithId extends GraphObject {
        String getId();
    }

	public Bitmap takeScreenshot() {
	   View rootView = findViewById(android.R.id.content).getRootView();
	   rootView.setDrawingCacheEnabled(true);
	   return rootView.getDrawingCache();
	}

	private void initImages() {

		for(int i = 0 ; i < arImageChunks.size(); i++) {
			ImageView img = new ImageView(getApplicationContext());
			img.setImageBitmap(arImageChunks.get(i));
			img.setOnTouchListener(new MyOnTouchListener(i));
			rlBgGame.addView(img);

			arChunkedImage.add(new ChunkedImage(i, -1, -1, img));
		}
		
		Collections.shuffle(arChunkedImage);
		
		// Set bitmap for linearlayout around
		for (int i = 0; i < arChunkedImage.size(); i++) {
			
			RelativeLayout.LayoutParams imgLayoutParam;
			int imageSize = 0;
			
			if(widthScreen / 7 > (heightScreen - 2 * (heightScreen / 10)) / 11) { // Tab
				imageSize = (heightScreen - 2 * (heightScreen / 10)) / 11;
			} else {	// Phone
				imageSize = widthScreen / 7;
			}
			
			imgLayoutParam = new RelativeLayout.LayoutParams(imageSize, imageSize);
			
			int hor_margin = ((widthScreen - 6 * imageSize)) / 7;
			int ver_margin = (heightScreen - 2 * (heightScreen / 10)) / 11 / 10;
			
			if(i < 6) {
				imgLayoutParam.topMargin 	= heightScreen/10 + ver_margin;
				imgLayoutParam.leftMargin 	= hor_margin + i * imageSize + (i * hor_margin);
			} else if(i >= 6 && i < 14) {
				imgLayoutParam.topMargin 	= heightScreen/10 + ver_margin + (i-5) * imageSize + (i-5) * ver_margin;
				imgLayoutParam.leftMargin 	= hor_margin + 5 * imageSize + (5 * hor_margin);
			} else if(i >= 14 && i < 19) {
				imgLayoutParam.topMargin 	= heightScreen/10 + ver_margin + 9 * imageSize + 9 * ver_margin;
				imgLayoutParam.leftMargin 	= hor_margin + (19-i) * imageSize + (19-i) * hor_margin;
			} else {
				imgLayoutParam.topMargin 	= heightScreen/10 + ver_margin + (28-i) * imageSize + (28-i) * ver_margin;
				imgLayoutParam.leftMargin 	= hor_margin;
			}

			arChunkedImage.get(i).image.setLayoutParams(imgLayoutParam);
			arChunkedImage.get(i).originalX = imgLayoutParam.topMargin;
			arChunkedImage.get(i).originalY = imgLayoutParam.leftMargin;
			
		}
		
	}

	class MyOnTouchListener implements OnTouchListener {

		int index;
		
		public MyOnTouchListener(int index) {
	          this.index = index;
	     }
		
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			
			final int X = (int) event.getRawX();
			final int Y = (int) event.getRawY();
			
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
					_xDelta = X - lParams.leftMargin;
					_yDelta = Y - lParams.topMargin;
					break;
				case MotionEvent.ACTION_UP:
	
					for (int i = 0; i < arPuzzleResult.size(); i++) {
						int[] location = new int[2];
						arPuzzleResult.get(i).layout.getLocationOnScreen(location);
	
						if (Y > location[1]
								&& Y < location[1] + arPuzzleResult.get(i).layout.getWidth()
								&& X > location[0]
								&& X  < location[0] + arPuzzleResult.get(i).layout.getHeight()) {
	
							if(arPuzzleResult.get(i).curImage != -1) {
								
								for(int j = 0 ; j < arChunkedImage.size(); j++) {
									if(arChunkedImage.get(j).index == arPuzzleResult.get(i).curImage) {
										((RelativeLayout.LayoutParams)arChunkedImage.get(j).image.getLayoutParams()).topMargin = arChunkedImage.get(j).originalX;
										((RelativeLayout.LayoutParams)arChunkedImage.get(j).image.getLayoutParams()).leftMargin = arChunkedImage.get(j).originalY;
										arChunkedImage.get(j).image.setVisibility(View.VISIBLE);
									}
								}
								
								arPuzzleResult.get(i).curImage = -1;
								arPuzzleResult.get(i).layout.setBackgroundResource(R.drawable.img_bg);
							}
							
							arPuzzleResult.get(i).curImage = index;
							
							BitmapDrawable bitmap = new BitmapDrawable(arImageChunks.get(index));
							arPuzzleResult.get(i).layout.setBackgroundDrawable(bitmap);
	
							view.setVisibility(View.GONE);
							
							if(checkResult()) {
								
								insertDatabase(column, passedSenconds);
								showResult();
							}
						}
					}
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_MOVE:
					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
					layoutParams.leftMargin 	= X - _xDelta;
					layoutParams.topMargin 		= Y - _yDelta;
					view.setLayoutParams(layoutParams);
					break;
			}
			return true;
		}
	}

	private void initTableResult() {
		lnOrder = (LinearLayout) findViewById(R.id.lnOrdered);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels / 3 * 2, 
																	 getResources().getDisplayMetrics().widthPixels / 3 * 2);
		lnOrder.setLayoutParams(lp);

		// Init game screen follow no of image list
		lnOrder.setWeightSum(column);

		int curPos = 1;
		
		for (int i = 0; i < column; i++) {
			LinearLayout layout_Row = new LinearLayout(getApplicationContext());
			LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
			layoutParam.weight = 1f;
			layout_Row.setOrientation(LinearLayout.HORIZONTAL);
			layout_Row.setWeightSum(column);
			layout_Row.setLayoutParams(layoutParam);
			
			for (int j = 0; j < column; j++) {
				final LinearLayout layout_Column = new LinearLayout(getApplicationContext());
				LinearLayout.LayoutParams lp_Column = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
				lp_Column.weight = 1f;
				layout_Column.setLayoutParams(lp_Column);

				layout_Column.setBackgroundResource(R.drawable.img_bg);

				TextView txtPosition = new TextView(getApplicationContext());
				LinearLayout.LayoutParams lp_Position = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
																						LinearLayout.LayoutParams.MATCH_PARENT, 
																						Gravity.CENTER);
				lp_Position.setMargins(5, 0, 0, 0);
				txtPosition.setLayoutParams(lp_Position);
				txtPosition.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
				txtPosition.setText(curPos++ + "");
				txtPosition.setTextColor(Color.BLACK);

				layout_Column.addView(txtPosition);

				layout_Row.addView(layout_Column);

				arPuzzleResult.add(new PuzzleResult(-1, layout_Column));
			}

			lnOrder.addView(layout_Row);
		}
		
		for(int i = 0 ; i < arPuzzleResult.size(); i++) {
			final int pos = i;
			
			arPuzzleResult.get(i).layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					for(int i = 0 ; i < arChunkedImage.size(); i++) {
						if(arChunkedImage.get(i).index == arPuzzleResult.get(pos).curImage) {
							((RelativeLayout.LayoutParams)arChunkedImage.get(i).image.getLayoutParams()).topMargin = arChunkedImage.get(i).originalX;
							((RelativeLayout.LayoutParams)arChunkedImage.get(i).image.getLayoutParams()).leftMargin = arChunkedImage.get(i).originalY;
							arChunkedImage.get(i).image.setVisibility(View.VISIBLE);
						}
					}
					
					arPuzzleResult.get(pos).curImage = -1;
					arPuzzleResult.get(pos).layout.setBackgroundResource(R.drawable.img_bg);
					
				}
			});
		}
	}

	private boolean checkResult() {
		for(int i = 0 ; i < arPuzzleResult.size(); i++) {
			if(arPuzzleResult.get(i).curImage != i) {
				return false;
			}
		}
		
		return true;
			
	}
	
	// Scheduler
	public void reScheduleTimer() {
		timer = new Timer();
		timerTask = new myTimerTask();
		timer.schedule(timerTask, 0, 1000);
	}

	private class myTimerTask extends TimerTask {
		@Override
		public void run() {
			passedSenconds++;
			updateLabel.sendEmptyMessage(0);
		}
	}

	private Handler updateLabel = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			int seconds = passedSenconds % 60;
			int minutes = (passedSenconds / 60) % 60;
			txtTimer.setText(String.format("%02d : %02d", minutes, seconds));
		}
	};

	private class ChunkedImage {
		private int index;
		private int originalX;
		private int originalY;
		private ImageView image;

		public ChunkedImage(int index, int originalX, int originalY, ImageView image) {
			super();
			this.index = index;
			this.originalX = originalX;
			this.originalY = originalY;
			this.image = image;
		}

	}

	private class PuzzleResult {
		private int curImage = -1;
		private LinearLayout layout;

		public PuzzleResult(int curImage, LinearLayout layout) {
			super();
			this.curImage = curImage;
			this.layout = layout;
		}

	}
	
	private void showResult() {
		findViewById(R.id.llResult).setVisibility(View.VISIBLE);
		
		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		String strResultListTitle = getResources().getString(R.string.Success);
		Util.changeTextViewColour(getApplicationContext(), txtTitle, strResultListTitle);
		
		TextView txtResult	= (TextView) findViewById(R.id.txtResult);
		txtResult.setText(column + "x" + column + "\n" + String.format("%02d : %02d", ((passedSenconds / 60) % 60), (passedSenconds % 60)));
		
		ImageView ivGame = (ImageView)findViewById(R.id.ivGame);
		ivGame.setImageBitmap(combineBitmap());
		
        // Back to first screen
		ImageButton btnRefresh = (ImageButton)findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		ImageButton btnResultList = (ImageButton) findViewById(R.id.btnResultList);
		btnResultList.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(GameActivity.this, ResultActivity.class);
				startActivity(intent);
			}
		});
		
		ImageButton btnFacebook = (ImageButton) findViewById(R.id.btnFacebook);
		btnFacebook.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				postPhoto();
				//performPublish(PendingAction.POST_PHOTO, canPresentShareDialogWithPhotos);
			}
		});
		
		canPresentShareDialogWithPhotos = FacebookDialog.canPresentShareDialog(this,FacebookDialog.ShareDialogFeature.PHOTOS);

	}
	
	private void insertDatabase(int column, int passedSenconds) {
		DBImagePuzzle db = new DBImagePuzzle(getApplicationContext());
		db.INSERT(column, passedSenconds);
	}
	
	private Bitmap combineBitmap() {
		
		int imgSize = arImageChunks.get(0).getWidth();
		
		Bitmap fullImage = Bitmap.createBitmap(column * arImageChunks.get(0).getWidth(), column * arImageChunks.get(0).getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(fullImage);
		for(int i = 0 ; i < arImageChunks.size(); i++) {
			canvas.drawBitmap(arImageChunks.get(i), (i % column) * imgSize, i / column * imgSize, null);
		}
		
		return fullImage;
	}
}
