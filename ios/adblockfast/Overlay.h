//
//  Overlay.h
//  adblockfast
//
//  Created by Brian Kennish on 10/10/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Overlay : UIButton

+ (Overlay *)open;
- (Overlay *)initWithHeight:(CGFloat)height
                       text:(NSAttributedString *)text
                  frameSize:(CGSize)frameSize;
- (void)open;
- (void)close;

@end
