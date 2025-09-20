# Requirements and Design

## 1. Change History

| **Change Date**   | **Modified Sections** | **Rationale** |
| ----------------- | --------------------- | ------------- |
| _Nothing to show_ |

---

## 2. Project Description

[WRITE_PROJECT_DESCRIPTION_HERE]

This project is an Android application designed to simplify group meetups by helping large groups find a convenient meeting point for activities. The app allows a group admin to create a group, set the desired meetup time, specify a radius, and choose activity categories. Group members then join by entering their starting locations, which are used to calculate a fair midpoint for everyone. Based on the midpoint and the chosen activity types, the app generates a list of suggested venues or activities within the defined radius, making it easier for groups with members coming from different places to quickly agree on a convenient and enjoyable location.

---

## 3. Requirements Specification

### **3.1. List of Features**
1. Authentication: To access app features, the user must user Google Authentication service to Sign Up. If a user does not have a pre-existing account for the app, they must use Sign Up before Signing In. An authenticated user can also Sign Out of the app.

2. Manage Group: A user can create a group to organize a meetup. The group can be configured with a name, preferred activities/venues, a radius for the midpoint to extend from and a pre-determined meet-up time. After creating the group, the group owner can view the group details, such as member list, their title ("Admin"), and the invitation code. The creator of the group has the ability to change and modify group meeting times, and the radius of the meetup area from the midpoint.

3. Manage Group Membership: When a user joins the group using the invitation code, they are prompted for the location they will be commuting from to the meetup, and their mode of transportation. As users join, the midpoint for the group meetup is calculated and updated, and a list of venues/activities are displayed in list form using an API call to Google Maps. 

4. Manage Options: The list of venue options can be filtered based on attributes.

### **3.2. Use Case Diagram**

### **3.3. Actors Description**
#TODO: IDK if User should be one of the actors since i mentioned as a primary actor for use case 1
1. **[Group Admin]**: User who creates a group, manages its settings, and oversees member participation.
2. **[Group Member]**: User who joins existing groups, provides location/transportation info.
3. **[Authentication Service (Google)]**: External system providing login, signup, and identity verification.
4. **[Maps API (Google Maps/Places)]**: External system providing activity/venue suggestions.

### **3.4. Use Case Description**
- Use cases for feature 1: [Authentication]
1. **[Sign Up]**: User registers with Google.
2. **[Sign In/Out]**: User signs in with existing account or logs out.
- Use cases for feature 2: [Manage_Group]
3. **[Create Group]**: Admin configures a group with name, activities, time, and radius.
4. **[Edit Group Settings]**: Admin modifies radius, time, or activity categories.
- Use cases for feature 3: [Manage_Membership]
5. **[Join Group]**: Member enters invitation code and location/transportation.
4. **[View Suggested Venues]**: Member sees venue/activity list dynamically updated.
- Use cases for feature 4: [Manage_Options]
3. **[WRITE_NAME_HERE]**: ...
4. **[WRITE_NAME_HERE]**: ...


### **3.5. Formal Use Case Specifications (5 Most Major Use Cases)**
<a name="uc1"></a>

#### Use Case 1: [User Authentication]

**Description**: User signs up or logs in with Google to access the app.

**Primary actor(s)**: User, Authentication Service.
    
**Main success scenario**:
1. User opens app.
2. User selects "Sign In with Google".
3. Google Authentication verifies identity.
4. User is logged into app.

**Failure scenario(s)**:
- 1a. ...
    - 1a1. ...
    - 1a2. ...

- 1b. ...
    - 1b1. ...
    - 1b2. ...
                
- 2a. ...
    - 2a1. ...
    - 2a2. ...

...

<a name="uc2"></a>

#### Use Case 2: [Create Group]
**Description**: Admin creates a group for meetup planning.

**Primary actor(s)**: Group Admin.
    
**Main success scenario**:
1. Admin selects "Create Group".
2. Admin enters group name, activities, radius, time.
3. System generates invitation code.
4. Group is created successfully.

**Failure scenario(s)**:
- 1a. ...
    - 1a1. ...
    - 1a2. ...

- 1b. ...
    - 1b1. ...
    - 1b2. ...
                
- 2a. ...
    - 2a1. ...
    - 2a2. ...

...

<a name="uc3"></a>

#### Use Case 2: [Join Group]
**Description**: Member joins a group with invitation code.

**Primary actor(s)**: Group Member.
    
**Main success scenario**:
1. Member enters invitation code.
2. Member inputs location and mode of transport.
3. System validates code.
4. Member is added to group.

**Failure scenario(s)**:
- 1a. ...
    - 1a1. ...
    - 1a2. ...

- 1b. ...
    - 1b1. ...
    - 1b2. ...
                
- 2a. ...
    - 2a1. ...
    - 2a2. ...

...

<a name="uc4"></a>

#### Use Case 2: [View Suggested Venues]
**Description**: Group members view recommended venues near midpoint.

**Primary actor(s)**: Group Member, Maps API.
    
**Main success scenario**:
1. System calculates midpoint.
2. System fetches nearby venues based on activities.
3. List is displayed to all members.

**Failure scenario(s)**:
- 1a. ...
    - 1a1. ...
    - 1a2. ...

- 1b. ...
    - 1b1. ...
    - 1b2. ...
                
- 2a. ...
    - 2a1. ...
    - 2a2. ...

...

<a name="uc5"></a>

#### Use Case 2: [Manage Group Settings]
**Description**: Admin modifies radius, time, or activity categories.

**Primary actor(s)**: Group Admin.
    
**Main success scenario**:
1. Admin selects group.
2. Admin edits details.
3. System updates group info and recalculates venues.

**Failure scenario(s)**:
- 1a. ...
    - 1a1. ...
    - 1a2. ...

- 1b. ...
    - 1b1. ...
    - 1b2. ...
                
- 2a. ...
    - 2a1. ...
    - 2a2. ...

...

### **3.6. Screen Mock-ups**


### **3.7. Non-Functional Requirements**
<a name="nfr1"></a>

1. **[WRITE_NAME_HERE]**
    - **Description**: ...
    - **Justification**: ...
2. ...

---

## 4. Designs Specification
### **4.1. Main Components**
1. **[Authentication Module]**
    - **Purpose**: Handle Google Sign In/Out.
    - **Interfaces**: 
        1. Google Auth API
            - **Purpose**: Verifies users.
        2. App Session Manager
            - **Purpose**: Stores login state.
2. **[Group Management Module]**
    - **Purpose**: Create/edit groups.
    - **Interfaces**: 
        1. Database
            - **Purpose**: Store group info.
        2. UI
            - **Purpose**: Display group details.
2. **[Maps & Midpoint Module]**
    - **Purpose**: Calculate midpoint, fetch venues.
    - **Interfaces**: 
        1. Google Maps/Places API
            - **Purpose**: Provide venues.


### **4.2. Databases**
1. **[User DB]**
    - **Purpose**: Store user profiles, Google IDs.
2. **[Group DB]**
    - **Purpose**: Store group details, members, activities.


### **4.3. External Modules**
1. **[Google Maps API]** 
    - **Purpose**: Midpoint and venue suggestions.
2. **[Google Authentication]** 
    - **Purpose**: Secure user login.
3. **[WRITE_NAME_HERE]** 
    - **Purpose**: ...


### **4.4. Frameworks**
1. **[Android SDK (Kotlin/Java)]**
    - **Purpose**: Mobile app development.
    - **Reason**: Native Android support.
2. **[WRITE_NAME_HERE]**
    - **Purpose**: ...
    - **Reason**: ...


### **4.5. Dependencies Diagram**


### **4.6. Use Case Sequence Diagram (5 Most Major Use Cases)**
1. [**[WRITE_NAME_HERE]**](#uc1)\
[SEQUENCE_DIAGRAM_HERE]
2. ...


### **4.7. Design and Ways to Test Non-Functional Requirements**
1. [**[WRITE_NAME_HERE]**](#nfr1)
    - **Validation**: ...
2. ...
