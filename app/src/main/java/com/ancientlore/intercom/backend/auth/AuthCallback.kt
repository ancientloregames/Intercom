package com.ancientlore.intercom.backend.auth

import com.ancientlore.intercom.backend.RequestCallback

interface AuthCallback : RequestCallback<AuthManager.User>