//
//  Constants.h
//  adblockfast
//
//  Created by Brian Kennish on 8/19/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#ifndef ADBLOCKFAST_CONSTANTS

#define ADBLOCKFAST_CONSTANTS
#define VERBOSE NO
#define REVERSE_DOMAIN_NAME @"com.rocketshipapps"
#define APP_GROUP_ID (@"group." REVERSE_DOMAIN_NAME @".blocker")
#define DEFAULT_PREFERENCES_FILENAME @"Defaults"
#define PREFERENCES_FILE_EXTENSION @"plist"
#define BLOCKER_PERMISSION_KEY @"IsBlockerAllowed"
#define BLOCKING_STATUS_KEY @"IsBlockingOn"
#define STATUS_LABEL @"Status: "
#define TAB_BAR_BUTTON_COUNT 3
#define OVERLAY_BORDER_WIDTH 1
#define DARK_COLOR_R (44. / 255)
#define DARK_COLOR_G (44. / 255)
#define DARK_COLOR_B (44. / 255)
#define OVERLAY_ALPHA .9
#define FRAME_WIDTH_TO_MARGIN (128. / 3)
#define MINIMUM_FRAME_DIMENSION_TO_TAB_BAR_HEIGHT (32. / 7)
#define MARGIN_TO_CORNER_RADIUS 1.5

#endif
