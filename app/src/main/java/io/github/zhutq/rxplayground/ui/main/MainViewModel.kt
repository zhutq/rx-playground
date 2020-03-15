package io.github.zhutq.rxplayground.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val data = MutableLiveData<Int>().apply { value = 100 }
}
