package com.topface.topface.ui.auth.fb_invitation

import java.io.Serializable

class Language : Serializable {

    var name: String? = null
    var emailId: String? = null
    var isSelected: Boolean = false

    constructor() {

    }

    constructor(name: String, emailId: String) {

        this.name = name
        this.emailId = emailId

    }

    constructor(name: String, emailId: String, isSelected: Boolean) {

        this.name = name
        this.emailId = emailId
        this.isSelected = isSelected
    }


}