package com.eninetworks.notas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eninetworks.notas.data.entity.Password
import com.eninetworks.notas.data.repository.PasswordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PasswordViewModel(private val repo: PasswordRepository) : ViewModel() {

    private val _categoryFilter = MutableStateFlow("")
    val categoryFilter = _categoryFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val passwords: StateFlow<List<Password>> = _categoryFilter
        .flatMapLatest { cat -> if (cat.isBlank()) repo.getAll() else repo.getByCategory(cat) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategoryFilter(cat: String) { _categoryFilter.value = cat }

    fun save(title: String, username: String, password: String, website: String = "",
             notes: String = "", category: String = "") =
        viewModelScope.launch {
            repo.save(Password(title = title, username = username, encryptedPassword = password,
                website = website, notes = notes, category = category))
        }

    fun update(existing: Password, title: String, username: String, password: String,
               website: String, notes: String, category: String) =
        viewModelScope.launch {
            repo.update(existing.copy(title = title, username = username, encryptedPassword = password,
                website = website, notes = notes, category = category))
        }

    fun delete(password: Password) = viewModelScope.launch { repo.delete(password) }

    fun decrypt(encryptedPassword: String) = repo.decrypt(encryptedPassword)

    class Factory(private val repo: PasswordRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = PasswordViewModel(repo) as T
    }
}
