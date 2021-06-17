package customRuleset

import com.pinterest.ktlint.core.*
import org.jetbrains.kotlin.com.intellij.lang.*
import org.jetbrains.kotlin.psi.*

class NoInternalImportRule() : Rule("no-internal-import"), Rule.Modifier.RestrictToRoot {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        val filePath = node.getUserData(KtLint.FILE_PATH_USER_DATA_KEY)

        if (filePath?.endsWith(".kt") != true) {
            return
        }

        val paths = (node.psi as KtFile).importDirectives.map { it.importPath?.pathStr }

        paths.forEach {
            if (it != null && filePath.contains("contentpartner") && it.contains("videos") && it.contains("service")) {
                emit(
                    node.startOffset, "You should not import $it from video service",
                    false
                )
            }
        }
    }
}
