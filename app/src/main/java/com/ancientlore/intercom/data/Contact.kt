package com.ancientlore.intercom.data

import android.net.Uri

data class Contact(val id: String = "",
                   val phone: String = "",
                   val name: String = "",
                   val photoUri: Uri = Uri.EMPTY)
