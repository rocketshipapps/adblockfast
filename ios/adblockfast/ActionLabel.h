//
//  ActionLabel.h
//  adblockfast
//
//  Created by Brian Kennish on 10/4/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "Label.h"

@interface ActionLabel : Label

- (ActionLabel *)initWithYOrigin:(CGFloat)yOrigin
                           width:(CGFloat)width
                            hint:(NSString *)hint
                            font:(UIFont *)font
                           color:(UIColor *)color;

@end
