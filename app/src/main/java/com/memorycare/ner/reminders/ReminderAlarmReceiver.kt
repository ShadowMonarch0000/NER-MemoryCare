package com.memorycare.ner.reminders

import android.R
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.memorycare.ner.MainActivity
import com.memorycare.ner.NERRepository
import com.memorycare.ner.ReminderType

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val repository = NERRepository(context)

        if (action == "ACTION_DONE") {
            val reminders = repository.getReminders()
            val rem = reminders.find { it.id == reminderId }
            if (rem != null) {
                repository.saveReminderItem(rem.copy(completedToday = true))
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(reminderId.hashCode())
            return
        }

        if (action == "ACTION_SNOOZE") {
            val reminders = repository.getReminders()
            val rem = reminders.find { it.id == reminderId }
            if (rem != null) {
                ReminderScheduler.scheduleReminderAlarm(context, rem, delayMinutes = 15L)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(reminderId.hashCode())
            return
        }

        // Show High Priority Notification
        val title = intent.getStringExtra("reminder_title") ?: "Daily Reminder"
        val message = intent.getStringExtra("reminder_message") ?: "It is time for your scheduled activity."
        val typeStr = intent.getStringExtra("reminder_type") ?: ReminderType.MEDICINE.name

        val iconEmoji = when (typeStr) {
            ReminderType.MEDICINE.name -> "💊 "
            ReminderType.WALKING.name -> "🚶 "
            ReminderType.SLEEP.name -> "😴 "
            ReminderType.COGNITIVE_GAME.name -> "🧠 "
            else -> "⏰ "
        }

        // Action Intention for Open App
        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Action DONE PendingIntent
        val doneIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            setAction("ACTION_DONE")
            putExtra("reminder_id", reminderId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 1,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Action SNOOZE PendingIntent
        val snoozeIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            setAction("ACTION_SNOOZE")
            putExtra("reminder_id", reminderId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(iconEmoji + title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)
            .addAction(R.drawable.checkbox_on_background, "DONE", donePendingIntent)
            .addAction(R.drawable.ic_lock_idle_alarm, "REMIND ME LATER", snoozePendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reminderId.hashCode(), notification)
    }
}
