//
//  Button.m
//  adblockfast
//
//  Created by Brian Kennish on 10/9/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "Button.h"
#import "Constants.h"

@implementation Button

- (Button *)initWithFrameSize:(CGSize)frameSize
        minimumFrameDimension:(CGFloat)minimumFrameDimension
                        index:(NSUInteger)index
                        label:(NSString *)label
                         font:(UIFont *)font
                        color:(UIColor *)color
{
    CGFloat frameWidth = frameSize.width;
    CGFloat width = frameWidth / TAB_BAR_BUTTON_COUNT;
    CGFloat height = minimumFrameDimension / MINIMUM_FRAME_DIMENSION_TO_TAB_BAR_HEIGHT;

    if (self = [super initWithFrame:CGRectMake(
                                               index * width,
                                               frameSize.height - height,
                                               width,
                                               height
                                               )]) {
        if (VERBOSE) self.layer.borderWidth = 1;
        [self setAttributedTitle:
            [[NSAttributedString alloc] initWithString:label
                                            attributes:@{
                                                         NSFontAttributeName: font,
                                                         NSForegroundColorAttributeName: color
                                                         }]
                        forState:UIControlStateNormal];
        self.alpha = 0;
    }

    return self;
}

@end
