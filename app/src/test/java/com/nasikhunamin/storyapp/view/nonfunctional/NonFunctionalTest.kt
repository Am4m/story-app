package com.nasikhunamin.storyapp.view.nonfunctional

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NonFunctionalTest {

    @Test
    fun manifestShouldDeclareRequiredPermissionsForNetworkAndLocationFeatures() {
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        assertTrue(manifest.contains("""android.permission.INTERNET"""))
        assertTrue(manifest.contains("""android.permission.ACCESS_FINE_LOCATION"""))
        assertTrue(manifest.contains("""android.permission.ACCESS_COARSE_LOCATION"""))
    }

    @Test
    fun manifestShouldKeepInternalActivitiesNonExported() {
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        listOf(
            ".view.addstory.AddStoryActivity",
            ".view.addstory.maps.MapsPickedLocationActivity",
            ".view.detail.DetailActivity",
            ".view.login.LoginActivity",
            ".view.signup.SignupActivity",
            ".view.welcome.WelcomeActivity",
            ".view.maps.MapsActivity"
        ).forEach { activityName ->
            assertTrue(
                "$activityName should be declared as not exported",
                manifest.containsActivityExported(activityName, exported = false)
            )
        }
    }

    @Test
    fun manifestShouldExposeOnlyMainActivityAsLauncherEntryPoint() {
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        assertTrue(manifest.containsActivityExported(".view.main.MainActivity", exported = true))
        assertTrue(manifest.contains("""android.intent.action.MAIN"""))
        assertTrue(manifest.contains("""android.intent.category.LAUNCHER"""))
    }

    @Test
    fun fileProviderShouldBePrivateAndGrantUriPermissions() {
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")
        val filePaths = readProjectFile("app/src/main/res/xml/file_paths.xml")

        assertTrue(manifest.contains("android:name=\"androidx.core.content.FileProvider\""))
        assertTrue(manifest.contains("android:exported=\"false\""))
        assertTrue(manifest.contains("android:grantUriPermissions=\"true\""))
        assertTrue(filePaths.contains("""<cache-path"""))
        assertFalse(
            "FileProvider should not expose the whole external storage root",
            filePaths.contains("""<external-path""")
        )
    }

    @Test
    fun loginLayoutShouldUseProperInputTypesAndHideLoadingByDefault() {
        val layout = readProjectFile("app/src/main/res/layout/activity_login.xml")

        assertTrue(layout.contains("android:id=\"@+id/ed_login_email\""))
        assertTrue(layout.contains("android:inputType=\"textEmailAddress\""))
        assertTrue(layout.contains("android:id=\"@+id/ed_login_password\""))
        assertTrue(layout.contains("android:inputType=\"textPassword\""))
        assertTrue(layout.contains("app:passwordToggleEnabled=\"true\""))
        assertTrue(layout.contains("android:id=\"@+id/progressBar\""))
        assertTrue(layout.contains("android:visibility=\"gone\""))
    }

    @Test
    fun signupLayoutShouldUseCustomValidatedEmailAndPasswordFields() {
        val layout = readProjectFile("app/src/main/res/layout/activity_signup.xml")

        assertTrue(layout.contains("com.nasikhunamin.storyapp.utils.customview.EmailEditText"))
        assertTrue(layout.contains("com.nasikhunamin.storyapp.utils.customview.PasswordEditText"))
        assertTrue(layout.contains("android:id=\"@+id/progressBar\""))
        assertTrue(layout.contains("android:visibility=\"gone\""))
        assertTrue(layout.contains("android:contentDescription=\"@string/button_language_localization\""))
    }

    @Test
    fun addStoryLayoutShouldBeAccessibleAndKeepOptionalLocationControlsHiddenByDefault() {
        val layout = readProjectFile("app/src/main/res/layout/activity_add_story.xml")

        assertTrue(layout.contains("android:id=\"@+id/previewImageView\""))
        assertTrue(layout.contains("android:contentDescription=\"@string/add_story_img_cd\""))
        assertTrue(layout.contains("android:id=\"@+id/galleryButton\""))
        assertTrue(layout.contains("android:id=\"@+id/cameraButton\""))
        assertTrue(layout.contains("android:id=\"@+id/uploadButton\""))
        assertTrue(layout.contains("android:id=\"@+id/locationButton\""))
        assertTrue(layout.contains("android:visibility=\"gone\""))
        assertTrue(layout.contains("android:inputType=\"textMultiLine\""))
    }

    @Test
    fun mainLayoutShouldExposeStoryListsAndAccessibleAddStoryAction() {
        val layout = readProjectFile("app/src/main/res/layout/activity_main.xml")

        assertTrue(layout.contains("android:id=\"@+id/rv_stories\""))
        assertTrue(layout.contains("android:id=\"@+id/rv_story_feed\""))
        assertTrue(layout.contains("android:id=\"@+id/fab_add_story\""))
        assertTrue(layout.contains("android:contentDescription=\"@string/add_story\""))
        assertTrue(layout.contains("androidx.recyclerview.widget.LinearLayoutManager"))
        assertTrue(layout.contains("androidx.recyclerview.widget.StaggeredGridLayoutManager"))
    }

    @Test
    fun apiServiceShouldProtectAuthenticatedEndpointsWithAuthorizationHeader() {
        val apiService = readProjectFile("app/src/main/java/com/nasikhunamin/storyapp/data/retrofit/ApiService.kt")

        assertTrue(apiService.contains("""@FormUrlEncoded"""))
        assertTrue(apiService.contains("@POST(\"login\")"))
        assertTrue(apiService.contains("@POST(\"register\")"))
        assertTrue(apiService.contains("@GET(\"stories\")"))
        assertTrue(apiService.contains("@Header(\"Authorization\") token: String"))
        assertTrue(apiService.contains("""@Multipart"""))
        assertTrue(apiService.contains("@POST(\"stories\")"))
    }

    @Test
    fun apiConfigShouldDisableBodyLoggingOutsideDebugBuilds() {
        val apiConfig = readProjectFile("app/src/main/java/com/nasikhunamin/storyapp/data/retrofit/ApiConfig.kt")

        assertTrue(apiConfig.contains("BuildConfig.DEBUG"))
        assertTrue(apiConfig.contains("HttpLoggingInterceptor.Level.BODY"))
        assertTrue(apiConfig.contains("HttpLoggingInterceptor.Level.NONE"))
    }

    @Test
    fun userPreferenceShouldPersistSessionDataAndClearItOnLogout() {
        val userPreference = readProjectFile("app/src/main/java/com/nasikhunamin/storyapp/data/pref/UserPreference.kt")

        assertTrue(userPreference.contains("""preferences[EMAIL_KEY] = user.email"""))
        assertTrue(userPreference.contains("""preferences[TOKEN_KEY] = user.token"""))
        assertTrue(userPreference.contains("""preferences[IS_LOGIN_KEY] = true"""))
        assertTrue(userPreference.contains("""preferences.clear()"""))
        assertTrue(userPreference.contains("preferencesDataStore(name = \"session\")"))
    }

    @Test
    fun repositoryShouldUsePagingAndIdlingResourceForReliabilityInTests() {
        val storyRepository = readProjectFile("app/src/main/java/com/nasikhunamin/storyapp/data/repository/StoryRepository.kt")

        assertTrue(storyRepository.contains("PagingConfig"))
        assertTrue(storyRepository.contains("pageSize = 5"))
        assertTrue(storyRepository.contains("StoryRemoteMediator"))
        assertTrue(storyRepository.contains("wrapEspressoIdlingResource"))
    }

    @Test
    fun stringsShouldProvideClearValidationAndEmptyStateMessages() {
        val strings = readProjectFile("app/src/main/res/values/strings.xml")

        listOf(
            "email_error",
            "password_error",
            "empty_email_password",
            "message_pcked",
            "retry",
            "feature_not_available"
        ).forEach { stringName ->
            assertTrue("Missing string resource: $stringName", strings.contains("name=\"$stringName\""))
        }
    }

    @Test
    fun optionMenuShouldExposeMapsLocalizationAndLogoutActions() {
        val menu = readProjectFile("app/src/main/res/menu/option_menu.xml")

        assertTrue(menu.contains("android:id=\"@+id/maps\""))
        assertTrue(menu.contains("android:id=\"@+id/localization\""))
        assertTrue(menu.contains("android:id=\"@+id/action_logout\""))
        assertTrue(menu.contains("android:title=\"@string/log_out\""))
    }

    private fun String.containsActivityExported(activityName: String, exported: Boolean): Boolean {
        val exportedValue = if (exported) "true" else "false"
        val pattern = Regex(
            "<activity[\\s\\S]*?android:name=\"${Regex.escape(activityName)}\"[\\s\\S]*?android:exported=\"$exportedValue\""
        )
        return pattern.containsMatchIn(this)
    }

    private fun readProjectFile(path: String): String {
        return projectFile(path).readText()
    }

    private fun projectFile(path: String): File {
        val fromRoot = File(path)
        if (fromRoot.exists()) return fromRoot

        val fromModule = File(path.removePrefix("app/"))
        if (fromModule.exists()) return fromModule

        error("Cannot find project file: $path")
    }
}