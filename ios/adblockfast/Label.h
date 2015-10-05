//
//  Label.h
//  adblockfast
//
//  Created by Brian Kennish on 10/4/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Label : UILabel

- (void)renderWithAttributedText:(NSAttributedString *)attributedText;

@end
