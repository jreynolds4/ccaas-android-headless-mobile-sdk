package com.example.xml_example.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.xml_example.MainActivity
import co.ccai.example.xml_example.R
import com.example.xml_example.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var editTextMenuId: EditText
    private lateinit var buttonContactSupport: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextMenuId = view.findViewById(R.id.editTextMenuId)
        buttonContactSupport = view.findViewById(R.id.buttonContactSupport)
        progressBar = view.findViewById(R.id.progressBar)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        editTextMenuId.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.menuId = editTextMenuId.text.toString()
            }
        }

        buttonContactSupport.setOnClickListener {
            viewModel.menuId = editTextMenuId.text.toString()
            viewModel.startContactCustomerSupport()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorToast.observe(viewLifecycleOwner) { error ->
                    if (error.isNotEmpty()) {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                }

                viewModel.showChatView.observe(viewLifecycleOwner) { showChat ->
                    if (showChat) {
                        viewModel.resetShowChatView()
                        (activity as? MainActivity)?.navigateToChat(
                            viewModel.menuId.toIntOrNull() ?: 0,
                            viewModel.chat.value
                        )
                    }
                }

                viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                    buttonContactSupport.isEnabled = !isLoading
                    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
