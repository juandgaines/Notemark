# NoteMark_Milestone_3_Requirements_1752679738242.pdf
<!-- Auto-extracted from milestones/milestone-3/NoteMark_Milestone_3_Requirements_1752679738242.pdf -->

## Page 1

NoteMark Milestone #3 Requirements
This requirements document explains NoteMark's Milestone #3 requirements.
For each milestone, a new requirements document and an updated Figma file will
be created. You can find all current milestones in the members area.

The mockups show the exact look and colors of a specific UI element. The app
has a single theme, but no light or dark theme.

Note that this serves to give you an overall impression of what the app should be
able to do. Feel free to decide how you implement specific things (e.g., how you
display a specific loading progress or how you specify error messages).

You can find the mockups for NoteMark here:
https://www.figma.com/design/3eLOJSbYh6HVwIYVcqR5YE/NoteMark?
node-id=9496-3694
Milestone #3 Goal
Modes Unlocked: Make NoteMark easier to read, write, and customize.

In this milestone, we make NoteMark better by completing the Note Detail
Screen. Users will be able to switch between three modes: Edit, View, and
Reader (for distraction-free reading). We will also add a Settings Screen, where
users can change app options and make NoteMark fit their needs. The goal is to
let users move easily between writing and reading, and to give them more control
over how the app works.
Icons
All icons for the app can be Material design icons or taken from the mockups as
SVG (in case an equivalent Material icon doesnʼt exist).
Adaptive Layouts
ll screens must look good and work well on phones and tablets, in both portrait
and landscape. All UI requirements apply to each device, and unique differences
will be explicitly mentioned.
NoteMark Milestone #3 Requirements 1

## Page 2

Technical Requirements
▼ Note List Screen (updated from Milestone #2)
Add a new icon to the toolba 
When tapped, navigate to the Settings Screen
▼ Settings Screen
Contains a title text: “Settings” with a back chevron ico 
On back chevron tap, navigate pop this screen from the backstac 
Settings item # 
Custom icon with text: “Log out 
Text and icon are in re 
On tap, immediately log user ou 
Logging a user out means 
Clearing the local database of all the user’s note 
Clearing any session tokens (usually saved in shared preferences or
data store 
Call the logout API endpoin 
With all that complete, clear the app’s backstack and take the user back
to the Login Screen
Logout

Endpoint: /api/auth/logout

Method: POST
Description: Log out by invalidating the provided refresh token.

Request body: 

{
"refreshToken": "<token>"

}


Successful logout response: status code 200 with no body.
NoteMark Milestone #3 Requirements 2

## Page 3

▼ Note Detail Screen (updated from Milestone #2

This screen will now consist of three (3) different states. We’ve already
implemented one of the states in Milestone #2: Edit Mode. An extended FAB is
introduced to help toggle between each mode and show the user which mode
they are currently in 
View Mod 
This is the default state when the user navigates to the Note Detail Scree 
At the top is the back chevron icon and the title “All Notes 
Tapping the chevron icon should pop the Note Detail screen from the
backstac 
In landscape mode, the icon and title are placed to the left of the main
content of the screen.
View Mode - Landscape
Note Metadat 
Located below the note titl 
Text content has a horizontal divider at the top and botto 
Note meta data content is organised in a ro 
“Date created” with the date the note was create 
“Last edited” with the date the note was last edite 
The date is formatted as: dd MMM yyyy, HH:mm
NoteMark Milestone #3 Requirements 3

## Page 4

dd - Day of month (2 digits, with leading zero if needed 
MMM - Short month name (Jan, Feb, Mar 
yyyy - 4-digit yea 
HH - Hour in 24-hour format (00-23 
mm - Minute (00-59 
If either date is less than 5 minutes ago, replace the date with the
text: “Just now 
Neither date values need to update in real-tim 
Below note metadata content is the note content tex 
None of the text is editable in this mod 
Extended FAB is visible at the bottom center of the scree 
Contains two icon buttons. Each can switch the screen into a different
mod 
Pencil icon - on tap, switch to Edit Mod 
Book icon - on tap, switch to Reader Mode
View Mode - Portrait
View Mode - Tablet
NoteMark Milestone #3 Requirements 4

## Page 5

Reader Mod 
To enter this mode, tap the Book icon in the Extended FA 
On mobile devices (sw < 600 dp) programmatically set the orientation to
landscape
You can programmatically change the device orientation! 
Checkout the Activity's requestedOrientation property.
Once the user enters Reader Mode, the following should happen 
Highlight the book icon: The book icon in the Extended FAB becomes
visually highlighted to indicate Reader Mode is active 
Fade Out Additional UI Elements: The following elements should
gradually fade out and become invisible 
The back icon chevron with “All Notes” titl 
The Extended FA 
Show Additional UI Elements on Tap: Tapping anywhere on the screen
should bring the additional UI elements back with a fade-in animation 
Auto-Hide Behavior: Once visible, the additional UI elements should
automatically fade out again when 
5 seconds have passed since user tapped screen to make them
visibl 
The user starts scrolling the note content (only applicable if the
content is scrollable on the device) 
The user taps anywhere on the screen during visibility 
Note: If the user taps while the UI elements are visible, it should
immediately hide them and cancel any ongoing timer for auto-
hiding after 5 seconds 
Tapping the back chevron icon and “All Notes” title pops the Note Detail
screen from the backstac 
Tapping the highlighted book icon will exit Reader Mode and enter View
Mode
Remember to reset the requested orientation! Also, don’t
mistakenly set it fixed to Portrait mode either
NoteMark Milestone #3 Requirements 5

## Page 6

Tapping the pencil icon switches to Edit Mod
Reader Mode (Additional UI elements visible) - Tablet
Reader Mode (Additional UI elements hidden) - Phone
Edit Mode (updated from Milestone #2 
Replace the back chevron and title text with an “x” ico 
On “x” tap 
If the title or note content is different from the original note then show a
confirmation dialo 
Title: “Discard Changes?”
NoteMark Milestone #3 Requirements 6

## Page 7

Body: “You have unsaved changes. If you discard now, all changes
will be lost. 
Confirmation button: “Discard 
Cancel button: “Keep Editing 
On confirm button tap, do not save the note and switch to View
Mod 
If no changes to the note was made, switch to View Mod 
“Save Note” text at top right corner of scree 
On tap 
Update the note with the edits made by the user and switch to View
Mod 
Once the note has been saved, switch to View Mod 
User should now see the updated note with the “Last Edited” value set
to “Just now 
Hide the Extended FAB
Edit Mode - Landscape
NoteMark Milestone #3 Requirements 7

## Page 8

Edit Mode - Portrait
Edit Mode - Tablet
NoteMark Milestone #3 Requirements 8

