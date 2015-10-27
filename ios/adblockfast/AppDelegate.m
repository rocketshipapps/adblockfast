//
//  AppDelegate.m
//  adblockfast
//
//  Created by Brian Kennish on 8/19/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "AppDelegate.h"
#import "Constants.h"

@interface AppDelegate ()
@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

    //Background fetch for badge count update
    [[UIApplication sharedApplication] setMinimumBackgroundFetchInterval:UIApplicationBackgroundFetchIntervalMinimum];
    
    // Parse ID
    [Parse setApplicationId:@"c4BdheEFIaDAXBQu7ZtRmDNR2WZHnyyOlzIy5V54"
                  clientKey:@"YehK1OwFeDMpKABYEGAYiRDgGIUOc857pEBp7oXS"];
    
    if (application.applicationState != UIApplicationStateBackground) {
        // Track an app open here if we launch with a push, unless "content_available" was used to trigger a background push (introduced in iOS 7). In that case, we skip tracking here to avoid double counting the app-open.
        BOOL preBackgroundPush = ![application respondsToSelector:@selector(backgroundRefreshStatus)];
        BOOL oldPushHandlerOnly = ![self respondsToSelector:@selector(application:didReceiveRemoteNotification:fetchCompletionHandler:)];
        BOOL noPushPayload = ![launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
        if (preBackgroundPush || oldPushHandlerOnly || noPushPayload) {
            [PFAnalytics trackAppOpenedWithLaunchOptions:launchOptions];
        }
    }
    
    return YES;
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    
    // Store the deviceToken in the current installation and save it to Parse.
    PFInstallation *currentInstallation = [PFInstallation currentInstallation];
    [currentInstallation setDeviceTokenFromData:deviceToken];
    currentInstallation.channels = @[@"global"];
    currentInstallation[@"osVersion"] = [UIDevice currentDevice].systemVersion;
    [currentInstallation saveInBackground];

}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    [PFPush handlePush:userInfo];
    
    if (application.applicationState == UIApplicationStateInactive) {
        // The application was just brought from the background to the foreground, so we consider the app as having been "opened by a push notification."
        [PFAnalytics trackAppOpenedWithRemoteNotificationPayload:userInfo];
    }
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    
    if (application.applicationState == UIApplicationStateInactive) {
        [PFAnalytics trackAppOpenedWithRemoteNotificationPayload:userInfo];
    }
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
 
    //Parse: reset badge count
    PFInstallation *currentInstallation = [PFInstallation currentInstallation];
    if (currentInstallation.badge != 0) {
        currentInstallation.badge = 0;
        [currentInstallation saveEventually];
    }
}

- (void)application:(UIApplication *)application performFetchWithCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
    NSUserDefaults *preferences = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP_ID];
    NSInteger notificationRequestCount = [preferences integerForKey:@"NotificationRequestCount"];
    
    NSLog(@"########### >> Notification count = %lu", notificationRequestCount);
    // Show this badge number update only to old users who have never received a notification.
    if (notificationRequestCount == 0) {
        [UIApplication sharedApplication].applicationIconBadgeNumber = 1;
    }
    completionHandler(UIBackgroundFetchResultNewData);
}

@end
