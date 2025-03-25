package com.achelm.offmusicplayer.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.achelm.offmusicplayer.LanguageManager
import com.achelm.offmusicplayer.R
import com.achelm.offmusicplayer.RatingManager.Companion.hasUserReviewed
import com.achelm.offmusicplayer.RatingManager.Companion.openPlayStorePage
import com.achelm.offmusicplayer.RatingManager.Companion.showInAppReview
import com.google.android.material.bottomsheet.BottomSheetDialog

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar

    private lateinit var appLanguageBtn: CardView
    private lateinit var rateAppBtn: CardView
    private lateinit var recommendAppBtn: CardView
    private lateinit var suggestionsBtn: CardView
    private lateinit var reportIssueBtn: CardView
    private lateinit var buymeCoffeeBtn: CardView
    private lateinit var privacyPolicyBtn: CardView
    private lateinit var customizeThemeBtn: CardView
    private lateinit var developerTextView: TextView

    companion object {
        private const val recipientEmail = "elmas3oudi9@gmail.com"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Language
        LanguageManager.loadLocale(this)

        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        setContentView(R.layout.activity_settings)

        // Initialize variables
        toolbar = findViewById(R.id.settingsActivity_toolBarId)
        appLanguageBtn = findViewById(R.id.settingsActivity_appLanguageBtnId)
        customizeThemeBtn = findViewById(R.id.settingsActivity_customizeThemeBtnId)
        rateAppBtn = findViewById(R.id.settingsActivity_rateAppBtnId)
        recommendAppBtn = findViewById(R.id.settingsActivity_recommendAppBtnId)
        suggestionsBtn = findViewById(R.id.settingsActivity_suggestionsBtnId)
        reportIssueBtn = findViewById(R.id.settingsActivity_reportIssueBtnId)
        buymeCoffeeBtn = findViewById(R.id.settingsActivity_buymeCoffeeBtnId)
        developerTextView = findViewById(R.id.settingsActivity_developerId)
        privacyPolicyBtn = findViewById(R.id.settingsActivity_privacyPolicyBtnId)

        // Set arrow back button to Toolbar
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        customizeThemeBtn.setOnClickListener {
            val intent = Intent(this , ThemeCustomizationActivity::class.java)
            startActivity(intent)
        }

        appLanguageProcess()

        rateOurAppProcess()

        recommendOurAppProcess()

        suggestionsForOurAppProcess()

        reportIssueWithOurAppFunc()

        buymeCoffeeProcess()

        privacyPolicyProcess()

        // Set current theme
        updateTheme()
    }

    private fun rateOurAppProcess() {
        rateAppBtn.setOnClickListener {
            // Check if the user has already reviewed the app
            if (hasUserReviewed(this)) {
                // Open the Play Store page for your app
                openPlayStorePage(this)
            } else {
                // User has not reviewed, show in-app review
                showInAppReview(this, this)
            }
        }
    }

    private fun recommendOurAppProcess() {
        recommendAppBtn.setOnClickListener {
            val appLink = "https://play.google.com/store/apps/details?id=" + packageName
            val recommendationMessage: String =  resources.getString(R.string.settingsActivity_recommendationMessage)+ "\n\n" + appLink

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_TEXT, recommendationMessage)
            startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.settingsActivity_Share_Music_Player_via)))
        }
    }

    private fun suggestionsForOurAppProcess() {
        suggestionsBtn.setOnClickListener {
            val emailSubject = resources.getString(R.string.settingsActivity_emailSubject_Suggestions)
            val emailMessage = resources.getString(R.string.settingsActivity_emailMessage_Suggestions)

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.setPackage("com.google.android.gm")
            // Add the email
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            // Add the email subject
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            // Add the email body text
            intent.putExtra(Intent.EXTRA_TEXT, emailMessage)

            try {
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                // Handle the case where no email app is installed on the device.
            }
        }
    }

    private fun reportIssueWithOurAppFunc() {
        reportIssueBtn.setOnClickListener {
            val emailSubject =  resources.getString(R.string.settingsActivity_emailSubject_ReportingIssue)
            val emailMessage =  resources.getString(R.string.settingsActivity_emailMessage_ReportingIssue)

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.setPackage("com.google.android.gm")
            // Add the email
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            // Add the email subject
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            // Add the email body text
            intent.putExtra(Intent.EXTRA_TEXT, emailMessage)

            try {
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                // Handle the case where no email app is installed on the device.
            }
        }
    }

    private fun buymeCoffeeProcess() {
        buymeCoffeeBtn.setOnClickListener {
            val buyMeCoffeeUrl = "https://buymeacoffee.com/achelmasoudi1"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(buyMeCoffeeUrl))
            startActivity(intent)
        }
    }

    private fun privacyPolicyProcess() {
        privacyPolicyBtn.setOnClickListener {
            val privacyPolicyUrl = "https://sites.google.com/view/musicplayer-privacyplayer/home"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(privacyPolicyUrl)
            startActivity(intent)
        }
    }

    private fun appLanguageProcess() {
        appLanguageBtn.setOnClickListener {
            val bottomSheetView: View = LayoutInflater.from(this).inflate( R.layout.bottom_sheet_layout_of_languages ,
                findViewById(R.id.bottomSheetLayoutOfLanguages_container) )

            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()

            val englishBtn: RadioButton = bottomSheetView.findViewById(R.id.bottomSheetLayoutOfLanguages_EnglishBtn)
            val turkishBtn: RadioButton = bottomSheetView.findViewById(R.id.bottomSheetLayoutOfLanguages_TurkishBtn)
            val frenchBtn: RadioButton = bottomSheetView.findViewById(R.id.bottomSheetLayoutOfLanguages_FrenchBtn)
            val germanBtn: RadioButton = bottomSheetView.findViewById(R.id.bottomSheetLayoutOfLanguages_GermanBtn)
            val russianBtn: RadioButton = bottomSheetView.findViewById(R.id.bottomSheetLayoutOfLanguages_RussianBtn)
            val spanishBtn: RadioButton = bottomSheetView.findViewById(R.id.bottomSheetLayoutOfLanguages_SpanishBtn)

            // Set current language selection
            when (LanguageManager.loadSelectedLanguage(this)) {
                "en" -> englishBtn.isChecked = true
                "tr" -> turkishBtn.isChecked = true
                "fr" -> frenchBtn.isChecked = true
                "de" -> germanBtn.isChecked = true
                "ru" -> russianBtn.isChecked = true
                "es" -> spanishBtn.isChecked = true
            }

            englishBtn.setOnClickListener {
                // Save the selected language to SharedPreferences and change app language
                saveAndChangeLanguage("en")
            }

            turkishBtn.setOnClickListener {
                saveAndChangeLanguage("tr")
            }

            frenchBtn.setOnClickListener {
                saveAndChangeLanguage("fr")
            }

            germanBtn.setOnClickListener {
                saveAndChangeLanguage("de")
            }

            russianBtn.setOnClickListener {
                saveAndChangeLanguage("ru")
            }

            spanishBtn.setOnClickListener {
                saveAndChangeLanguage("es")
            }
        }
    }

    private fun saveAndChangeLanguage(language: String) {
        LanguageManager.setLocaleLanguage(this, language)

        // Restart the app to apply the new language
        val intent = Intent(this, SplashScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        // Set Language
        LanguageManager.loadLocale(this)

        updateTheme()
    }

    private fun updateTheme() {
        // Set current theme
        window.statusBarColor = ContextCompat.getColor( this , MainActivity.currentTheme[MainActivity.themeIndex] )
        toolbar.setBackgroundColor( ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex]) )
        developerTextView.setTextColor( ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex]) )
    }
}