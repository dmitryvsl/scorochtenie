package com.example.scorochenie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.scorochenie.R
import com.example.scorochenie.databinding.FragmentSpeedSelectionBinding
import com.example.scorochenie.domain.TechniqueType

class SpeedSelectionFragment : Fragment() {

    companion object {
        private const val ARG_TECHNIQUE_TYPE = "technique_type"

        fun newInstance(techniqueType: TechniqueType): SpeedSelectionFragment {
            return SpeedSelectionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TECHNIQUE_TYPE, techniqueType)
                }
            }
        }
    }

    private var _binding: FragmentSpeedSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeedSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val techniqueType = arguments?.getSerializable(ARG_TECHNIQUE_TYPE).let {
            it as? TechniqueType
                ?: throw RuntimeException("Can't find argument '$ARG_TECHNIQUE_TYPE' in fragment 'SpeedSelectionFragment'")
        }

        with(binding) {
            tvTechniqueTitle.text = techniqueType.displayName
            btnSlowSpeed.setOnClickListener {
                navigateToReadingTest(techniqueType, 200L)
            }
            btnMediumSpeed.setOnClickListener {
                navigateToReadingTest(techniqueType, 400L)
            }
            btnFastSpeed.setOnClickListener {
                navigateToReadingTest(techniqueType, 600L)
            }
        }
    }

    private fun navigateToReadingTest(techniqueType: TechniqueType, durationPerWord: Long) {
        val fragment = ReadingTestFragment.newInstance(techniqueType, durationPerWord)
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