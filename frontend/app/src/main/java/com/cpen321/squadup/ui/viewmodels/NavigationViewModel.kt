package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.cpen321.squadup.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val navigationStateManager: NavigationStateManager
) : ViewModel()
