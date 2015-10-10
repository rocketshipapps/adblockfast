//
//  StatusLabel.h
//  adblockfast
//
//  Created by Brian Kennish on 10/4/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "Label.h"

@interface StatusLabel : Label

- (StatusLabel *)initWithYTerminus:(CGFloat)yTerminus
                             width:(CGFloat)width
                           message:(NSString *)message
                              font:(UIFont *)font
                          boldFont:(UIFont *)boldFont
                             color:(UIColor *)color;

@end
