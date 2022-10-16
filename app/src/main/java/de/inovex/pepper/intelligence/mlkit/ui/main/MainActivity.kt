package de.inovex.pepper.intelligence.mlkit.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy
import dagger.hilt.android.AndroidEntryPoint
import de.inovex.pepper.intelligence.mlkit.R
import de.inovex.pepper.intelligence.mlkit.ui.drawing.DrawingFragment
import de.inovex.pepper.intelligence.mlkit.ui.menu.MenuFragment
import de.inovex.pepper.intelligence.mlkit.ui.reading.ReadingFragment
import de.inovex.pepper.intelligence.mlkit.ui.seeing.SeeingFragment
import de.inovex.pepper.intelligence.mlkit.ui.translating.TranslatingFragment
import de.inovex.pepper.intelligence.mlkit.utils.Language
import de.inovex.pepper.intelligence.mlkit.utils.hideLoadingIndicator
import de.inovex.pepper.intelligence.mlkit.utils.replaceFragment
import de.inovex.pepper.intelligence.mlkit.utils.showFragment
import de.inovex.pepper.intelligence.mlkit.utils.showLoadingIndicator
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class MainActivity : RobotActivity(), RobotLifecycleCallbacks {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var loadingIndicator: RelativeLayout
    private lateinit var loadingIndicatorProgress: ProgressBar
    private lateinit var homeButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.i("Oncreate")
        super.onCreate(savedInstanceState)

        // Set Content View
        setContentView(R.layout.activity_main)

        // Set Speech bar
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS)

        // Set Menu Fragment
        showFragment(supportFragmentManager, MenuFragment(), MenuFragment::class.simpleName)

        // Save current robot language
        // And show language alert if the current language not supported by the app
        when (Locale.getDefault()) {
            Locale.US -> viewModel.language = Language.ENGLISH
            Locale.GERMANY -> viewModel.language = Language.GERMAN
            Locale("es", "ES") -> viewModel.language = Language.SPANISH
            else -> {
                viewModel.language = Language.OTHER
                AlertDialog.Builder(this)
                    .setTitle(R.string.wrong_language_title_dialog)
                    .setMessage("")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
            }
        }

        // Set language flag icon
        val languageIcon = findViewById<ImageView>(R.id.languageIcon)
        languageIcon.setImageResource(
            when (viewModel.language) {
                Language.ENGLISH -> R.drawable.language_english_flag_icon
                Language.SPANISH -> R.drawable.language_spanish_flag_icon
                else -> R.drawable.language_german_flag_icon
            }
        )
        languageIcon.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        // Home button
        homeButton = findViewById(R.id.homeButton)

        // LoadingIndicator
        loadingIndicator = findViewById(R.id.loadingIndicator)
        loadingIndicatorProgress = findViewById(R.id.loadingIndicatorProgress)

        // Show loading indicator until the chat is running to indicate the robot is not listening
        showLoadingIndicator(
            loadingIndicator,
            loadingIndicatorProgress,
            resources.getColor(R.color.inovex_blue, null)
        )

        // Subscribe to menu buttons that open the demos
        viewModel.uiEvents.observe(this) {
            when (it) {
                MainViewModel.UiEvent.NavigateToDrawing -> openDemo(
                    DrawingFragment(),
                    DrawingFragment::class.simpleName
                )
                MainViewModel.UiEvent.NavigateToReading -> openDemo(
                    ReadingFragment(),
                    ReadingFragment::class.simpleName
                )
                MainViewModel.UiEvent.NavigateToSeeing -> openDemo(
                    SeeingFragment(),
                    SeeingFragment::class.simpleName
                )
                MainViewModel.UiEvent.NavigateToTranslating -> openDemo(
                    TranslatingFragment(),
                    TranslatingFragment::class.simpleName
                )
                else -> {}
            }
        }

        // Register Qi SDK
        QiSDK.register(this, this)
    }

    override fun onResume() {
        super.onResume()

        Timber.i("Onresume")
    }

    override fun onRestart() {
        super.onRestart()

        Timber.i("onRestart")
    }

    fun goHome(v: View) {
        homeButton.visibility = View.GONE

        Timber.i("Back to menu")
        replaceFragment(supportFragmentManager, MenuFragment(), MenuFragment::class.simpleName)
    }

    private fun openDemo(fragment: Fragment, tag: String?) {
        homeButton.visibility = View.VISIBLE

        Timber.i("Going to $tag")
        replaceFragment(supportFragmentManager, fragment, tag)
    }

    // Robot Focus /////////////////////////////////////////////////////

    override fun onRobotFocusGained(qiContext: QiContext?) {
        Timber.d("onRobotFocusGained")

        if (qiContext != null) {
            // Save QiContext
            viewModel.qiContext = qiContext

            // Start chat and explain demo rules
            viewModel.startChat() {
                runOnUiThread {
                    hideLoadingIndicator(loadingIndicator)
                    val fragment: MenuFragment =
                        supportFragmentManager.findFragmentByTag(MenuFragment::class.simpleName) as MenuFragment
                    if (fragment.isVisible) {
                        fragment.explainDemoRules()
                    }
                }
            }
            // Set listeners for QiChat Bookmarks
            setQiChatBookmarkListeners()
        }
    }

    private fun setQiChatBookmarkListeners() {
        // Set bookmark listener to start recognition of a drawing
        viewModel.qiChatbot?.bookmarkStatus(viewModel.mainTopic.bookmarks[getString(R.string.readyToRecognizeBookmark)])
            ?.addOnReachedListener {
                runOnUiThread {
                    val fragment: DrawingFragment =
                        supportFragmentManager.findFragmentByTag(DrawingFragment::class.simpleName) as DrawingFragment
                    if (fragment.isVisible) {
                        fragment.recognizeDrawing()
                    }
                }
            }

        // Set bookmark listener to clear a drawing
        viewModel.qiChatbot?.bookmarkStatus(viewModel.mainTopic.bookmarks[getString(R.string.clearBookmark)])
            ?.addOnReachedListener {
                runOnUiThread {
                    val fragment: DrawingFragment =
                        supportFragmentManager.findFragmentByTag(DrawingFragment::class.simpleName) as DrawingFragment
                    if (fragment.isVisible) {
                        fragment.clear()
                    }
                }
            }

        // Set bookmark listeners for starting the Use Cases
        viewModel.qiChatbot?.bookmarkStatus(viewModel.mainTopic.bookmarks[getString(R.string.startDrawingBookmark)])
            ?.addOnReachedListener {
                runOnUiThread {
                    openDemo(DrawingFragment(), DrawingFragment::class.simpleName)
                }
            }

        viewModel.qiChatbot?.bookmarkStatus(viewModel.mainTopic.bookmarks[getString(R.string.startReadingBookmark)])
            ?.addOnReachedListener {
                runOnUiThread {
                    openDemo(ReadingFragment(), ReadingFragment::class.simpleName)
                }
            }

        viewModel.qiChatbot?.bookmarkStatus(viewModel.mainTopic.bookmarks[getString(R.string.startSeeingBookmark)])
            ?.addOnReachedListener {
                runOnUiThread {
                    openDemo(SeeingFragment(), SeeingFragment::class.simpleName)
                }
            }

        viewModel.qiChatbot?.bookmarkStatus(viewModel.mainTopic.bookmarks[getString(R.string.startTranslatingBookmark)])
            ?.addOnReachedListener {
                runOnUiThread {
                    openDemo(TranslatingFragment(), TranslatingFragment::class.simpleName)
                }
            }

        // Set bookmark listeners to respond to seeing questions asked
        viewModel.qiChatbot?.bookmarkStatus(viewModel.mainTopic.bookmarks[getString(R.string.askedWhereItIsBookmark)])
            ?.addOnReachedListener {
                val fragment: SeeingFragment =
                    supportFragmentManager.findFragmentByTag(SeeingFragment::class.simpleName) as SeeingFragment
                if (fragment.isVisible) {
                    fragment.locateObject()
                }
            }

        // Set bookmark listeners to respond to translation questions asked
        viewModel.qiChatbot?.bookmarkStatus(
            viewModel.mainTopic.bookmarks[getString(R.string.askedToTranslateBookmark)]
        )?.addOnReachedListener {
            val fragment: TranslatingFragment =
                supportFragmentManager.findFragmentByTag(
                    TranslatingFragment::class.simpleName
                ) as TranslatingFragment
            if (fragment.isVisible) {
                fragment.translate()
            }
        }

        viewModel.qiChatbot?.bookmarkStatus(
            viewModel.mainTopic.bookmarks[getString(R.string.pronounceTranslationBookmark)]
        )?.addOnReachedListener {
            val fragment: TranslatingFragment = supportFragmentManager.findFragmentByTag(
                TranslatingFragment::class.simpleName
            ) as TranslatingFragment
            if (fragment.isVisible) {
                fragment.pronounceTranslation()
            }
        }
    }

    override fun onRobotFocusLost() {
        Timber.d("Robot Focus lost")
    }

    override fun onRobotFocusRefused(reason: String?) {
        Timber.e("Robot Focus refused")
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        // Remove all listeners and chatbots
        viewModel.pepperActions.removeChatListeners(viewModel.qiChatbot)
        viewModel.qiChatbot = null

        // Unregister QiSDK
        QiSDK.unregister(this)
        super.onDestroy()
    }
}
