//
//  Label.m
//  adblockfast
//
//  Created by Brian Kennish on 10/4/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "Label.h"
#import "Constants.h"

@implementation Label

- (void)renderWithAttributedText:(NSAttributedString *)attributedText
{
    if (VERBOSE) self.layer.borderWidth = 1;
    self.attributedText = attributedText;
    self.textAlignment = NSTextAlignmentCenter;
    self.alpha = 0;
}

@end
