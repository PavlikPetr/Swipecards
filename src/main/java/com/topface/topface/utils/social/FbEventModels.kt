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