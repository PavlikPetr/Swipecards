package com.topface.topface.chat.vm

import android.databinding.ObservableBoolean
import com.topface.topface.chat.IComplainHeaderActionListener

/**
 * VM for chat screen (partial)
 * Created by m.bayutin on 27.03.17.
 */
class ChatViewModel(val complainActionListener: IComplainHeaderActionListener,
                    val complainVisibility: ObservableBoolean)