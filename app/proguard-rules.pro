# Vosk / JNA
-keep class org.vosk.** { *; }
-keep class com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.** { public *; }
-dontwarn java.awt.*
-dontwarn com.sun.jna.**
# SQLCipher
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }
# whisper.cpp JNI: 클래스·네이티브 메서드 이름이 심볼(Java_kr_voicemate_malitda_stt_WhisperNative_*)과 일치해야 함
-keep class kr.voicemate.malitda.stt.WhisperNative { *; }
-keepclasseswithmembernames,includedescriptorclasses class * { native <methods>; }
