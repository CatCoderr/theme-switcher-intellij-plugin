#import "me_catcoder_themeswitcher_plugin_mac_MacOSDarkMode.h"
#import <Foundation/Foundation.h>

JNIEXPORT jboolean JNICALL Java_me_catcoder_themeswitcher_plugin_mac_MacOSDarkMode_isDarkModeEnabled
  (JNIEnv * env, jclass clazz){

     NSString *osxMode = [[NSUserDefaults standardUserDefaults] stringForKey:@"AppleInterfaceStyle"];

     return osxMode != nil;
}
