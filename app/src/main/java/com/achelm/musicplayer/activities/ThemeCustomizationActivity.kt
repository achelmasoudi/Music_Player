package com.achelm.musicplayer.activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.R

class ThemeCustomizationActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var applyBtn: CardView

    private lateinit var defaultThemeBtn: View
    private lateinit var _1ThemeBtn: View
    private lateinit var _2ThemeBtn: View
    private lateinit var _3ThemeBtn: View
    private lateinit var _4ThemeBtn: View
    private lateinit var _5ThemeBtn: View
    private lateinit var _6ThemeBtn: View
    private lateinit var _7ThemeBtn: View
    private lateinit var _8ThemeBtn: View

    // Checked icons
    private lateinit var checkedDefaultThemeIcon: View
    private lateinit var checked_1ThemeIcon: View
    private lateinit var checked_2ThemeIcon: View
    private lateinit var checked_3ThemeIcon: View
    private lateinit var checked_4ThemeIcon: View
    private lateinit var checked_5ThemeIcon: View
    private lateinit var checked_6ThemeIcon: View
    private lateinit var checked_7ThemeIcon: View
    private lateinit var checked_8ThemeIcon: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Language
        LanguageManager.loadLocale(this)

        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        MainActivity.themeIndex = themeEditor.getInt("themeIndex", 0)

        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        setContentView(R.layout.activity_theme_customization)

        // Initialize variables
        toolbar = findViewById(R.id.themeCustomizationActivity_toolBarId)
        applyBtn = findViewById(R.id.themeCustomizationActivity_applyBtnId)

        defaultThemeBtn = findViewById(R.id.themeCustomizationActivity_defaultThemeBtnId)
        _1ThemeBtn = findViewById(R.id.themeCustomizationActivity_theme_1_BtnId)
        _2ThemeBtn = findViewById(R.id.themeCustomizationActivity_theme_2_BtnId)
        _3ThemeBtn = findViewById(R.id.themeCustomizationActivity_theme_3_BtnId)
        _4ThemeBtn = findViewById(R.id.themeCustomizationActivity_theme_4_BtnId)
        _5ThemeBtn = findViewById(R.id.themeCustomizationActivity_theme_5_BtnId)
        _6ThemeBtn = findViewById(R.id.themeCustomizationActivity_theme_6_BtnId)
        _7ThemeBtn = findViewById(R.id.themeCustomizationActivity_theme_7_BtnId)
        _8ThemeBtn = findViewById(R.id.themeCustomizationActivity_theme_8_BtnId)

        checkedDefaultThemeIcon = findViewById(R.id.themeCustomizationActivity_defaultTheme_checkedIconId)
        checked_1ThemeIcon = findViewById(R.id.themeCustomizationActivity_theme_1_checkedIconId)
        checked_2ThemeIcon = findViewById(R.id.themeCustomizationActivity_theme_2_checkedIconId)
        checked_3ThemeIcon = findViewById(R.id.themeCustomizationActivity_theme_3_checkedIconId)
        checked_4ThemeIcon = findViewById(R.id.themeCustomizationActivity_theme_4_checkedIconId)
        checked_5ThemeIcon = findViewById(R.id.themeCustomizationActivity_theme_5_checkedIconId)
        checked_6ThemeIcon = findViewById(R.id.themeCustomizationActivity_theme_6_checkedIconId)
        checked_7ThemeIcon = findViewById(R.id.themeCustomizationActivity_theme_7_checkedIconId)
        checked_8ThemeIcon = findViewById(R.id.themeCustomizationActivity_theme_8_checkedIconId)

        // Set arrow back button to Toolbar
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        when ( MainActivity.themeIndex ) {
            0 -> checkedDefaultThemeIcon.visibility = View.VISIBLE
            1 -> checked_1ThemeIcon.visibility = View.VISIBLE
            2 -> checked_2ThemeIcon.visibility = View.VISIBLE
            3 -> checked_3ThemeIcon.visibility = View.VISIBLE
            4 -> checked_4ThemeIcon.visibility = View.VISIBLE
            5 -> checked_5ThemeIcon.visibility = View.VISIBLE
            6 -> checked_6ThemeIcon.visibility = View.VISIBLE
            7 -> checked_7ThemeIcon.visibility = View.VISIBLE
            8 -> checked_8ThemeIcon.visibility = View.VISIBLE
        }

        defaultThemeBtn.setOnClickListener {
            saveTheme(0)
        }

        _1ThemeBtn.setOnClickListener {
            saveTheme(1)
        }


        _2ThemeBtn.setOnClickListener {
            saveTheme(2)
        }

        _3ThemeBtn.setOnClickListener {
            saveTheme(3)
        }

        _4ThemeBtn.setOnClickListener {
            saveTheme(4)
        }

        _5ThemeBtn.setOnClickListener {
            saveTheme(5)
        }

        _6ThemeBtn.setOnClickListener {
            saveTheme(6)
        }

        _7ThemeBtn.setOnClickListener {
            saveTheme(7)
        }

        _8ThemeBtn.setOnClickListener {
            saveTheme(8)
        }

        // Set current theme
        updateTheme()
    }

    private fun saveTheme(index: Int) {

        // Update visibility of checked icons immediately
        checkedDefaultThemeIcon.visibility = if (index == 0) View.VISIBLE else View.GONE
        checked_1ThemeIcon.visibility = if (index == 1) View.VISIBLE else View.GONE
        checked_2ThemeIcon.visibility = if (index == 2) View.VISIBLE else View.GONE
        checked_3ThemeIcon.visibility = if (index == 3) View.VISIBLE else View.GONE
        checked_4ThemeIcon.visibility = if (index == 4) View.VISIBLE else View.GONE
        checked_5ThemeIcon.visibility = if (index == 5) View.VISIBLE else View.GONE
        checked_6ThemeIcon.visibility = if (index == 6) View.VISIBLE else View.GONE
        checked_7ThemeIcon.visibility = if (index == 7) View.VISIBLE else View.GONE
        checked_8ThemeIcon.visibility = if (index == 8) View.VISIBLE else View.GONE

        applyBtn.setOnClickListener {
            if(MainActivity.themeIndex != index){
                val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
                editor.putInt("themeIndex", index)
                editor.apply()
            }
            startAnimationForTheme()
        }
    }

    override fun onResume() {
        super.onResume()

        updateTheme()
    }

    private fun startAnimationForTheme() {
        // Translation animation
        val translationYAnimator = ObjectAnimator.ofFloat(window.decorView, "translationY", 0f, -100f)

        // Scale animation
        val scaleXAnimator = ObjectAnimator.ofFloat(window.decorView, "scaleX", 1f, 0.8f)
        val scaleYAnimator = ObjectAnimator.ofFloat(window.decorView, "scaleY", 1f, 0.8f)

        // Fade-out animation
        val fadeOutAnimator = ObjectAnimator.ofFloat(window.decorView, "alpha", 1f, 0f)

        // Set animation properties
        val duration = 500L
        translationYAnimator.duration = duration
        scaleXAnimator.duration = duration
        scaleYAnimator.duration = duration
        fadeOutAnimator.duration = duration

        // Set interpolators
        val interpolator = AccelerateInterpolator()
        translationYAnimator.interpolator = interpolator
        scaleXAnimator.interpolator = interpolator
        scaleYAnimator.interpolator = interpolator
        fadeOutAnimator.interpolator = interpolator

        // Animator set for combining animations
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translationYAnimator, scaleXAnimator, scaleYAnimator, fadeOutAnimator)

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                // Apply theme change here
                updateTheme()

                // Call recreate() to apply theme change
                recreate()
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        animatorSet.start()
    }

    private fun updateTheme() {
        // Set current theme
        window.statusBarColor = ContextCompat.getColor( this ,  MainActivity.currentTheme[MainActivity.themeIndex] )
        toolbar.setBackgroundColor( ContextCompat.getColor(this , MainActivity.currentTheme[MainActivity.themeIndex]) )
        applyBtn.setCardBackgroundColor( ContextCompat.getColor(this , MainActivity.currentTheme[MainActivity.themeIndex]) )
    }

}