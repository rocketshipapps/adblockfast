//
//  ViewButton.h
//  adblockfast
//
//  Created by Brian Kennish on 10/9/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewButton : UIButton

- (ViewButton *)initWithFrameSize:(CGSize)frameSize
            minimumFrameDimension:(CGFloat)minimumFrameDimension
                            index:(NSUInteger)index
                            label:(NSString *)label
                             font:(UIFont *)font
                            color:(UIColor *)color;

@end
