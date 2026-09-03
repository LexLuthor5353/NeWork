package ru.netology.nework.feature.events

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEditEventBinding
import ru.netology.nework.feature.common.MapFragment

class EditEventFragment : Fragment(R.layout.fragment_edit_event) {

    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditEventBinding.bind(view)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_save, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.actionSave) {
                    parentFragmentManager.popBackStack()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.editEventLocationButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, MapFragment.newInstance(pickLocation = true))
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
