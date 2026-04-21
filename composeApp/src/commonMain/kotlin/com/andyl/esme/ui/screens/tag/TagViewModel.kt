package com.andyl.esme.ui.screens.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.esme.data.repository.NoteRepository
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.EsmeNote
import com.andyl.esme.ui.screens.tag.helper.TagGraphBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagViewModel(
    private val repository: NoteRepository,
    private val graphBuilder: TagGraphBuilder
) : ViewModel() {

    private val _state = MutableStateFlow(TagState())
    val state: StateFlow<TagState> = _state.asStateFlow()

    private var job: Job? = null

    fun load(tag: String) {
        job?.cancel()

        job = repository.getNotesWithBlocks()
            .map { notes -> graphBuilder.buildTagHub(tag, notes) }
            .onStart { _state.update { it.copy(isLoading = true, tag = tag) } }
            .onEach { hub ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        hub = hub
                    )
                }
            }
            .catch {
                _state.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleTask(block: EsmeBlock.Todo, checked: Boolean) {
        viewModelScope.launch {
            repository.saveBlocks(listOf(block.copy(isChecked = checked)))
        }
    }
}