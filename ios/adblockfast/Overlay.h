//
//  Overlay.h
//  adblockfast
//
//  Created by Brian Kennish on 10/10/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Overlay : UIButton

- (Overlay *)initWithFrameSize:(CGSize)frameSize
                        height:(CGFloat)height
                          text:(NSAttributedString *)text;

@end
