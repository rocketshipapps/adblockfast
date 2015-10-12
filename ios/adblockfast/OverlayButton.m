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

- (OverlayButton *)initWithIndex:(NSUInteger)index
                          height:(CGFloat)height
                           label:(NSAttributedString *)label
                           color:(UIColor *)color
                     buttonCount:(NSUInteger)buttonCount
                   overlayHeight:(CGFloat)overlayHeight
                      frameWidth:(CGFloat)frameWidth
{
    CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
    CGFloat slotWidth = (frameWidth - 2 * margin) / buttonCount;
    CGFloat width = 2 * margin + label.size.width;

    if (self = [super initWithFrame:CGRectMake(
                                               index * slotWidth + (slotWidth - width) / 2,
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
