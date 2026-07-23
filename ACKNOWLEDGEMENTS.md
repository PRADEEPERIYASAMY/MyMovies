# Acknowledgements

MyMovies is built on the shoulders of excellent open-source libraries and publicly available services. This document credits the tools, libraries, and people that made this project possible.

---

## Libraries & Frameworks

| Library | Purpose | License |
|---|---|---|
| [Kotlin](https://kotlinlang.org/) | Primary application language | Apache 2.0 |
| [Hilt](https://dagger.dev/hilt/) | Dependency injection framework | Apache 2.0 |
| [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Asynchronous programming | Apache 2.0 |
| [Retrofit2](https://square.github.io/retrofit/) | HTTP client for movie catalog API | Apache 2.0 |
| [OkHttp](https://square.github.io/okhttp/) | Underlying HTTP engine + logging | Apache 2.0 |
| [Gson](https://github.com/google/gson) | JSON serialization/deserialization | Apache 2.0 |
| [Room](https://developer.android.com/training/data-storage/room) | Local SQLite database / cache layer | Apache 2.0 |
| [ExoPlayer](https://github.com/google/ExoPlayer) | Video playback engine | Apache 2.0 |
| [TorrentStream-Android](https://github.com/TorrentStream/TorrentStream-Android) | BitTorrent streaming engine | Apache 2.0 |
| [open-subtitles-android](https://github.com/vanitas92/open-subtitles-android) | OpenSubtitles API client for `.srt` retrieval | Apache 2.0 |
| [Glide](https://github.com/bumptech/glide) | Image loading and caching | BSD, MIT, Apache 2.0 |
| [Picasso](https://square.github.io/picasso/) | Legacy image loading (being phased out) | Apache 2.0 |
| [Dexter](https://github.com/Karumi/Dexter) | Runtime permission handling | Apache 2.0 |
| [AndroidX Biometric](https://developer.android.com/jetpack/androidx/releases/biometric) | Biometric authentication | Apache 2.0 |
| [Navigation Component](https://developer.android.com/guide/navigation) | Single-Activity Fragment navigation | Apache 2.0 |
| [Material Components](https://github.com/material-components/material-components-android) | UI component library | Apache 2.0 |
| [CircleImageView](https://github.com/hdodenhof/CircleImageView) | Circular image view widget | Apache 2.0 |
| [android-gif-drawable](https://github.com/koral--/android-gif-drawable) | GIF rendering in Android views | MIT |
| [DiscreteScrollView](https://github.com/yarolegovich/DiscreteScrollView) | Scrollable list with item selection | MIT |
| [Firebase Crashlytics](https://firebase.google.com/products/crashlytics) | Crash reporting | Google Terms of Service |
| [Firebase Analytics](https://firebase.google.com/products/analytics) | Usage analytics | Google Terms of Service |

---

## Data Sources & Services

- **Third-party movie catalog API** for movie listings, metadata, and torrent magnet links.
- **OpenSubtitles** for community-contributed subtitle search and download.

---

## Inspiration & Reference Material

- The Android architecture guidelines from [developer.android.com](https://developer.android.com/topic/architecture) informed the MVVM + Repository layer structure.
- The cache-as-source-of-truth pattern is drawn from the [Guide to App Architecture](https://developer.android.com/topic/architecture/data-layer).

---

Made with love by [Pradeep Periyasamy](https://github.com/PRADEEPERIYASAMY) ❤️
