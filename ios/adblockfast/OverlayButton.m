//
//  OverlayButton.m
//  adblockfast
//
//  Created by Brian Kennish on 10/10/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "OverlayButton.h"
#import "Constants.h"

@implementation OverlayButton

- (OverlayButton *)initWithFrameWidth:(CGFloat)frameWidth
                        overlayHeight:(CGFloat)overlayHeight
                               height:(CGFloat)height
                                label:(NSAttributedString *)label
                                color:(UIColor *)color
{
    CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
    CGFloat width = 2 * margin + label.size.width;

    if (self = [super initWithFrame:CGRectMake(
                                               (frameWidth - 2 * margin - width) / 2,
                                               overlayHeight - height - margin,
                                               width,
                                               height
                                               )]) {
        self.layer.borderWidth = 1;
        self.layer.cornerRadius = margin / MARGIN_TO_CORNER_RADIUS;
        self.layer.borderColor = color.CGColor;
        [self setAttributedTitle:label forState:UIControlStateNormal];
    }

    return self;
}

@end
