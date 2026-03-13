package com.selegic.encye.article

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.selegic.encye.util.copyUriToCacheFile
import org.json.JSONObject
import androidx.compose.ui.unit.dp

@Composable
fun ArticleComposerRoute(
    onBack: () -> Unit,
    viewModel: ArticleComposerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val editorController = remember { RichArticleEditorController() }

    val coverPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val context = editorController.context ?: return@rememberLauncherForActivityResult
        val file = uri?.let { copyUriToCacheFile(context = context, uri = it, prefix = "cover") }
        viewModel.setCoverImage(file, uri?.lastPathSegment)
    }

    val inlineImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val context = editorController.context ?: return@rememberLauncherForActivityResult
        val file = uri?.let { copyUriToCacheFile(context = context, uri = it, prefix = "inline") } ?: return@rememberLauncherForActivityResult
        viewModel.uploadInlineImage(file)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
            onBack()
        }
    }

    LaunchedEffect(uiState.insertedInlineImageUrl) {
        val imageUrl = uiState.insertedInlineImageUrl
        if (!imageUrl.isNullOrBlank()) {
            editorController.insertImage(imageUrl)
            viewModel.clearInsertedInlineImageSignal()
        }
    }

    ArticleComposerScreen(
        uiState = uiState,
        editorController = editorController,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onTitleChange = viewModel::updateTitle,
        onSubtitleChange = viewModel::updateSubtitle,
        onToggleCategory = viewModel::toggleCategorySelection,
        onReloadCategories = viewModel::loadCategories,
        onSectionChange = viewModel::updateSection,
        onTagsChange = viewModel::updateTags,
        onPublishedChange = viewModel::updatePublished,
        onBodyChange = viewModel::updateHtmlDescription,
        onPickCoverImage = { coverPicker.launch("image/*") },
        onPickInlineImage = { inlineImagePicker.launch("image/*") },
        onSubmit = {
            editorController.requestHtml { html ->
                viewModel.updateHtmlDescription(html)
                viewModel.submit()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleComposerScreen(
    uiState: ArticleComposerUiState,
    editorController: RichArticleEditorController,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onToggleCategory: (String) -> Unit,
    onReloadCategories: () -> Unit,
    onSectionChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onPublishedChange: (Boolean) -> Unit,
    onBodyChange: (String) -> Unit,
    onPickCoverImage: () -> Unit,
    onPickInlineImage: () -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Write article") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 16.dp))
                    } else {
                        Button(
                            modifier = Modifier.padding(end = 12.dp),
                            onClick = onSubmit
                        ) {
                            Text("Publish")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.subtitle,
                onValueChange = onSubtitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Subtitle") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryMultiSelectField(
                    categories = uiState.availableCategories,
                    selectedCategoryIds = uiState.selectedCategoryIds,
                    isLoading = uiState.isLoadingCategories,
                    onToggleCategory = onToggleCategory,
                    onReload = onReloadCategories,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.section,
                    onValueChange = onSectionChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Section ID") },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = uiState.tags,
                onValueChange = onTagsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tags") },
                supportingText = { Text("Comma-separated") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Publish now", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = if (uiState.isPublished) "Article will be published" else "Article will be saved unpublished",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isPublished,
                    onCheckedChange = onPublishedChange
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.coverImageFile != null,
                    onClick = onPickCoverImage,
                    label = {
                        Text(uiState.coverImageName?.let { "Cover: $it" } ?: "Select cover image")
                    }
                )
                FilterChip(
                    selected = uiState.isUploadingInlineImage,
                    onClick = onPickInlineImage,
                    label = {
                        Text(if (uiState.isUploadingInlineImage) "Uploading inline image..." else "Insert inline image")
                    }
                )
            }

            Text(
                text = "Body",
                style = MaterialTheme.typography.titleMedium
            )

            HtmlRichEditor(
                modifier = Modifier
                    .fillMaxWidth(),
                initialHtml = uiState.htmlDescription,
                controller = editorController,
                onHtmlChange = onBodyChange,
                onRequestInlineImage = onPickInlineImage
            )

            Text(
                text = "The editor stores HTML and submits it through the existing article `description` field.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CategoryMultiSelectField(
    categories: List<CategoryOption>,
    selectedCategoryIds: List<String>,
    isLoading: Boolean,
    onToggleCategory: (String) -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedNames = categories
        .filter { it.id in selectedCategoryIds }
        .map { it.name }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = when {
                    isLoading -> "Loading categories..."
                    selectedNames.isEmpty() -> ""
                    else -> selectedNames.joinToString(", ")
                },
                onValueChange = { },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text("Categories") },
                placeholder = { Text("Choose categories") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                supportingText = {
                    val helperText = if (selectedCategoryIds.size > 1) {
                        "UI supports multiple selections, but publish currently accepts one category."
                    } else {
                        "Multiple selection UI backed by category tree."
                    }
                    Text(helperText)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (isLoading) {
                    DropdownMenuItem(
                        text = { Text("Loading...") },
                        onClick = { }
                    )
                } else if (categories.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Retry loading categories") },
                        onClick = onReload
                    )
                } else {
                    categories.forEach { category ->
                        val selected = category.id in selectedCategoryIds
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${"  ".repeat(category.depth)}${if (selected) "[x]" else "[ ]"} ${category.name}"
                                )
                            },
                            onClick = { onToggleCategory(category.id) }
                        )
                    }
                }
            }
        }

        if (selectedNames.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedNames.forEach { name ->
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text(name) }
                    )
                }
            }
        }
    }
}

class RichArticleEditorController {
    internal var webView: WebView? = null
    internal var isPageLoaded: Boolean = false
    internal var context: Context? = null
    internal var pendingHtml: String? = null

    fun loadHtml(html: String) {
        pendingHtml = html
        if (isPageLoaded) {
            webView?.evaluateJavascript(
                "window.EncyeEditor.setHtml(${JSONObject.quote(html)});",
                null
            )
        }
    }

    fun requestHtml(onResult: (String) -> Unit) {
        val activeWebView = webView
        if (activeWebView == null) {
            onResult(pendingHtml.orEmpty())
            return
        }

        activeWebView.evaluateJavascript("window.EncyeEditor.getHtml();") { result ->
            onResult(parseJsStringResult(result))
        }
    }

    fun insertImage(url: String) {
        webView?.evaluateJavascript(
            "window.EncyeEditor.insertImage(${JSONObject.quote(url)});",
            null
        )
    }
}

@Composable
private fun HtmlRichEditor(
    modifier: Modifier = Modifier,
    initialHtml: String,
    controller: RichArticleEditorController,
    onHtmlChange: (String) -> Unit,
    onRequestInlineImage: () -> Unit
) {
    var hasLoadedInitialHtml by remember { mutableStateOf(false) }

    DisposableEffect(controller) {
        onDispose {
            controller.webView = null
            controller.isPageLoaded = false
            controller.context = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                controller.context = context
                controller.webView = this
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        controller.isPageLoaded = true
                        val htmlToLoad = controller.pendingHtml ?: initialHtml
                        if (htmlToLoad.isNotBlank()) {
                            controller.loadHtml(htmlToLoad)
                        }
                    }
                }
                addJavascriptInterface(
                    EditorBridge(
                        onHtmlChange = onHtmlChange,
                        onRequestInlineImage = onRequestInlineImage
                    ),
                    "AndroidEditorBridge"
                )
                loadUrl("file:///android_asset/article_editor/index.html")
            }
        },
        update = {
            if (!hasLoadedInitialHtml && controller.isPageLoaded && initialHtml.isNotBlank()) {
                controller.loadHtml(initialHtml)
                hasLoadedInitialHtml = true
            }
        }
    )
}

private class EditorBridge(
    private val onHtmlChange: (String) -> Unit,
    private val onRequestInlineImage: () -> Unit
) {
    @JavascriptInterface
    fun onHtmlChanged(html: String) {
        onHtmlChange(html)
    }

    @JavascriptInterface
    fun requestInlineImage() {
        onRequestInlineImage()
    }
}

private fun parseJsStringResult(result: String?): String {
    if (result.isNullOrBlank() || result == "null") {
        return ""
    }

    return runCatching {
        JSONObject("""{"value":$result}""").getString("value")
    }.getOrElse {
        result.removePrefix("\"").removeSuffix("\"")
    }
}
