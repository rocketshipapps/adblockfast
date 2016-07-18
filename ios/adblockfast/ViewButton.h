//
//  ViewButton.h
//  Adblock Fast
//
//  Created by Brian Kennish on 10/9/15.
//  Copyright © 2015, 2016 Rocketship. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewButton : UIButton

- (ViewButton *)initWithIndex:(NSUInteger)index
                        label:(NSString *)label
                         font:(UIFont *)font
                        color:(UIColor *)color
                    frameSize:(CGSize)frameSize
        minimumFrameDimension:(CGFloat)minimumFrameDimension;

@end
