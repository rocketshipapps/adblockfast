//
//  StatusLabel.m
//  adblockfast
//
//  Created by Brian Kennish on 10/4/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "StatusLabel.h"
#import "Constants.h"

@implementation StatusLabel

- (StatusLabel *)initWithYTerminus:(CGFloat)yTerminus
                             width:(CGFloat)width
                           message:(NSString *)message
                              font:font
                          boldFont:boldFont
                             color:color
{
    NSMutableAttributedString *attributedMessage =
        [[NSMutableAttributedString alloc] initWithString:STATUS_LABEL
                                               attributes:@{
                                                            NSFontAttributeName: boldFont,
                                                            NSForegroundColorAttributeName: color
                                                            }];
    [attributedMessage appendAttributedString:
        [[NSAttributedString alloc] initWithString:message
                                        attributes:@{
                                                     NSFontAttributeName: font,
                                                     NSForegroundColorAttributeName: color
                                                     }]];
    CGFloat height = attributedMessage.size.height;
    if (self = [super initWithFrame:CGRectMake(0, yTerminus - 2 * height, width, height)])
        [self renderWithAttributedText:attributedMessage];
    return self;
}

@end
