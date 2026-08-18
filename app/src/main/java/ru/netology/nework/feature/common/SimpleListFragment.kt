package ru.netology.nework.feature.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import ru.netology.nework.databinding.FragmentSimpleListBinding

open class SimpleListFragment(
    @param:StringRes private val titleRes: Int,
    @param:StringRes private val subtitleRes: Int
) : Fragment() {

    private var _binding: FragmentSimpleListBinding? = null
    protected val binding: FragmentSimpleListBinding
        get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.setText(titleRes)
        binding.subtitle.setText(subtitleRes)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
