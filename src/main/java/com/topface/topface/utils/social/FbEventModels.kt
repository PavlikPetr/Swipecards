package com.topface.topface.utils.social

import com.topface.topface.data.AppOptions

/**
 * Event with fb app link, dispatched when link is ready
 */
data class FbAppLinkReadyEvent(val appLink: String)

/**
 * Event with templates, dispatched when got them from appOptions
 */
data class FbInviteTemplatesEvent(val inviteTemplates: AppOptions.Invites)

/**
 * Event about Facebook app link was stored
 */
data class FbAppLinkStoredEvent(val appLink:String?)

/**
 * Event about auth status state calculated
 */
data class AuthStatusReadyEvent(val authStatus:String?)