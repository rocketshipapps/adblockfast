//
//  ActionLabel.m
//  adblockfast
//
//  Created by Brian Kennish on 10/4/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "ActionLabel.h"

@implementation ActionLabel

- (ActionLabel *)initWithYOrigin:(CGFloat)yOrigin
                           width:(CGFloat)width
                            hint:(NSString *)hint
                            font:font
                           color:color
{
    NSAttributedString *attributedHint =
        [[NSAttributedString alloc] initWithString:hint
                                        attributes:@{
                                                     NSFontAttributeName: font,
                                                     NSForegroundColorAttributeName: color
                                                     }];
    CGFloat height = attributedHint.size.height;
    if (self = [super initWithFrame:CGRectMake(0, yOrigin + height, width, height)])
        [self renderWithAttributedText:attributedHint];
    return self;
}

@end
