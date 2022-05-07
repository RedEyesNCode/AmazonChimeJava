package com.redeyesncode.amazonchimekotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoFacade
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.*
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.CameraCaptureSource
import com.amazonaws.services.chime.sdk.meetings.device.MediaDeviceType
import com.amazonaws.services.chime.sdk.meetings.utils.logger.Logger
import com.redeyesncode.amazonchimekotlin.R
import com.redeyesncode.amazonchimekotlin.data.VideoCollectionTile
import com.redeyesncode.amazonchimekotlin.databinding.ItemVideoBinding
import com.redeyesncode.amazonchimekotlin.demoapp.activity.MeetingActivity
import com.redeyesncode.amazonchimekotlin.utils.inflate
import com.redeyesncode.amazonchimekotlin.utils.isLandscapeMode

class VideoAdapter(
    private val videoCollectionTiles: Collection<VideoCollectionTile>,
    private val userPausedVideoTileIds: MutableSet<Int>,
    private val remoteVideoSourceConfigurations: MutableMap<RemoteVideoSource, VideoSubscriptionConfiguration>,
    private val audioVideoFacade: AudioVideoFacade,
    private val cameraCaptureSource: CameraCaptureSource?,
    private val context: Context?,
    private val logger: Logger
) : RecyclerView.Adapter<VideoHolder>() {
    private lateinit var tabContentLayout: ConstraintLayout
    private val VIDEO_ASPECT_RATIO_16_9 = 0.5625

    private lateinit var binding : ItemVideoBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        tabContentLayout = (context as MeetingActivity).findViewById(R.id.constraintLayout)

        binding = ItemVideoBinding.inflate(LayoutInflater.from(context),parent,false)
        val inflatedView = parent.inflate(R.layout.item_video, false)
        return VideoHolder(
            context,
            inflatedView,
            audioVideoFacade,
            userPausedVideoTileIds,
            remoteVideoSourceConfigurations,
            logger,
            cameraCaptureSource,
            binding
        )
    }

    override fun getItemCount(): Int {
        return videoCollectionTiles.size
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val videoCollectionTile = videoCollectionTiles.elementAt(position)
        holder.bindVideoTile(videoCollectionTile)
        context?.let {
            if (!videoCollectionTile.videoTileState.isContent) {
                val viewportWidth = tabContentLayout.width
                if (isLandscapeMode(context)) {
                    holder.tileContainer.layoutParams.width = viewportWidth / 2
                } else {
                    holder.tileContainer.layoutParams.height =
                        (VIDEO_ASPECT_RATIO_16_9 * viewportWidth).toInt()
                }
            }
            val videoRenderView = holder.videoBinding.videoSurface
            videoRenderView.scalingType = VideoScalingType.AspectFit
            videoRenderView.hardwareScaling = false
        }
    }
}

class VideoHolder(
    private val context: Context,
    private val view: View,
    private val audioVideo: AudioVideoFacade,
    private val userPausedVideoTileIds: MutableSet<Int>,
    private val remoteVideoSourceConfigurations: MutableMap<RemoteVideoSource, VideoSubscriptionConfiguration>,
    private val logger: Logger,
    private val cameraCaptureSource: CameraCaptureSource?,
    var binding: ItemVideoBinding
) : RecyclerView.ViewHolder(view) {

    val tileContainer: ConstraintLayout = view.findViewById(R.id.tile_container)
    var videoBinding = ItemVideoBinding.bind(view)

    init {
        videoBinding.videoSurface.logger = logger
    }

    fun bindVideoTile(videoCollectionTile: VideoCollectionTile) {
        audioVideo.bindVideoView(videoBinding.videoSurface, videoCollectionTile.videoTileState.tileId)
        // Save the bound VideoRenderView in order to explicitly control the visibility of SurfaceView.
        // This is to bypass the issue where we cannot hide a SurfaceView that overlaps with another one.
        videoCollectionTile.videoRenderView = videoBinding.videoSurface
        videoCollectionTile.pauseMessageView = videoBinding.poorConnectionMessage

        if (videoCollectionTile.videoTileState.isContent) {
            videoBinding.videoSurface.contentDescription = "ScreenTile"
        } else {
            videoBinding.videoSurface.contentDescription = "${videoCollectionTile.attendeeName} VideoTile"
        }
        if (videoCollectionTile.videoTileState.isLocalTile) {
            videoBinding.onTileButton.setImageResource(R.drawable.ic_switch_camera)
            videoBinding.attendeeName.visibility = View.GONE
            videoBinding.onTileButton.visibility = View.VISIBLE

            // To facilitate demoing and testing both use cases, we account for both our external
            // camera and the camera managed by the facade. Actual applications should
            // only use one or the other
            updateLocalVideoMirror()
            videoBinding.onTileButton.setOnClickListener {
                if (audioVideo.getActiveCamera() != null) {
                    audioVideo.switchCamera()
                } else {
                    cameraCaptureSource?.switchCamera()
                }
                updateLocalVideoMirror()
            }
        } else {
            videoBinding.videoSurface.mirror = false
            videoBinding.attendeeName.text = videoCollectionTile.attendeeName
            videoBinding.attendeeName.visibility = View.VISIBLE
            videoBinding.onTileButton.visibility = View.VISIBLE
            when (videoCollectionTile.videoTileState.pauseState) {
                VideoPauseState.Unpaused ->
                    videoBinding.onTileButton.setImageResource(R.drawable.ic_pause_video)
                VideoPauseState.PausedByUserRequest ->
                    videoBinding.onTileButton.setImageResource(R.drawable.ic_resume_video)
                VideoPauseState.PausedForPoorConnection ->
                    videoBinding.poorConnectionMessage.visibility = View.VISIBLE
            }

            videoBinding.onTileButton.setOnClickListener {
                val tileId = videoCollectionTile.videoTileState.tileId
                if (videoCollectionTile.videoTileState.pauseState == VideoPauseState.Unpaused) {
                    audioVideo.pauseRemoteVideoTile(tileId)
                    userPausedVideoTileIds.add(tileId)
                    videoBinding.onTileButton.setImageResource(R.drawable.ic_resume_video)
                } else {
                    audioVideo.resumeRemoteVideoTile(tileId)
                    userPausedVideoTileIds.remove(tileId)
                    videoBinding.onTileButton.setImageResource(R.drawable.ic_pause_video)
                }
            }

            videoBinding.videoSurface.setOnClickListener {
                val attendeeId = videoCollectionTile.videoTileState.attendeeId
                showPriorityPopup(videoBinding.onTileButton, attendeeId)
            }
        }

        videoBinding.videoConfigButton.setOnClickListener {
            val attendeeId = videoCollectionTile.videoTileState.attendeeId
            showResolutionPopup(videoBinding.videoConfigButton, attendeeId)
        }
    }

    private fun showPriorityPopup(view: View, attendeeId: String) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.priority_popup_menu)
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            val priority = when (item?.itemId) {
                R.id.highest -> {
                    VideoPriority.Highest
                }
                R.id.high -> {
                    VideoPriority.High
                }
                R.id.medium -> {
                    VideoPriority.Medium
                }
                R.id.low -> {
                    VideoPriority.Low
                }
                R.id.lowest -> {
                    VideoPriority.Lowest
                }
                else -> {
                    VideoPriority.Lowest
                }
            }

            for (source in remoteVideoSourceConfigurations) {
                if (source.key?.attendeeId == attendeeId) {
                    val resolution: VideoResolution = source.value.targetResolution
                    source.setValue(VideoSubscriptionConfiguration(priority, resolution))
                }
            }
            audioVideo.updateVideoSourceSubscriptions(remoteVideoSourceConfigurations, emptyArray())
            true
        }
        popup.show()
    }

    private fun showResolutionPopup(view: View, attendeeId: String) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.resolution_popup_menu)
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            var resolution = when (item?.itemId) {
                R.id.high -> {
                    VideoResolution.High
                }
                R.id.medium -> {
                    VideoResolution.Medium
                }
                R.id.low -> {
                    VideoResolution.Low
                }
                else -> {
                    VideoResolution.Low
                }
            }

            for (source in remoteVideoSourceConfigurations) {
                if (source.key.attendeeId == attendeeId) {
                    val priority: VideoPriority = source.value.priority
                    source.setValue(VideoSubscriptionConfiguration(priority, resolution))
                }
            }

            audioVideo.updateVideoSourceSubscriptions(remoteVideoSourceConfigurations, emptyArray())
            true
        }
        popup.show()
    }

    private fun updateLocalVideoMirror() {
        videoBinding.videoSurface.mirror =
                // If we are using internal source, base mirror state off that device type
            (audioVideo.getActiveCamera()?.type == MediaDeviceType.VIDEO_FRONT_CAMERA ||
                    // Otherwise (audioVideo.getActiveCamera() == null) use the device type of our external/custom camera capture source
                    (audioVideo.getActiveCamera() == null && cameraCaptureSource?.device?.type == MediaDeviceType.VIDEO_FRONT_CAMERA))
    }
}