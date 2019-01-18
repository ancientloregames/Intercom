package com.ancientlore.intercom.data

import android.net.Uri

data class Contact(var id: String = "",
                   var phone: String = "",
                   var name: String = "",
                   var photoUri: Uri = Uri.EMPTY)
