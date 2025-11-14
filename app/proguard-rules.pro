# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class org.json.** { *; }

# Keep Firestore model classes (POJOs) from being obfuscated
-keepclassmembers class com.smartparking.app.data.model.** {
  <fields>;
  <init>();
}

# Keep Glide generated classes
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$ImageType {
  **[] $VALUES;
  public *;
}

# Keep ZXing classes for QR scanning
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }