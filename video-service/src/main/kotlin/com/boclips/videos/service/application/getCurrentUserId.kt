import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.UserId

fun UserExtractor.getCurrentUserId() = this.getCurrentUser().let { UserId(value = it.id) }