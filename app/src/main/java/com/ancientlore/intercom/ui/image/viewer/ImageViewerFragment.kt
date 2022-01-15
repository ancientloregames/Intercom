package com.ancientlore.intercom.ui.image.viewer

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ImageViewerUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import javax.inject.Inject

class ImageViewerFragment
	: BasicFragment<ImageViewerViewModel, ImageViewerUiBinding>() {

	companion object {

		fun newInstance(imageUrl: String) : ImageViewerFragment {
			return newInstance(Uri.parse(imageUrl))
		}

		fun newInstance(imageUri: Uri) : ImageViewerFragment {
			return ImageViewerFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_FRAGMENT_PARAMS,
						ImageViewerViewModel.Params(imageUri))
				}
			}
		}
	}

	@Inject
	protected lateinit var params: ImageViewerViewModel.Params

	@Inject
	protected lateinit var viewModel: ImageViewerViewModel

	override fun getOpenAnimation(): Int = R.anim.center_scale_fade_in

	override fun getCloseAnimation(): Int = R.anim.center_scale_fade_out

	override fun getLayoutResId(): Int = R.layout.image_viewer_ui

	override fun createDataBinding(view: View) = ImageViewerUiBinding.bind(view)

	override fun requestViewModel(): ImageViewerViewModel = viewModel

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.ui = viewModel

		subscriptions.add(viewModel.closeRequest()
			.subscribe {
				close()
			})
	}
}