package com.redeyesncode.amazonchimekotlin.model

import androidx.lifecycle.ViewModel
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoFacade
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.CameraCaptureSource
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.DefaultEglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.EglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSession
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionConfiguration
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionCredentials
import com.redeyesncode.amazonchimekotlin.device.ScreenShareManager
import com.redeyesncode.amazonchimekotlin.utils.CpuVideoProcessor
import com.redeyesncode.amazonchimekotlin.utils.GpuVideoProcessor

class MeetingSessionModel : ViewModel() {
    lateinit var meetingSession: MeetingSession

    val credentials: MeetingSessionCredentials
        get() = meetingSession.configuration.credentials

    val configuration: MeetingSessionConfiguration
        get() = meetingSession.configuration

    val audioVideo: AudioVideoFacade
        get() = meetingSession.audioVideo

    // Graphics/capture related objects
    val eglCoreFactory: EglCoreFactory = DefaultEglCoreFactory()
    lateinit var cameraCaptureSource: CameraCaptureSource
    lateinit var gpuVideoProcessor: GpuVideoProcessor
    lateinit var cpuVideoProcessor: CpuVideoProcessor

    // Source for screen capture and share, will be set only if created in call
    var screenShareManager: ScreenShareManager? = null

    // For use with replica promotions, null if not a replica meeting
    var primaryExternalMeetingId: String? = null
}