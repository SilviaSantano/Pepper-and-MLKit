package de.inovex.pepper.intelligence.mlkit.utils

import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import de.inovex.pepper.intelligence.mlkit.R

fun showLoadingIndicator(
    loadingIndicator: RelativeLayout,
    loadingIndicatorProgress: ProgressBar,
    color: Int
) {
    loadingIndicator.visibility = View.VISIBLE
    loadingIndicator.bringToFront()
    loadingIndicator.alpha = 1f
    loadingIndicatorProgress.isIndeterminate = true
    loadingIndicatorProgress.indeterminateDrawable.setTint(
        color
    )
}

fun hideLoadingIndicator(loadingIndicator: RelativeLayout) {
    loadingIndicator.visibility = View.GONE
}

fun showFragment(supportFragmentManager: FragmentManager, fragment: Fragment, tag: String?) {
    // Begin the transaction
    val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

    // Replace the contents of the container with the new fragment
    ft.replace(R.id.fragmentContainer, fragment, tag)
    ft.commit()
}

fun replaceFragment(supportFragmentManager: FragmentManager, fragment: Fragment, tag: String?) {
    if (supportFragmentManager.findFragmentByTag(tag)?.isVisible != true) {
        // Begin the transaction
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

        // Replace the contents of the container with the new fragment
        ft.replace(R.id.fragmentContainer, fragment, tag)
        ft.commit()
    }
}
