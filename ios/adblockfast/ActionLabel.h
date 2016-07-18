//
//  ActionLabel.h
//  Adblock Fast
//
//  Created by Brian Kennish on 10/4/15.
//  Copyright © 2015, 2016 Rocketship. All rights reserved.
//

#import "Label.h"

@interface ActionLabel : Label

- (ActionLabel *)initWithYOrigin:(CGFloat)yOrigin
                           width:(CGFloat)width
                            hint:(NSString *)hint
                            font:(UIFont *)font
                           color:(UIColor *)color;

@end
