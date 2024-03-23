//
//  AppDelegate.m
//  Adblock Fast
//
//  Created by Brian Kennish on 8/19/15.
//  Copyright © 2015, 2016 Rocketship. All rights reserved.
//

#import "AppDelegate.h"
//#import <OneSignal/OneSignal.h>
#import "Constants.h"

@interface AppDelegate ()

@property (nonatomic) NSUserDefaults *preferences;

@end

@implementation AppDelegate

- (NSUserDefaults *)preferences
{
    if (!_preferences) {
        _preferences = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP_ID];
        [_preferences registerDefaults:
            [NSDictionary dictionaryWithContentsOfFile:
                [[NSBundle mainBundle] pathForResource:DEFAULT_PREFERENCES_FILENAME
                                                ofType:PREFERENCES_FILE_EXTENSION]]];
    }

    return _preferences;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
//    [OneSignal initWithLaunchOptions:launchOptions
//                               appId:@"0490dddc-22c0-47b8-b9b2-26bfa035ce0c"
//            handleNotificationAction:nil
//                            settings:@{kOSSettingsKeyAutoPrompt: @NO}];
    [application setMinimumBackgroundFetchInterval:UIApplicationBackgroundFetchIntervalMinimum];
    return YES;
}

- (void)application:(UIApplication *)application
        performFetchWithCompletionHandler:(nonnull void (^)(UIBackgroundFetchResult))completionHandler
{
    [self.preferences integerForKey:NOTIFICATION_REQUEST_COUNT_KEY] ||
        (application.applicationIconBadgeNumber = 1);
    completionHandler(UIBackgroundFetchResultNewData);
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{ if (application.applicationIconBadgeNumber) application.applicationIconBadgeNumber = 0; }

@end
