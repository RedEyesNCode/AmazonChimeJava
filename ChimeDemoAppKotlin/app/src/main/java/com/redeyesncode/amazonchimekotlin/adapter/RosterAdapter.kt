package com.redeyesncode.amazonchimekotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.services.chime.sdk.meetings.audiovideo.SignalStrength
import com.amazonaws.services.chime.sdk.meetings.audiovideo.VolumeLevel
import com.amazonaws.services.chime.sdk.meetings.internal.AttendeeStatus
import com.redeyesncode.amazonchimekotlin.R
import com.redeyesncode.amazonchimekotlin.data.RosterAttendee
import com.redeyesncode.amazonchimekotlin.databinding.RowRosterBinding
import com.redeyesncode.amazonchimekotlin.utils.inflate

class RosterAdapter(
    private val roster: Collection<RosterAttendee>,var context: Context
) :
    RecyclerView.Adapter<RosterHolder>() {

    private lateinit var binding:RowRosterBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RosterHolder {
        binding = RowRosterBinding.inflate(LayoutInflater.from(context))
        val inflatedView = parent.inflate(R.layout.row_roster, false)
        return RosterHolder(inflatedView,binding)
    }

    override fun getItemCount(): Int {
        return roster.size
    }

    override fun onBindViewHolder(holder: RosterHolder, position: Int) {
        val attendee = roster.elementAt(position)
        holder.bindAttendee(attendee)
    }
}

class RosterHolder(inflatedView: View,binding: RowRosterBinding) :
    RecyclerView.ViewHolder(inflatedView) {

    private var view: View = inflatedView
    private var binding = RowRosterBinding.bind(inflatedView);

    fun bindAttendee(attendee: RosterAttendee) {
        val attendeeName = attendee.attendeeName
        binding.attendeeName.text = attendeeName
        binding.attendeeName.contentDescription = attendeeName
        binding.activeSpeakerIndicator.visibility = if (attendee.isActiveSpeaker) View.VISIBLE else View.INVISIBLE
        binding.activeSpeakerIndicator.contentDescription = if (attendee.isActiveSpeaker) "${attendee.attendeeName} Active" else ""

        if (attendee.attendeeStatus == AttendeeStatus.Joined) {
            if (attendee.signalStrength == SignalStrength.None ||
                attendee.signalStrength == SignalStrength.Low
            ) {
                val drawable = if (attendee.volumeLevel == VolumeLevel.Muted) {
                    R.drawable.ic_microphone_poor_connectivity_dissabled
                } else {
                    R.drawable.ic_microphone_poor_connectivity
                }
                binding.attendeeVolume.setImageResource(drawable)
                view.contentDescription = "$attendeeName Signal Strength Poor"
            } else {
                when (attendee.volumeLevel) {
                    VolumeLevel.Muted -> {
                        binding.attendeeVolume.setImageResource(R.drawable.ic_microphone_disabled)
                        binding.attendeeVolume.contentDescription = "$attendeeName Muted"
                    }
                    VolumeLevel.NotSpeaking -> {
                        binding.attendeeVolume.setImageResource(R.drawable.ic_microphone_enabled)
                        binding.attendeeVolume.contentDescription = "$attendeeName Not Speaking"
                    }
                    VolumeLevel.Low -> {
                        binding .attendeeVolume.setImageResource(R.drawable.ic_microphone_audio_1)
                        binding.attendeeVolume.contentDescription = "$attendeeName Speaking"
                    }
                    VolumeLevel.Medium -> {
                        binding.attendeeVolume.setImageResource(R.drawable.ic_microphone_audio_2)
                        binding.attendeeVolume.contentDescription = "$attendeeName Speaking"
                    }
                    VolumeLevel.High -> {
                        binding.attendeeVolume.setImageResource(R.drawable.ic_microphone_audio_3)
                        binding.attendeeVolume.contentDescription = "$attendeeName Speaking"
                    }
                }
            }
            binding.attendeeVolume.visibility = View.VISIBLE
        } else {
            binding.attendeeVolume.visibility = View.GONE
        }
    }
}