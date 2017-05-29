package com.hcm.imagepuzzlemaker.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.provider.Settings;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.hcm.imagepuzzlemaker.R;

public class AdmobUtil {

	private static AdView 						bottomAdView		= null;
	private static AdView 						topAdView			= null;
	private static InterstitialAd 				interstitial;
	
    public static void initAdView(Activity activity) {
		ConsoleLogger.logEnterFunction();
		// Prepare the Interstitial Ad
		interstitial = new InterstitialAd(activity);
		// Insert the Ad Unit ID
		interstitial.setAdUnitId(activity.getResources().getString(R.string.Admob_Key_Interstitial));
 
		//Locate the Banner Ad in activity_main.xml
		bottomAdView = (AdView) activity.findViewById(R.id.BottomAdView);
		topAdView = (AdView) activity.findViewById(R.id.TopAdView);
 
		String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase();
        
		// Request for Ads
		AdRequest adRequest = new AdRequest.Builder()
 
		// Add a test device to show Test Ads
		 .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		 .addTestDevice(deviceId).build();

		// Load ads into Banner Ads
		bottomAdView.loadAd(adRequest);
		topAdView.loadAd(adRequest);
 
		// Load ads into Interstitial Ads
		interstitial.loadAd(adRequest);
 
		// Prepare an Interstitial Ad Listener
		interstitial.setAdListener(new AdListener() {
			public void onAdLoaded() {
				// Call displayInterstitial() function
				displayInterstitial();
			}
		});
		ConsoleLogger.logLeaveFunction();
	}
	
	private static void displayInterstitial() {
		ConsoleLogger.logEnterFunction();
		// If Ads are loaded, show Interstitial else show nothing.
		if (interstitial.isLoaded()) {
			interstitial.show();
		}
		ConsoleLogger.logLeaveFunction();
	}
	
	private static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
	
}
