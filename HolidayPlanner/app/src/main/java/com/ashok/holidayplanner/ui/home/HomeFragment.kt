package com.ashok.holidayplanner.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ashok.holidayplanner.HolidayApplication
import com.ashok.holidayplanner.R
import com.ashok.holidayplanner.databinding.FragmentHomeBinding
import com.ashok.holidayplanner.ui.auth.AuthFragment
import com.ashok.holidayplanner.ui.auth.AuthViewModel
import com.ashok.holidayplanner.ui.holidays.HolidaysFragment
import com.ashok.holidayplanner.ui.leaves.LeavesFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        (requireActivity().application as HolidayApplication).container.viewModelFactory
    }

    private var currentTabTag: String = TAB_HOLIDAYS

    companion object {
        private const val TAB_HOLIDAYS = "tab_holidays"
        private const val TAB_LEAVES = "tab_leaves"
        private const val KEY_CURRENT_TAB = "key_current_tab"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupBottomNavigation()

        currentTabTag = savedInstanceState?.getString(KEY_CURRENT_TAB) ?: TAB_HOLIDAYS
        selectTab(currentTabTag)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_TAB, currentTabTag)
    }

    private fun setupToolbar() {
        val userName = authViewModel.getUserDisplayName()
        val isGuest = authViewModel.isGuest()

        val subtitle = if (isGuest) {
            getString(R.string.guest_account)
        } else {
            getString(R.string.signed_in_as, userName)
        }
        binding.topToolbar.subtitle = subtitle

        binding.topToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_sign_out -> {
                    authViewModel.signOut()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AuthFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_holidays -> {
                    selectTab(TAB_HOLIDAYS)
                    true
                }
                R.id.action_leaves -> {
                    selectTab(TAB_LEAVES)
                    true
                }
                else -> false
            }
        }
    }

    private fun selectTab(tag: String) {
        currentTabTag = tag
        val fragmentManager = childFragmentManager
        val existingFragment = fragmentManager.findFragmentByTag(tag)

        val targetFragment = existingFragment ?: when (tag) {
            TAB_HOLIDAYS -> HolidaysFragment()
            TAB_LEAVES -> LeavesFragment()
            else -> HolidaysFragment()
        }

        fragmentManager.beginTransaction()
            .replace(R.id.home_tab_container, targetFragment, tag)
            .commit()

        val targetMenuId = when (tag) {
            TAB_HOLIDAYS -> R.id.action_holidays
            TAB_LEAVES -> R.id.action_leaves
            else -> R.id.action_holidays
        }
        if (binding.bottomNavigation.selectedItemId != targetMenuId) {
            binding.bottomNavigation.selectedItemId = targetMenuId
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
