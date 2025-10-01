# AdMob Setup Guide

This guide will help you set up AdMob advertisements in your Crunchyroll RSS Tracker app securely.

## Prerequisites

1. A Google AdMob account - Sign up at [https://admob.google.com/](https://admob.google.com/)
2. Your app registered in the AdMob console
3. Ad units created for your app

## Step 1: Create AdMob Account and App

1. Go to [AdMob Console](https://apps.admob.com/)
2. Sign in with your Google account
3. Create a new app or select existing app
4. Note down your **App ID** (format: `ca-app-pub-1234567890123456~1234567890`)

## Step 2: Create Ad Units

1. In AdMob console, go to your app
2. Click "Ad units" in the left sidebar
3. Create the following ad units:
   - **Banner ad unit** for main screen advertisements
   - **(Optional) Interstitial ad unit** for full-screen ads

4. Note down the **Ad Unit IDs** (format: `ca-app-pub-1234567890123456/1234567890`)

## Step 3: Configure Your Project

### 3.1 Create AdMob Configuration File

1. Copy the template file:
   ```bash
   cp admob.properties.template admob.properties
   ```

2. Edit `admob.properties` with your actual AdMob IDs:
   ```properties
   # Your actual AdMob App ID
   ADMOB_APP_ID=ca-app-pub-1234567890123456~1234567890
   
   # Your actual Banner Unit ID
   ADMOB_BANNER_UNIT_ID=ca-app-pub-1234567890123456/1234567890
   
   # Optional: Your Interstitial Unit ID
   ADMOB_INTERSTITIAL_UNIT_ID=ca-app-pub-1234567890123456/1234567890
   
   # Enable ads in production
   ADS_ENABLED=true
   ```

### 3.2 Verify Configuration

- ✅ `admob.properties` file exists in project root
- ✅ `admob.properties` is listed in `.gitignore`
- ✅ Real AdMob IDs are in the format `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX`
- ✅ Never commit `admob.properties` to version control

## Step 4: Build and Test

### Debug Builds
- Debug builds automatically use Google's test AdMob IDs
- You should see test ads with "Test Ad" labels
- No real revenue is generated from test ads

### Release Builds
- Release builds use your actual AdMob IDs from `admob.properties`
- Real ads will be displayed to users
- Revenue will be tracked in your AdMob account

### Build Commands
```bash
# Debug build (uses test ads)
./gradlew assembleDebug

# Release build (uses your real AdMob IDs)
./gradlew assembleRelease
```

## Security Features

### What's Protected:
- ✅ Real AdMob IDs are never committed to version control
- ✅ Test IDs are used for debug builds automatically
- ✅ Configuration is loaded from secure properties file
- ✅ Fallback to demo ads if AdMob fails or isn't configured

### File Structure:
```
project-root/
├── admob.properties.template  (committed - template only)
├── admob.properties          (NOT committed - your real IDs)
├── .gitignore               (includes admob.properties)
└── app/
    ├── build.gradle         (reads from admob.properties)
    └── src/main/
        ├── AndroidManifest.xml (declares AdMob app ID)
        └── java/.../AdManager.kt (handles ad loading)
```

## Testing Your Setup

### 1. Verify AdMob Configuration
Add this to your MainActivity's `onCreate()` method for debugging:
```kotlin
val adManager = AdManager(this)
Log.d("AdMob", adManager.getAdConfigInfo())
```

### 2. Check Ad Loading
- **Debug build**: Should show "Test Ad" labels
- **Release build**: Should show real ads (if properly configured)
- **Fallback**: Shows demo ads if AdMob fails

### 3. Common Issues

| Issue | Solution |
|-------|----------|
| "AdMob IDs not configured" | Check `admob.properties` file exists and has correct format |
| Test ads in release build | Verify `admob.properties` has real IDs, not test IDs |
| No ads showing | Check internet connection and AdMob account status |
| Build errors | Ensure `admob.properties` exists or build will use placeholders |

## Ad Revenue and Analytics

### AdMob Console
- View revenue, impressions, and click-through rates
- Access at [https://apps.admob.com/](https://apps.admob.com/)
- Revenue reports typically update within 24 hours

### App Analytics
The `AdManager` class logs ad events:
- Ad loads, failures, and clicks
- Configuration status
- Error messages

### Best Practices
1. **Don't click your own ads** - This can get your AdMob account suspended
2. **Use test devices** - Register your test devices in AdMob console
3. **Monitor performance** - Check fill rates and eCPM regularly
4. **Respect user experience** - Don't overload with ads

## Troubleshooting

### Debug Information
To check if everything is configured correctly:
```kotlin
// In your Activity
val adManager = AdManager(this)
if (adManager.isAdMobConfigured()) {
    Log.d("AdMob", "✅ AdMob is properly configured")
} else {
    Log.w("AdMob", "❌ AdMob configuration issues detected")
}
Log.d("AdMob", adManager.getAdConfigInfo())
```

### Common Errors
1. **"Invalid Ad Unit ID"** - Check your ad unit IDs in AdMob console
2. **"App ID missing"** - Verify AndroidManifest.xml has the meta-data entry
3. **"No ads available"** - Normal for new AdMob apps, ads will appear as inventory builds

### Support
- [AdMob Help Center](https://support.google.com/admob/)
- [AdMob Developer Documentation](https://developers.google.com/admob/android)
- Check the `AdManager.kt` logs for detailed error messages

## Updating AdMob IDs

To change your AdMob IDs later:
1. Update `admob.properties` with new IDs
2. Clean and rebuild the project:
   ```bash
   ./gradlew clean
   ./gradlew assembleRelease
   ```
3. Test thoroughly before deploying

Remember: Never commit your real AdMob IDs to version control!