//
//  OverlayButton.h
//  adblockfast
//
//  Created by Brian Kennish on 10/10/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface OverlayButton : UIButton

- (OverlayButton *)initWithIndex:(NSUInteger)index
                          height:(CGFloat)height
                           label:(NSAttributedString *)label
                           color:(UIColor *)color
                     buttonCount:(NSUInteger)buttonCount
                   overlayHeight:(CGFloat)overlayHeight
                      frameWidth:(CGFloat)frameWidth;

@end
