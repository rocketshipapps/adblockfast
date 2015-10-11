//
//  Overlay.m
//  adblockfast
//
//  Created by Brian Kennish on 10/10/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "Overlay.h"
#import "Constants.h"

@implementation Overlay

- (Overlay *)initWithFrameSize:(CGSize)frameSize
                        height:(CGFloat)height
                          text:(NSAttributedString *)text
{
    CGFloat frameWidth = frameSize.width;
    CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
    CGFloat width = frameWidth - 2 * margin;

    if (self = [super initWithFrame:CGRectMake(
                                               (frameWidth - width) / 2,
                                               (frameSize.height - height) / 2,
                                               width,
                                               height
                                               )]) {
        self.layer.borderWidth = OVERLAY_BORDER_WIDTH;
        CGFloat cornerRadius = margin / MARGIN_TO_CORNER_RADIUS;
        self.layer.cornerRadius = cornerRadius;
        self.layer.borderColor =
            [[UIColor blackColor] colorWithAlphaComponent:OVERLAY_ALPHA].CGColor;
        self.layer.masksToBounds = YES;
        self.backgroundColor = [UIColor colorWithRed:DARK_COLOR_R / 2
                                               green:DARK_COLOR_G / 2
                                                blue:DARK_COLOR_B / 2
                                               alpha:OVERLAY_ALPHA];
        self.titleEdgeInsets = UIEdgeInsetsMake(margin, margin, margin, margin);
        self.contentVerticalAlignment = UIControlContentVerticalAlignmentTop;
        self.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [self setAttributedTitle:text forState:UIControlStateNormal];
        self.titleLabel.numberOfLines = 0;
        self.transform = CGAffineTransformScale(self.transform, 1. / width, 1. / height);
        self.alpha = 0;
    }

    return self;
}

@end
