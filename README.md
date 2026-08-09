# Capture Request Injector

An Xposed/LSPosed module that allows you to override camera capture request parameters on Android devices. This module hooks into the Camera2 API and lets you modify capture settings like exposure time, ISO, noise reduction mode, and more for individual camera apps.

## Features

- **Global Rules**: Apply camera parameter overrides to all camera apps
- **Per-App Rules**: Configure specific overrides for individual camera applications
- **Priority Apps**: Camera apps from scope.list are shown at the top for quick access
- **Quick Settings**: Toggle global rules or disable all rules from Quick Settings tiles

## How to Use

1. **Installation**
   - Install the APK on your device
   - Open LSPosed Manager
   - Enable the module for your target camera apps

2. **Configure Rules**
   - Open the Capture Request Injector app
   - Tap "Global Rules" at the top to set default rules for all cameras
   - Tap the "+" button to add a specific camera app
   - Select the app from the list (camera apps are prioritized at the top)
   - Tap on the app to configure its specific rules
   - Add rules to override camera parameters

3. **Included Parameters**
   - Exposure time
   - ISO sensitivity
   - Noise reduction mode
   - Edge enhancement mode
   - Shading mode
   - And many more Camera2 capture request keys

4. **Quick Settings**
   - Add the "Disable Global Rules" and "Disable All Rules" tiles to Quick Settings
   - Quickly toggle rules without opening the app

## How It Works

The module hooks into `CaptureRequest.Builder.build()` in target camera apps. When a capture request is built, the module applies configured rules to override specific parameters. Rules are stored in a JSON file in external storage and read by the hook in the target app's process.

## Scope

The module is configured to work with popular camera apps including:
- Google Camera (GCam)
- MGC (Modded Google Camera)
- LineageOS Aperture
- Samsung Camera
- And many other camera-related apps

See `app/src/main/resources/META-INF/xposed/scope.list` for the full list.

## Building

1. Clone the repository
2. Open in Android Studio
3. Build the APK
4. Install on your device with LSPosed

## Requirements

- Android device with LSPosed or Xposed framework
- Android 10.0+ (API level 29+)
- Camera2 API support in target apps

## Credits

- Built with [libxposed](https://github.com/libxposed/api)
- Uses Material Design components
- Icon by [Material Icons](https://fonts.google.com/icons)

## License

See LICENSE file for details.

## Contributing

Contributions are welcome! Feel free to submit issues and pull requests.
