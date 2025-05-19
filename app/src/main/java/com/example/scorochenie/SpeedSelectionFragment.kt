package com.example.scorochenie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.scorochenie.databinding.FragmentSpeedSelectionBinding

class SpeedSelectionFragment : Fragment() {

    companion object {
        private const val ARG_TECHNIQUE_NAME = "technique_name"
        fun newInstance(techniqueName: String): SpeedSelectionFragment {
            val fragment = SpeedSelectionFragment()
            val args = Bundle()
            args.putString(ARG_TECHNIQUE_NAME, techniqueName)
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentSpeedSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeedSelectionBinding.inflate(inflater, container, false)
        val view = binding.root

        val techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME) ?: ""

        binding.btnSlowSpeed.setOnClickListener {
            navigateToReading(techniqueName, 200L)
        }
        binding.btnMediumSpeed.setOnClickListener {
            navigateToReading(techniqueName, 400L)
        }
        binding.btnFastSpeed.setOnClickListener {
            navigateToReading(techniqueName, 600L)
        }

        return view
    }

    private fun navigateToReading(techniqueName: String, durationPerWord: Long) {
        val fragment = ReadingTestFragment.newInstance(techniqueName, durationPerWord)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}