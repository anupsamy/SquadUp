package com.cpen321.squadup.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.cpen321.squadup.data.remote.dto.*
import com.cpen321.squadup.data.repository.GroupRepository
import com.cpen321.squadup.data.repository.ProfileRepository
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// --- Hilt Test Activity ---
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()

// --- Fake Profile Repository ---
class FakeProfileRepository : ProfileRepository {
    private var dummyUser = User(
        _id = "1",
        email = "leader@example.com",
        name = "Leader User",
        address = Address("123 Main St"),
        transitType = TransitType.DRIVING,
        profilePicture = ""
    )

    override suspend fun getProfile(): Result<User> = Result.success(dummyUser)

    override suspend fun updateProfile(
        name: String,
        address: Address?,
        transitType: TransitType?
    ): Result<User> {
        dummyUser = dummyUser.copy(name = name, address = address, transitType = transitType)
        return Result.success(dummyUser)
    }

    override suspend fun updateProfilePicture(profilePictureUrl: String): Result<User> {
        dummyUser = dummyUser.copy(profilePicture = profilePictureUrl)
        return Result.success(dummyUser)
    }
}

// --- Fake Group Repository ---
class FakeGroupRepository : GroupRepository {
    override suspend fun getGroups(): Result<List<GroupDataDetailed>> = Result.success(emptyList())
    override suspend fun createGroup(
        groupName: String,
        meetingTime: String,
        groupLeaderId: GroupUser,
        expectedPeople: Number,
        activityType: String
    ): Result<GroupData> =
        Result.success(GroupData(GroupDataDetailed(groupName, meetingTime, "JOINCODE123", groupLeaderId, expectedPeople)))

    override suspend fun getGroupByJoinCode(joinCode: String): Result<GroupDataDetailed> =
        Result.success(GroupDataDetailed("Test group", "2025-11-05T14:00:00Z", joinCode, GroupUser("1", "Leader", ""), 5, listOf()))

    override suspend fun deleteGroupByJoinCode(joinCode: String): Result<Unit> = Result.success(Unit)
    override suspend fun joinGroup(joinCode: String, expectedPeople: Number, updatedMembers: List<GroupUser>): Result<Unit> = Result.success(Unit)
    override suspend fun updateGroup(joinCode: String, expectedPeople: Number?, updatedMembers: List<GroupUser>?, meetingTime: String?): Result<Unit> = Result.success(Unit)
    override suspend fun getMidpointByJoinCode(joinCode: String): Result<MidpointActivitiesResponse> = Result.success(MidpointActivitiesResponse(SquadGoal(GeoLocation(0.0, 0.0)), listOf()))
    override suspend fun updateMidpointByJoinCode(joinCode: String): Result<MidpointActivitiesResponse> = Result.success(MidpointActivitiesResponse(SquadGoal(GeoLocation(0.0, 0.0)), listOf()))
    override suspend fun leaveGroup(joinCode: String, userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getActivities(joinCode: String): Result<List<Activity>> = Result.success(emptyList())
    override suspend fun selectActivity(joinCode: String, activity: Activity): Result<Unit> = Result.success(Unit)
    override suspend fun getMidpoints(joinCode: String): Result<List<LatLng>> = Result.success(emptyList())
}

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@MediumTest
class MemberSettingsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private val testGroup = GroupDataDetailed(
        joinCode = "ABC123",
        groupLeaderId = GroupUser("1", "Leader", email = ""),
        groupMemberIds = listOf(
            GroupUser("1", "Leader", address = Address("123 Main St"), transitType = TransitType.DRIVING, email = ""),
            GroupUser("2", "Member", address = Address("UBC"), transitType = TransitType.TRANSIT, email = "")
        ),
        meetingTime = "2025-11-05T14:00:00Z",
        expectedPeople = 5,
        groupName = "Test group"
    )

    private fun createProfileViewModel(): ProfileViewModel =
        ProfileViewModel(FakeProfileRepository(), composeTestRule.activity)

    private fun createGroupViewModel(): GroupViewModel =
        GroupViewModel(FakeGroupRepository())

    @Test
    fun updateMemberAddress() {
        val profileViewModel = createProfileViewModel()
        val groupViewModel = createGroupViewModel()

        composeTestRule.setContent {
            MemberSettingsScreen(
                navController = rememberNavController(),
                group = testGroup,
                profileViewModel = profileViewModel,
                groupViewModel = groupViewModel
            )
        }

        composeTestRule.onNodeWithText("Address").performTextInput("456 New Street")
        composeTestRule.onNodeWithText("Save").assertIsEnabled().performClick()
        composeTestRule.onNodeWithText("Settings saved successfully!").assertIsDisplayed()
    }

    @Test
    fun updateExpectedPeople() {
        val profileViewModel = createProfileViewModel()
        val groupViewModel = createGroupViewModel()

        composeTestRule.setContent {
            MemberSettingsScreen(
                navController = rememberNavController(),
                group = testGroup,
                profileViewModel = profileViewModel,
                groupViewModel = groupViewModel
            )
        }

        composeTestRule.onNodeWithText("Expected People").performTextInput("10")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.onNodeWithText("Settings saved successfully!").assertIsDisplayed()
    }

    @Test
    fun updateTransitType() {
        val profileViewModel = createProfileViewModel()
        val groupViewModel = createGroupViewModel()

        composeTestRule.setContent {
            MemberSettingsScreen(
                navController = rememberNavController(),
                group = testGroup,
                profileViewModel = profileViewModel,
                groupViewModel = groupViewModel
            )
        }

        composeTestRule.onNodeWithText("Transit Type").performClick()
        composeTestRule.onNodeWithText("WALKING").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.onNodeWithText("Settings saved successfully!").assertIsDisplayed()
    }

    @Test
    fun updateEventTime() {
        val profileViewModel = createProfileViewModel()
        val groupViewModel = createGroupViewModel()

        composeTestRule.setContent {
            MemberSettingsScreen(
                navController = rememberNavController(),
                group = testGroup,
                profileViewModel = profileViewModel,
                groupViewModel = groupViewModel
            )
        }

        composeTestRule.onNodeWithText("Update Meeting Date & Time").performClick()
        composeTestRule.onNodeWithText("Meeting Time").assertExists()
    }
}
