import android.app.NotificationManager
import com.tejpratapsingh.animator.R

// Assuming your NotificationChannelType enum looks something like this:
// You'll need to add these string resources to your strings.xml file.
// e.g., <string name="notification_channel_rendering_progress_name">Rendering Progress</string>

enum class NotificationChannelType(
    val channelId: String,
    val channelNameResId: Int, // Changed to Int for resource ID
    val channelDescriptionResId: Int, // Changed to Int for resource ID
    val importance: Int
) {
    RENDERING_PROGRESS(
        "render_progress_channel",
        R.string.notification_channel_rendering_progress_name,
        R.string.notification_channel_rendering_progress_description,
        NotificationManager.IMPORTANCE_LOW
    ),
    RENDERING_COMPLETED(
        "render_completed_channel",
        R.string.notification_channel_rendering_completed_name,
        R.string.notification_channel_rendering_completed_description,
        NotificationManager.IMPORTANCE_DEFAULT
    );
}
