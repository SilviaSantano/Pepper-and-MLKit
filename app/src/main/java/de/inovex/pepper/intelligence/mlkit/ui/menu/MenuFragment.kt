package de.inovex.pepper.intelligence.mlkit.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import de.inovex.pepper.intelligence.mlkit.R
import de.inovex.pepper.intelligence.mlkit.databinding.FragmentMenuBinding
import de.inovex.pepper.intelligence.mlkit.ui.main.MainViewModel

@AndroidEntryPoint
class MenuFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewmodel = viewModel
            }

        return binding.root
    }

    override fun onResume() {

        super.onResume()

        viewModel.uiEvents.observe(viewLifecycleOwner) {
            if (it is MainViewModel.UiEvent.ExplainDemoRules)
                explainDemoRules()
        }
    }

    fun explainDemoRules() {
        viewModel.goToQiChatBookmark(getString(R.string.demoRulesBookmark))
    }
}
