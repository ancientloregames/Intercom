package com.ancientlore.intercom.di

import java.lang.RuntimeException

object NoFragmentParamsException: RuntimeException("Fragment params are mandatory")