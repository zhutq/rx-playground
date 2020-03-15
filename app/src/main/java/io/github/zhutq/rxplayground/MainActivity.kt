package io.github.zhutq.rxplayground

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import io.github.zhutq.rxplayground.ui.main.MainFragment
import io.github.zhutq.rxplayground.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    var fragment: MainFragment = MainFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            fragment.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, it)
                    .commitNow()
            }
        }
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    fun showHideFragment(view: View) {
        fragment.let {
            if (it.isAdded) {
                if (it.isHidden) {
                    supportFragmentManager.beginTransaction()
                        .show(it)
                        .commitAllowingStateLoss()
                } else {
                    supportFragmentManager.beginTransaction()
                        .hide(it)
                        .commitAllowingStateLoss()
                }
            }
        }
    }

    fun addRemoveFragment(view: View) {
        fragment.let {
            if (it.isAdded) {
                supportFragmentManager.beginTransaction()
                    .remove(it)
                    .commitAllowingStateLoss()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, it)
                    .commitAllowingStateLoss()
            }
        }
    }

    fun openSecondActivity(view: View) {
        startActivity(Intent(this, SecondActivity::class.java))
    }

    fun setValue(view: View) {
        viewModel.data.value = 100
    }
}
