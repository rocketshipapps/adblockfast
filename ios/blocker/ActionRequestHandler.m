//
//  ActionRequestHandler.m
//  blocker
//
//  Created by Brian Kennish on 8/19/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "ActionRequestHandler.h"
#import "Constants.h"

#define RULESET_COMPILE_COUNT_KEY @"RulesetCompileCount"
#define BLOCKER_RULESET_FILENAME @"blockerList"
#define NOOP_RULESET_FILENAME @"noopList"
#define RULESET_FILE_EXTENSION @"json"

@interface ActionRequestHandler ()

@property (nonatomic) NSUserDefaults *preferences;

@end

@implementation ActionRequestHandler

- (NSUserDefaults *)preferences
{
    if (!_preferences) {
        _preferences = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP_ID];
        [_preferences registerDefaults:[NSDictionary
                                           dictionaryWithContentsOfFile:
                                               [[NSBundle mainBundle]
                                                   pathForResource:DEFAULT_PREFERENCES_FILENAME
                                                            ofType:PREFERENCES_FILE_EXTENSION]]];
    }

    return _preferences;
}

- (void)beginRequestWithExtensionContext:(NSExtensionContext *)context
{
    NSUserDefaults *preferences = self.preferences;
    if (![preferences boolForKey:BLOCKER_PERMISSION_KEY])
        [preferences setBool:YES forKey:BLOCKER_PERMISSION_KEY];
    NSItemProvider *attachment =
        [[NSItemProvider alloc]
            initWithContentsOfURL:[[NSBundle mainBundle]
                                      URLForResource:
                                          [preferences boolForKey:BLOCKING_STATUS_KEY] ?
                                              BLOCKER_RULESET_FILENAME : NOOP_RULESET_FILENAME
                                       withExtension:RULESET_FILE_EXTENSION]];
    NSExtensionItem *item = [[NSExtensionItem alloc] init];
    item.attachments = @[attachment];
    [context completeRequestReturningItems:@[item] completionHandler:nil];
    NSInteger rulesetCompileCount = [preferences integerForKey:RULESET_COMPILE_COUNT_KEY];
    [preferences setInteger:++rulesetCompileCount forKey:RULESET_COMPILE_COUNT_KEY];
}

@end
