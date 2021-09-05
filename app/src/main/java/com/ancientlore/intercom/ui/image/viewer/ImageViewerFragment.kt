package com.ancientlore.intercom.ui.image.viewer

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ImageViewerUiBinding
import com.ancientlore.intercom.ui.BasicFragment

class ImageViewerFragment
	: BasicFragment<ImageViewerViewModel, ImageViewerUiBinding>() {

	companion object {

		private const val ARG_IMAGE_URI = "imageUri"

		fun newInstance(imageUrl: String) : ImageViewerFragment {
			return newInstance(Uri.parse(imageUrl))
		}

		fun newInstance(imageUri: Uri) : ImageViewerFragment {
			return ImageViewerFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_IMAGE_URI, imageUri)
				}
			}
		}
	}

	private val imageUri : Uri by lazy {
		arguments?.getParcelable<Uri>(ARG_IMAGE_URI)
		?: throw RuntimeException("Image uri is a mandotory arg") }

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getOpenAnimation(): Int = R.anim.center_scale_fade_in

	override fun getCloseAnimation(): Int = R.anim.center_scale_fade_out

	override fun getLayoutResId(): Int = R.layout.image_viewer_ui

	override fun createViewModel() = ImageViewerViewModel(imageUri)

	override fun bind(view: View, viewModel: ImageViewerViewModel) {
		dataBinding = ImageViewerUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: ImageViewerViewModel) {
		subscriptions.add(viewModel.closeRequest()
			.subscribe {
				close()
			})
	}
}