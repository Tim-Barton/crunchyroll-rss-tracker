# Notification Debugging Guide

This guide will help you diagnose and fix issues with notifications not triggering in the Crunchyroll RSS Tracker app.

## Common Issues and Solutions

### 1. **No Notifications Appearing**

#### Check Notification Permission
- Go to Settings → "View Sync Status"
- Verify "Notification Permission: Granted"
- If denied, go to Settings → "Enable Notifications"

#### Check Background Sync Status
- Open app Settings menu
- Select "View Sync Status"
- Verify:
  - ✓ Enabled: Yes
  - ✓ Notification Permission: Granted
  - ✓ Saved Titles: > 0
  - ✓ Last Sync: Recent timestamp

### 2. **First Time Setup**

For notifications to work, you need:
1. **Grant notification permission** (Android 13+)
2. **Add anime series to favorites** (at least 1 saved title)
3. **Enable background sync** (should be automatic when you have favorites)
4. **Wait for sync** (runs every 24 hours, or force sync manually)

### 3. **Testing Notifications**

#### Quick Test (Recommended)
1. Open app → Menu (⋮) → Settings
2. Select "Send Test Notification"
3. You should see a notification immediately
4. If this works, your notification setup is correct

#### Full Sync Test
1. Open app → Menu (⋮) → Settings
2. Select "Clear Episode History" → Confirm
3. Select "Force Sync Now"
4. Wait 10-30 seconds
5. Check your notification tray
6. **Note**: This will treat ALL current episodes as new!

### 4. **Viewing Debug Logs**

To see detailed sync logs, use Android Studio or ADB:

```bash
adb logcat -s RssCheckWorker:D
```

You'll see logs like:
```
=== RssCheckWorker started ===
Notification permission: OK
Saved titles count: 3
Saved title: 'One Piece' (id: one_piece)
Fetching RSS feed...
RSS fetch successful - 50 episodes found
New episodes found: 2
✓ NEW EPISODE DETECTED: One Piece - Episode 1090
Sending multiple episodes notification: 2 episodes
=== RssCheckWorker completed successfully ===
```

### 5. **Why Episodes Might Not Be "New"**

The app tracks episode IDs to avoid duplicate notifications. An episode is considered "new" if:
1. Its episode ID hasn't been seen before, OR
2. Its publish date is after the last sync time

**Common Issue**: If you run the sync multiple times quickly, episodes won't trigger notifications again because their IDs are already stored.

**Solution**: Use "Clear Episode History" to reset this tracking.

### 6. **Title Matching Issues**

Notifications only trigger for series you've saved as favorites. The matching is case-insensitive and trimmed.

#### Debug Title Matching
Check logs for:
```
Saved title map keys: [one piece, naruto, attack on titan]
Episode #0 matches saved title: 'One Piece' -> 'One Piece'
Episode #1 no match: 'Bleach' (key: 'bleach')
```

If episodes aren't matching:
1. Check the exact series name in your favorites
2. Check the exact series name in the RSS feed
3. They must match (after lowercase + trim)

**Example**: If RSS has "One Piece (Dub)" but you saved "One Piece", they won't match.

### 7. **Background Sync Timing**

The background sync runs:
- **Automatically**: Every 24 hours
- **Constraints**: Requires internet connection + battery not low
- **Flex window**: Can execute within ±15 minutes of scheduled time

**Important**: Android may delay background tasks based on:
- Battery optimization settings
- Doze mode
- App standby buckets

#### Force Immediate Sync
Use Settings → "Force Sync Now" to bypass the 24-hour wait.

### 8. **Android Battery Optimization**

If notifications work with "Force Sync Now" but not automatically:

1. Go to Android Settings
2. Apps → Crunchyroll RSS Tracker
3. Battery → Unrestricted (or disable battery optimization)
4. This allows background work to run on schedule

### 9. **Checking WorkManager Status**

Use "View Sync Status" to see:
- When the last sync occurred
- Which series are being tracked
- Current notification permission status

### 10. **RSS Feed Issues**

If the worker is running but finding 0 episodes:

Check logs for:
```
RSS fetch successful - 0 episodes found
```

This means:
- The RSS feed is empty (unlikely)
- Parsing failed (check for exceptions)
- Network issue (check internet connection)

## Step-by-Step Debugging Process

1. **Send Test Notification**
   - If this fails → Notification permission issue
   - If this works → Continue to step 2

2. **Check Sync Status**
   - Open "View Sync Status"
   - Verify all checkboxes are met
   - Note the "Last Sync" time

3. **Clear History & Force Sync**
   - Clear Episode History
   - Force Sync Now
   - Wait 30 seconds
   - Check notifications

4. **Check Logs**
   ```bash
   adb logcat -s RssCheckWorker:D NotificationHelper:D
   ```
   - Look for "NEW EPISODE DETECTED"
   - Look for "Sending notification"

5. **Verify Title Matching**
   - Check log for "Saved title map keys"
   - Compare with "Episode #X matches/no match"
   - Ensure your saved titles match RSS feed titles exactly

6. **Wait for Automatic Sync**
   - If manual sync works, wait 24 hours
   - Check battery optimization settings
   - Verify app isn't force-stopped

## Troubleshooting Checklist

- [ ] Notification permission granted
- [ ] At least 1 anime series saved as favorite
- [ ] Background sync enabled
- [ ] Internet connection available
- [ ] Battery not critically low
- [ ] App not force-stopped
- [ ] Battery optimization disabled (optional, helps reliability)
- [ ] Test notification works
- [ ] Force sync works
- [ ] Saved title names match RSS feed exactly

## Advanced Debugging

### Check SharedPreferences
```bash
adb shell run-as com.example.crunchyrollwatcher cat /data/data/com.example.crunchyrollwatcher/shared_prefs/rss_check_prefs.xml
```

### Check WorkManager Database
```bash
adb shell dumpsys jobscheduler | grep -A 20 "com.example.crunchyrollwatcher"
```

### Force WorkManager to Run
```bash
adb shell am broadcast -a "androidx.work.diagnostics.REQUEST_DIAGNOSTICS" --es "androidx.work.tag" "rss_check_work_tag"
```

## Still Not Working?

If none of the above helps:

1. Check logs for exceptions or errors
2. Verify the RSS feed is accessible: https://www.crunchyroll.com/rss
3. Check if other apps can send you notifications
4. Try reinstalling the app (will clear all data)
5. Check Android version compatibility (needs Android 8.0+)

## Log Examples

### Successful Sync with Notifications
```
D/RssCheckWorker: === RssCheckWorker started ===
D/RssCheckWorker: Notification permission: OK
D/RssCheckWorker: Saved titles count: 2
D/RssCheckWorker: Saved title: 'Frieren: Beyond Journey's End' (id: frieren_beyond_journey_s_end)
D/RssCheckWorker: Fetching RSS feed...
D/RssCheckWorker: RSS fetch successful - 50 episodes found
D/RssCheckWorker: Last check time: 1234567890 (Jan 01, 2024 12:00:00)
D/RssCheckWorker: Episode #5 matches saved title: 'Frieren: Beyond Journey's End'
D/RssCheckWorker:   Episode: Episode 28 | ID: xyz123 | IsNew: true
I/RssCheckWorker:   ✓ NEW EPISODE DETECTED: Frieren: Beyond Journey's End - Episode 28
D/RssCheckWorker: New episodes found: 1
I/RssCheckWorker: Sending single episode notification
D/RssCheckWorker: === RssCheckWorker completed successfully ===
```

### No Saved Titles
```
D/RssCheckWorker: === RssCheckWorker started ===
D/RssCheckWorker: Notification permission: OK
D/RssCheckWorker: Saved titles count: 0
W/RssCheckWorker: No saved titles - skipping check
```

### Permission Denied
```
D/RssCheckWorker: === RssCheckWorker started ===
W/RssCheckWorker: Notification permission not granted - skipping check
```

### No New Episodes
```
D/RssCheckWorker: === RssCheckWorker started ===
D/RssCheckWorker: RSS fetch successful - 50 episodes found
D/RssCheckWorker: New episodes found: 0
D/RssCheckWorker: No new episodes to notify about
D/RssCheckWorker: === RssCheckWorker completed successfully ===
```
