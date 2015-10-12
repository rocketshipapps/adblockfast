//
//  Overlay.m
//  adblockfast
//
//  Created by Brian Kennish on 10/10/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "Overlay.h"
#import "Constants.h"

@interface Overlay ()

@property (nonatomic) CGFloat width;
@property (nonatomic) CGFloat height;
@property (nonatomic) CGFloat frameWidth;

@end

static Overlay *_open;

@implementation Overlay

+ (Overlay *)open
{
    return _open;
}

- (Overlay *)initWithHeight:(CGFloat)height
                       text:(NSAttributedString *)text
                  frameSize:(CGSize)frameSize
{
    CGFloat frameWidth = self.frameWidth = frameSize.width;
    CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
    CGFloat width = self.width = frameWidth - 2 * margin;

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
        self.height = height;
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

- (void)open
{
    BOOL isOpen = [self isEqual:_open];
    CGFloat delay = 0;

    if (!isOpen) {
        if (_open) {
            [_open close];
            delay = MEDIUM_DURATION;
        }

        _open = self;
        self.alpha = 1;
    }

    CGFloat frameWidth = self.frameWidth;
    CGFloat width = self.width;
    CGFloat height = self.height;
    CGFloat heightDuringBounce = frameWidth / width * height;
    [UIView animateWithDuration:isOpen ? MICRO_DURATION : SMALL_DURATION
                          delay:delay
                        options:UIViewAnimationOptionCurveEaseIn
                     animations:^{
                         self.transform =
                             CGAffineTransformScale(
                                                    self.transform,
                                                    isOpen ? frameWidth / width : frameWidth,
                                                    isOpen ? heightDuringBounce / height :
                                                             heightDuringBounce
                                                    );
                     }
                     completion:nil];
    [UIView animateWithDuration:MICRO_DURATION
                          delay:delay + (isOpen ? MICRO_DURATION : SMALL_DURATION)
                        options:UIViewAnimationOptionCurveEaseOut
                     animations:^{
                         self.transform = CGAffineTransformScale(
                                                                 self.transform,
                                                                 width / frameWidth,
                                                                 height / heightDuringBounce
                                                                 );
                     }
                     completion:nil];
}

- (void)close
{
    _open = nil;
    CGFloat delay = SMALL_DURATION + MICRO_DURATION;
    CGFloat frameWidth = self.frameWidth;
    [UIView animateWithDuration:delay
                     animations:^{
                         self.transform =
                             CGAffineTransformScale(
                                                    self.transform,
                                                    1. /
                                                        (frameWidth -
                                                             2 * frameWidth /
                                                                 FRAME_WIDTH_TO_MARGIN),
                                                    1. / self.height
                                                    );
                     }];
    dispatch_after(
                   dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_SEC),
                   dispatch_get_main_queue(),
                   ^{ self.alpha = 0; }
                   );
}

@end
