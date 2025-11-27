package com.ezt.video.instasaver.screen.home.download

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.base.BaseFragment
import com.ezt.video.instasaver.databinding.FragmentDownloadsBinding
import com.ezt.video.instasaver.screen.home.download.adapter.DownloadViewAdapter2
import com.ezt.video.instasaver.utils.Common.gone
import com.ezt.video.instasaver.utils.Common.visible
import com.ezt.video.instasaver.utils.Utils.hideKeyBoard
import com.ezt.video.instasaver.viewmodel.DownloadsViewModel
import com.ezt.video.instasaver.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadsFragment :
    BaseFragment<FragmentDownloadsBinding>(FragmentDownloadsBinding::inflate) {
    private val downloadViewModel: DownloadsViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var downloadViewAdapter2: DownloadViewAdapter2


    private var videoSelect = false
    private var photoSelect = false
    private var carouselSelect = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = context ?: return
        val layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd = true
        binding.downloadView.layoutManager = layoutManager
        downloadViewAdapter2 = DownloadViewAdapter2()
        binding.downloadView.adapter = downloadViewAdapter2
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        downloadViewModel.allPosts.observe(viewLifecycleOwner) { data ->
            println("FragmentDownloadsBinding: $data")
            downloadViewAdapter2.submitList(data)

            binding.faqButton.setOnClickListener {
                withSafeContext {
                    val ctx = context ?: return@setOnClickListener
                    val alertDialog = AlertDialog.Builder(ctx)
                        .setTitle("Delete your media files")
                        .setMessage("Do you want to clear all the IG files you downloaded?")
                        .setPositiveButton("Delete all") { dialogInterface, _ ->
                            data.onEach {
                                homeViewModel.deleteCarousel(it.link ?: "")
                            }
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                    alertDialog.show()
                }

            }
        }

        downloadViewModel.postByUser.observe(viewLifecycleOwner) { data ->
            downloadViewAdapter2.submitList(data)
        }

        binding.apply {
            searchView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 🔥 This is called every time the user types or deletes a character
                    val text = s.toString()
                    downloadViewModel.fetchPostsByUser(text, videoSelect, photoSelect, carouselSelect)
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            searchView.doOnTextChanged { text, _, _, _ ->
                val icon = if (!text.isNullOrEmpty())
                    ContextCompat.getDrawable(requireContext(), R.drawable.baseline_close_black_24)
                else null

                searchView.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
            }

            searchView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val editText = v as EditText

                    val drawableEnd = editText.compoundDrawables[2]
                    if (drawableEnd != null) {
                        val drawableWidth = drawableEnd.bounds.width()
                        if (event.rawX >= (editText.right - drawableWidth - editText.paddingEnd)) {
                            editText.text.clear()
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }

            downloadView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    withSafeContext { ctx ->
                        ctx.hideKeyBoard(searchView)
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dx != 0 || dy != 0) {
                        withSafeContext { ctx ->
                            ctx.hideKeyBoard(searchView)
                        }
                    }
                }
            })


            showTitle.setOnClickListener {
                if (showTitle.text.equals(resources.getString(R.string.show_less))) {
                    showTitle.text = resources.getString(R.string.show_more)
                    bonusOptions.gone()
                } else {
                    showTitle.text = resources.getString(R.string.show_less)
                    bonusOptions.visible()
                }
            }

            video.setOnClickListener {
                withSafeContext { ctx ->
                    videoSelect = !videoSelect
                    photoSelect = false
                    carouselSelect = false
                    if(videoSelect) {
                        downloadViewModel.fetchVideoPosts()
                    }

                    updateTextViewAppearance(ctx ,video, videoSelect)
                    updateTextViewAppearance(ctx ,photo, photoSelect)
                    updateTextViewAppearance(ctx ,carousel, carouselSelect)

                }

            }


            photo.setOnClickListener {
                withSafeContext { ctx ->
                    photoSelect = !photoSelect
                    videoSelect = false
                    carouselSelect = false

                    if(photoSelect) {
                        downloadViewModel.fetchPhotoPosts()
                    }
                    updateTextViewAppearance(ctx ,video, videoSelect)
                    updateTextViewAppearance(ctx ,photo, photoSelect)
                    updateTextViewAppearance(ctx ,carousel, carouselSelect)
                }

            }



            carousel.setOnClickListener {
                withSafeContext { ctx ->
                    carouselSelect = !carouselSelect
                    videoSelect = false
                    photoSelect = false

                    if(carouselSelect) {
                        downloadViewModel.fetchCarouselPosts()
                    }

                    updateTextViewAppearance(ctx ,video, videoSelect)
                    updateTextViewAppearance(ctx ,photo, photoSelect)
                    updateTextViewAppearance(ctx ,carousel, carouselSelect)
                }

            }
        }

    }

    private fun updateTextViewAppearance(ctx: Context, textView: TextView, condition: Boolean) {
        if(condition) {
            textView.setTextColor(ContextCompat.getColor(ctx, R.color.teal_200))
        } else {
            textView.setTextColor(ContextCompat.getColor(ctx, R.color.black))
        }
    }
}