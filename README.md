# Volleyverse

This is an Android application for the Volleyverse website.

## How to Run

Development and testing has taken place in Android Studio. The application can be easily deployed on an
emulator or a physical device through Android Studio. Thus far, the application has only been tested on
an Android emulator (AVD) running Android 6.0 with API 23.

## Current State

The application is currently able to load and refresh posts.

### Screenshots

Splash screen

![Splash screen](screenshots/splash.png)

Main posts view

![Posts view](screenshots/main.png)

Article view

![Article view](screenshots/article.png)

Navigation drawer

![Navigation drawer](screenshots/navdrawer.png)

"Static page" (e.g. About Us)

![About Us page](screenshots/navpage.png)

## Next Steps

- Add a feedback activity
- Show a clean WebView to the user immediately (get rid of the lag from removing tags manually via javascript)
- Implement offline loading using WebView caching mechanisms
- Add push functionality and a settings activity to allow the user to control push subscriptions
- Tweak color schemes/look and feel
- Try using Fragments
- Test on different platforms/versions
