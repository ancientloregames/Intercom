package com.ancientlore.intercom.di

import com.ancientlore.intercom.di.auth.email.login.EmailLoginModule
import com.ancientlore.intercom.di.auth.email.login.EmailLoginScreenScope
import com.ancientlore.intercom.di.auth.email.login.EmailLoginViewModelModule
import com.ancientlore.intercom.di.auth.email.signup.EmailSignupModule
import com.ancientlore.intercom.di.auth.email.signup.EmailSignupScreenScope
import com.ancientlore.intercom.di.auth.email.signup.EmailSignupViewModelModule
import com.ancientlore.intercom.di.auth.phone.check.PhoneCheckModule
import com.ancientlore.intercom.di.auth.phone.check.PhoneCheckScreenScope
import com.ancientlore.intercom.di.auth.phone.check.PhoneCheckViewModelModule
import com.ancientlore.intercom.di.auth.phone.login.PhoneLoginModule
import com.ancientlore.intercom.di.auth.phone.login.PhoneLoginScreenScope
import com.ancientlore.intercom.di.auth.phone.login.PhoneLoginViewModelModule
import com.ancientlore.intercom.di.broadcast.creation.BroadcastCreationModule
import com.ancientlore.intercom.di.broadcast.creation.BroadcastCreationScreenScope
import com.ancientlore.intercom.di.broadcast.creation.BroadcastCreationViewModelModule
import com.ancientlore.intercom.di.broadcast.list.BroadcastListModule
import com.ancientlore.intercom.di.broadcast.list.BroadcastListScreenScope
import com.ancientlore.intercom.di.broadcast.list.BroadcastListViewModelModule
import com.ancientlore.intercom.di.call.answer.audio.AudioCallAnswerModule
import com.ancientlore.intercom.di.call.answer.audio.AudioCallAnswerScreenScope
import com.ancientlore.intercom.di.call.answer.audio.AudioCallAnswerViewModelModule
import com.ancientlore.intercom.di.call.answer.video.VideoCallAnswerModule
import com.ancientlore.intercom.di.call.answer.video.VideoCallAnswerScreenScope
import com.ancientlore.intercom.di.call.answer.video.VideoCallAnswerViewModelModule
import com.ancientlore.intercom.di.call.offer.audio.AudioCallOfferModule
import com.ancientlore.intercom.di.call.offer.audio.AudioCallOfferScreenScope
import com.ancientlore.intercom.di.call.offer.audio.AudioCallOfferViewModelModule
import com.ancientlore.intercom.di.call.offer.video.VideoCallOfferModule
import com.ancientlore.intercom.di.call.offer.video.VideoCallOfferScreenScope
import com.ancientlore.intercom.di.call.offer.video.VideoCallOfferViewModelModule
import com.ancientlore.intercom.di.chat.creation.ChatCreationModule
import com.ancientlore.intercom.di.chat.creation.ChatCreationScreenScope
import com.ancientlore.intercom.di.chat.creation.ChatCreationViewModelModule
import com.ancientlore.intercom.di.chat.creation.group.ChatCreationGroupModule
import com.ancientlore.intercom.di.chat.creation.group.ChatCreationGroupScreenScope
import com.ancientlore.intercom.di.chat.creation.group.ChatCreationGroupViewModelModule
import com.ancientlore.intercom.di.chat.creation.group.desc.ChatCreationDescModule
import com.ancientlore.intercom.di.chat.creation.group.desc.ChatCreationDescScreenScope
import com.ancientlore.intercom.di.chat.creation.group.desc.ChatCreationDescViewModelModule
import com.ancientlore.intercom.di.chat.detail.ChatDetailModule
import com.ancientlore.intercom.di.chat.detail.ChatDetailScreenScope
import com.ancientlore.intercom.di.chat.detail.ChatDetailViewModelModule
import com.ancientlore.intercom.di.chat.flow.ChatFlowModule
import com.ancientlore.intercom.di.chat.flow.ChatFlowScreenScope
import com.ancientlore.intercom.di.chat.flow.ChatFlowViewModelModule
import com.ancientlore.intercom.di.chat.list.ChatListModule
import com.ancientlore.intercom.di.chat.list.ChatListScreenScope
import com.ancientlore.intercom.di.chat.list.ChatListViewModelModule
import com.ancientlore.intercom.di.contact.detail.ContactDetailModule
import com.ancientlore.intercom.di.contact.detail.ContactDetailScreenScope
import com.ancientlore.intercom.di.contact.detail.ContactDetailViewModelModule
import com.ancientlore.intercom.di.contact.list.ContactListModule
import com.ancientlore.intercom.di.contact.list.ContactListScreenScope
import com.ancientlore.intercom.di.contact.list.ContactListViewModelModule
import com.ancientlore.intercom.di.image.viewer.ImageViewerModule
import com.ancientlore.intercom.di.image.viewer.ImageViewerScreenScope
import com.ancientlore.intercom.di.image.viewer.ImageViewerViewModelModule
import com.ancientlore.intercom.di.settings.SettingsModule
import com.ancientlore.intercom.di.settings.SettingsScreenScope
import com.ancientlore.intercom.di.settings.SettingsViewModelModule
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginFragment
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupFragment
import com.ancientlore.intercom.ui.auth.phone.check.PhoneCheckFragment
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginFragment
import com.ancientlore.intercom.ui.boadcast.creation.BroadcastCreationFragment
import com.ancientlore.intercom.ui.boadcast.list.BroadcastListFragment
import com.ancientlore.intercom.ui.call.answer.audio.AudioCallAnswerFragment
import com.ancientlore.intercom.ui.call.answer.video.VideoCallAnswerFragment
import com.ancientlore.intercom.ui.call.offer.audio.AudioCallOfferFragment
import com.ancientlore.intercom.ui.call.offer.video.VideoCallOfferFragment
import com.ancientlore.intercom.ui.chat.creation.ChatCreationFragment
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescFragment
import com.ancientlore.intercom.ui.chat.creation.group.ChatCreationGroupFragment
import com.ancientlore.intercom.ui.chat.detail.ChatDetailFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.ui.chat.list.ChatListFragment
import com.ancientlore.intercom.ui.contact.detail.ContactDetailFragment
import com.ancientlore.intercom.ui.contact.list.ContactListFragment
import com.ancientlore.intercom.ui.image.viewer.ImageViewerFragment
import com.ancientlore.intercom.ui.settings.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface FragmentBuildersModule {

	@EmailLoginScreenScope
	@ContributesAndroidInjector(
		modules = [EmailLoginModule::class, EmailLoginViewModelModule::class]
	)
	fun contributeEmailLoginFragment(): EmailLoginFragment

	@EmailSignupScreenScope
	@ContributesAndroidInjector(
		modules = [EmailSignupModule::class, EmailSignupViewModelModule::class]
	)
	fun contributeEmailSignupFragment(): EmailSignupFragment

	@PhoneCheckScreenScope
	@ContributesAndroidInjector(
		modules = [PhoneCheckModule::class, PhoneCheckViewModelModule::class]
	)
	fun contributePhoneCheckFragment(): PhoneCheckFragment

	@PhoneLoginScreenScope
	@ContributesAndroidInjector(
		modules = [PhoneLoginModule::class, PhoneLoginViewModelModule::class]
	)
	fun contributePhoneLoginFragment(): PhoneLoginFragment

	@ChatListScreenScope
	@ContributesAndroidInjector(
		modules = [ChatListModule::class, ChatListViewModelModule::class]
	)
	fun contributeChatListFragment(): ChatListFragment

	@ChatFlowScreenScope
	@ContributesAndroidInjector(
		modules = [ChatFlowModule::class, ChatFlowViewModelModule::class]
	)
	fun contributeChatFlowFragment(): ChatFlowFragment

	@ChatDetailScreenScope
	@ContributesAndroidInjector(
		modules = [ChatDetailModule::class, ChatDetailViewModelModule::class]
	)
	fun contributeChatDetailFragment(): ChatDetailFragment

	@ChatCreationScreenScope
	@ContributesAndroidInjector(
		modules = [ChatCreationModule::class, ChatCreationViewModelModule::class]
	)
	fun contributeChatCreationFragment(): ChatCreationFragment

	@ChatCreationGroupScreenScope
	@ContributesAndroidInjector(
		modules = [ChatCreationGroupModule::class, ChatCreationGroupViewModelModule::class]
	)
	fun contributeChatCreationGroupFragment(): ChatCreationGroupFragment

	@ChatCreationDescScreenScope
	@ContributesAndroidInjector(
		modules = [ChatCreationDescModule::class, ChatCreationDescViewModelModule::class]
	)
	fun contributeChatCreationDescFragment(): ChatCreationDescFragment

	@ContactListScreenScope
	@ContributesAndroidInjector(
		modules = [ContactListModule::class, ContactListViewModelModule::class]
	)
	fun contributeContactListFragment(): ContactListFragment

	@ContactDetailScreenScope
	@ContributesAndroidInjector(
		modules = [ContactDetailModule::class, ContactDetailViewModelModule::class]
	)
	fun contributeContactDetailFragment(): ContactDetailFragment

	@BroadcastListScreenScope
	@ContributesAndroidInjector(
		modules = [BroadcastListModule::class, BroadcastListViewModelModule::class]
	)
	fun contributeBroadcastListFragment(): BroadcastListFragment

	@BroadcastCreationScreenScope
	@ContributesAndroidInjector(
		modules = [BroadcastCreationModule::class, BroadcastCreationViewModelModule::class]
	)
	fun contributeBroadcastCreationFragment(): BroadcastCreationFragment

	@AudioCallAnswerScreenScope
	@ContributesAndroidInjector(
		modules = [AudioCallAnswerModule::class, AudioCallAnswerViewModelModule::class]
	)
	fun contributeAudioCallAnswerFragment(): AudioCallAnswerFragment

	@AudioCallOfferScreenScope
	@ContributesAndroidInjector(
		modules = [AudioCallOfferModule::class, AudioCallOfferViewModelModule::class]
	)
	fun contributeAudioCallOfferFragment(): AudioCallOfferFragment

	@VideoCallAnswerScreenScope
	@ContributesAndroidInjector(
		modules = [VideoCallAnswerModule::class, VideoCallAnswerViewModelModule::class]
	)
	fun contributeVideoCallAnswerFragment(): VideoCallAnswerFragment

	@VideoCallOfferScreenScope
	@ContributesAndroidInjector(
		modules = [VideoCallOfferModule::class, VideoCallOfferViewModelModule::class]
	)
	fun contributeVideoCallOfferFragment(): VideoCallOfferFragment

	@ImageViewerScreenScope
	@ContributesAndroidInjector(
		modules = [ImageViewerModule::class, ImageViewerViewModelModule::class]
	)
	fun contributeImageViewerFragment(): ImageViewerFragment

	@SettingsScreenScope
	@ContributesAndroidInjector(
		modules = [SettingsModule::class, SettingsViewModelModule::class]
	)
	fun contributeSettingsFragment(): SettingsFragment
}