package com.achelm.musicplayer

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

class RatingManager {

    //I used this Method cuz The in-app review API is designed to show the review dialog to the user ONLY ONCE

    companion object {

        val PREF_HAS_REVIEWED = "hasReviewed"

        // Check if the user has already reviewed the app
        fun hasUserReviewed(context: Context): Boolean {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(PREF_HAS_REVIEWED, false)
        }

        // Save that the user has reviewed the app
        fun setUserReviewed(context: Context) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREF_HAS_REVIEWED, true)
            editor.apply()
        }

        // Show in-app review flow
        fun showInAppReview(context: Context, activity: Activity) {
            val reviewManager: ReviewManager = ReviewManagerFactory.create(activity)
            val request: Task<ReviewInfo> = reviewManager.requestReviewFlow()
            request.addOnCompleteListener(OnCompleteListener<ReviewInfo?> { task: Task<ReviewInfo?> ->
                try {
                    if (task.isSuccessful()) {
                        // We can get the ReviewInfo object
                        val reviewInfo: ReviewInfo = task.getResult()!!
                        val reviewFlow: Task<Void?> =
                            reviewManager.launchReviewFlow(activity, reviewInfo)
                        reviewFlow.addOnCompleteListener { task1: Task<Void?> ->
                            if (task1.isSuccessful) {
                            Toast.makeText(context, context.getResources().getString(R.string.ratingManager_Rating_is_completed), Toast.LENGTH_SHORT).show();
                                // Save that the user has reviewed the app
                                setUserReviewed(context)
                            }
                            else {
                                // There was some problem, log or handle the error code.
                                Toast.makeText(context, context.resources.getString(R.string.ratingManager_Review_failed_to_start), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else {
                        // There was some problem, log or handle the error code.
                        Toast.makeText(context, context.resources.getString(R.string.ratingManager_Review_failed_to_start), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Open Play Store page for your app
        fun openPlayStorePage(context: Context) {
            try {
                val uri = Uri.parse("market://details?id=" + context.packageName)
                val playStoreIntent = Intent(Intent.ACTION_VIEW, uri)
                playStoreIntent.setPackage("com.android.vending")
                context.startActivity(playStoreIntent)
            } catch (e: ActivityNotFoundException) {
                // If Play Store is not available, open the app in a browser
                val uri =
                    Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(browserIntent)
            }
        }

    }
}