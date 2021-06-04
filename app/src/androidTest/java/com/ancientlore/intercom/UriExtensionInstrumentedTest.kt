package com.ancientlore.intercom

import android.net.Uri
import androidx.test.runner.AndroidJUnit4
import com.ancientlore.intercom.utils.extensions.isInternal
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UriExtensionInstrumentedTest {

	private lateinit var mockFileUri: Uri
	private lateinit var mockContentUri: Uri

	private lateinit var mockHttpUri: Uri
	private lateinit var mockFtpUri: Uri

	@Before
	fun setup() {
		mockFileUri = Uri.parse("file:///some/path/to/file.ext")
		mockContentUri = Uri.parse("content://some/path/to/file.ext")
		mockHttpUri = Uri.parse("http:///some/path/to/file.ext")
		mockFtpUri = Uri.parse("ftp://some/path/to/file.ext")
	}

	@Test
	fun isUriInternal_correct() {

		Assert.assertEquals(true, mockFileUri.isInternal())
		Assert.assertEquals(true, mockContentUri.isInternal())

		Assert.assertEquals(false, mockHttpUri.isInternal())
		Assert.assertEquals(false, mockFtpUri.isInternal())
	}
}