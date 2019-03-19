#import "MacOSDarkMode.h"
#import <Foundation/Foundation.h>

BOOL isDarkMode() {
    NSString *osxMode = [[NSUserDefaults standardUserDefaults] stringForKey:@"AppleInterfaceStyle"];

    return osxMode != nil;
}

JNIEXPORT jboolean JNICALL Java_MacOSDarkMode_isDarkModeEnabled
   (JNIEnv *env , jobject obj) {

   return isDarkMode();
}
