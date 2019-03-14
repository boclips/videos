import com.boclips.security.utils.User
import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.UserId

fun getCurrentUserId() = UserId(value = getCurrentUser().id)

fun getCurrentUser() = UserExtractor.getCurrentUser() ?: User(false, "anonymous", emptySet())