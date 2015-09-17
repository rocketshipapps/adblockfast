//
//  ViewController.m
//  adblockfast
//
//  Created by Brian Kennish on 8/19/15.
//  Copyright © 2015 Rocketship. All rights reserved.
//

#import "ViewController.h"
#import <SafariServices/SFContentBlockerManager.h>
#import "Constants.h"

#define CONTENT_BLOCKER_ID (REVERSE_DOMAIN_NAME @".adblockfast.blocker")
#define APP_OPEN_COUNT_KEY @"AppOpenCount"
#define NOTIFICATION_REQUEST_TEXT_KEY @"NotificationRequestText"
#define HELP_TEXT_KEY @"HelpText"
#define ABOUT_TEXT_KEY @"AboutText"
#define HEADING_FONT_NAME @"HudsonNY"
#define BODY_FONT_NAME @"AvenirNextLTPro-Light"
#define BODY_BOLD_FONT_NAME @"AvenirNext-Medium"
#define BLOCKED_FILENAME_PREFIX @"blocked-"
#define UNBLOCKED_FILENAME_PREFIX @"unblocked-"
#define VECTOR_GRAPHIC_FILE_EXTENSION @"pdf"
#define NAME @"Adblock Fast"
#define STATUS_LABEL @"Status: "
#define DISALLOWED_STATUS_MESSAGE @"Waiting for permission"
#define BLOCKED_STATUS_MESSAGE @"Blocking ads"
#define UNBLOCKED_STATUS_MESSAGE @"Not blocking ads"
#define DISALLOWED_ACTION_HINT @"Tap to get help"
#define BLOCKED_ACTION_HINT @"Tap to unblock"
#define UNBLOCKED_ACTION_HINT @"Tap to block"
#define HELP_LABEL @"Help"
#define ABOUT_LABEL @"About"
#define CLOSE_LABEL @"Okay"
#define TAB_BAR_BUTTON_COUNT 3
#define OVERLAY_BORDER_WIDTH 1
#define OVERLAY_LINE_SPACING 2
#define LIGHT_COLOR_R (255. / 255)
#define LIGHT_COLOR_G (255. / 255)
#define LIGHT_COLOR_B (255. / 255)
#define DARK_COLOR_R (44. / 255)
#define DARK_COLOR_G (44. / 255)
#define DARK_COLOR_B (44. / 255)
#define OVERLAY_ALPHA .9
#define MICRO_DURATION .125
#define SMALL_DURATION .25
#define MEDIUM_DURATION .5
#define LARGE_DURATION 1
#define MEGA_DURATION 2
#define FRAME_COUNT 32
#define FPS 24
#define NOTIFICATION_REQUEST_FREQUENCY 3
#define MINIMUM_IPHONE_ASPECT_RATIO 1.5
#define IPHONE_TO_IPAD_FRAME_DIMENSION (8. / 7)
#define FRAME_WIDTH_TO_MARGIN (128. / 3)
#define MINIMUM_FRAME_DIMENSION_TO_NAVBAR_HEIGHT (16. / 3)
#define MINIMUM_FRAME_DIMENSION_TO_HEADING_FONT_SIZE 16.
#define MINIMUM_FRAME_DIMENSION_TO_BODY_FONT_SIZE (320. / 13)
#define MINIMUM_FRAME_DIMENSION_TO_TAB_BAR_HEIGHT (32. / 7)
#define MARGIN_TO_CORNER_RADIUS 1.5
#define NAME_HEIGHT_TO_MARGIN 3.
#define HEAD_INDENT_TO_FONT_SIZE (14. / 13)
#define NORMAL_TO_SMALL_CAPS_FONT_SIZE (9. / 7)

@interface ViewController ()

@property (nonatomic) NSUserDefaults *preferences;
@property (nonatomic) CGFloat minimumFrameDimension;
@property (nonatomic) UIFont *headingFont;
@property (nonatomic) UIColor *darkColor;
@property (nonatomic) UILabel *nameLabel;
@property (nonatomic) CGRect onOffButtonFrame;
@property (nonatomic) UIFont *bodyBoldFont;
@property (nonatomic) UIFont *bodyFont;
@property (nonatomic) UILabel *disallowedStatusLabel;
@property (nonatomic) UILabel *blockedStatusLabel;
@property (nonatomic) UILabel *unblockedStatusLabel;
@property (nonatomic) UIButton *onOffButton;
@property (nonatomic) UILabel *disallowedActionLabel;
@property (nonatomic) UILabel *blockedActionLabel;
@property (nonatomic) UILabel *unblockedActionLabel;
@property (nonatomic) UIButton *helpButton;
@property (nonatomic) UIButton *aboutButton;
@property (nonatomic) UIColor *lightColor;
@property (nonatomic) NSMutableAttributedString *helpViewText;
@property (nonatomic) NSAttributedString *closeButtonText;
@property (nonatomic) CGFloat closeButtonHeight;
@property (nonatomic) CGFloat helpViewHeight;
@property (nonatomic) UIButton *helpView;
@property (nonatomic) NSMutableAttributedString *aboutViewText;
@property (nonatomic) CGFloat aboutViewHeight;
@property (nonatomic) UIButton *aboutView;
@property (nonatomic) BOOL hasLaunchScreen;
@property (nonatomic) BOOL isOnOffButtonAnimating;
@property (nonatomic) BOOL isRulesetCompiling;
@property (nonatomic) BOOL isHelpOpen;
@property (nonatomic) BOOL isAboutOpen;

@end

@implementation ViewController

+ (NSString *)expandMarkdownLineBreaks:(NSString *)markdown
{
    return [markdown stringByReplacingOccurrencesOfString:@"<br>" withString:@"\n"];
}

+ (NSMutableAttributedString *)markdownToAttributedString:(NSString *)markdown
                                                 fontName:(NSString *)fontName
                                             boldFontName:(NSString *)boldFontName
                                                 fontSize:(NSUInteger)fontSize
                                                textColor:(UIColor *)textColor
{
    NSArray *words = [markdown componentsSeparatedByString:@" "];
    NSUInteger wordCount = words.count;
    NSMutableAttributedString *attributedString = [[NSMutableAttributedString alloc] init];
    UIFont *font = [UIFont fontWithName:fontName size:fontSize];

    for (NSUInteger i = 0; i < wordCount; i++) {
        [attributedString appendAttributedString:
            [[NSAttributedString alloc] initWithString:i ? @" " : @""
                                            attributes:@{
                                                         NSFontAttributeName: font,
                                                         NSForegroundColorAttributeName: textColor
                                                         }]];
        NSString *word = words[i];
        NSArray *tokens = [word componentsSeparatedByString:@"**"];
        BOOL isWordBold = tokens.count == 3;
        NSString *leftovers;

        if (isWordBold) {
            word = tokens[1];
            leftovers = tokens[2];
        }

        tokens = [word componentsSeparatedByString:@"<small>"];
        BOOL isWordSmall = tokens.count == 2;
        if (isWordSmall) word = tokens[1];
        tokens = [word componentsSeparatedByString:@")"];

        if (tokens.count == 2) {
            word = tokens[0];
            leftovers = @")";
        }

        NSUInteger smallCapsFontSize = fontSize / NORMAL_TO_SMALL_CAPS_FONT_SIZE;
        [attributedString appendAttributedString:
            [[NSAttributedString alloc] initWithString:
                [ViewController expandMarkdownLineBreaks:word]
                                            attributes:@{
                                                NSFontAttributeName:
                                                    isWordBold ?
                                                        isWordSmall ?
                                                            [UIFont fontWithName:boldFontName
                                                                            size:smallCapsFontSize]
                                                            : [UIFont fontWithName:boldFontName
                                                                              size:fontSize] :
                                                        isWordSmall ?
                                                            [UIFont fontWithName:fontName
                                                                            size:smallCapsFontSize]
                                                            : font,
                                                NSForegroundColorAttributeName: textColor
                                                }]];
        if ([leftovers length])
            [attributedString appendAttributedString:
                [[NSAttributedString alloc] initWithString:
                    [ViewController expandMarkdownLineBreaks:leftovers]
                                                attributes:@{
                                                             NSFontAttributeName: font,
                                                             NSForegroundColorAttributeName:
                                                                 textColor
                                                             }]];
    }

    return attributedString;
}

+ (UIImage *)drawVectorGraphicWithFilename:(NSString *)filename inFrame:(CGRect)frame
{
    CGPDFDocumentRef pdf =
        CGPDFDocumentCreateWithURL(
            (__bridge CFURLRef)[NSURL fileURLWithPath:
                [[NSBundle mainBundle] pathForResource:filename
                                                ofType:VECTOR_GRAPHIC_FILE_EXTENSION]]
                                   );
    CGPDFPageRef pdfPage = CGPDFDocumentGetPage(pdf, 1);
    CGSize pdfSize = CGPDFPageGetBoxRect(pdfPage, kCGPDFCropBox).size;
    CGFloat pdfWidth = pdfSize.width;
    CGSize frameSize = frame.size;
    CGFloat pdfHeight = pdfSize.height;
    CGFloat pdfToImageDimension = MAX(pdfWidth / frameSize.width, pdfHeight / frameSize.height);
    CGFloat imageWidth = pdfWidth / pdfToImageDimension;
    CGFloat imageHeight = pdfHeight / pdfToImageDimension;
    CGSize imageSize = CGSizeMake(imageWidth, imageHeight);
    UIGraphicsBeginImageContextWithOptions(imageSize, NO, 0);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextScaleCTM(context, imageWidth / pdfWidth, imageHeight / pdfHeight);
    CGContextDrawPDFPage(context, pdfPage);
    CGPDFDocumentRelease(pdf);
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    UIGraphicsBeginImageContextWithOptions(imageSize, NO, 0);
    context = UIGraphicsGetCurrentContext();
    CGContextDrawImage(context, CGRectMake(0, 0, imageWidth, imageHeight), [image CGImage]);
    image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
}

- (void)animateOnOffButtonWithIndex:(NSUInteger)index
{
    UIButton *button = self.onOffButton;
    NSUInteger frameCountForStatus = FRAME_COUNT / 2;
    BOOL isAlternateStatus = fmod(index, FRAME_COUNT) >= frameCountForStatus;
    NSUInteger indexForStatus = fmod(index, frameCountForStatus);
    UIImage *image =
        [ViewController drawVectorGraphicWithFilename:
            [isAlternateStatus ? UNBLOCKED_FILENAME_PREFIX :
                                 BLOCKED_FILENAME_PREFIX stringByAppendingFormat:
                @"%lu", (unsigned long)indexForStatus]
                                              inFrame:self.onOffButtonFrame];
    [button setImage:image forState:UIControlStateNormal];
    [button setImage:image forState:UIControlStateHighlighted];
    index++;
    dispatch_after(
                   dispatch_time(DISPATCH_TIME_NOW, 1. / FPS * NSEC_PER_SEC),
                   dispatch_get_main_queue(),
                   ^{
                       NSUserDefaults *preferences = self.preferences;

                       if (
                           self.isRulesetCompiling ||
                               isAlternateStatus ==
                                   [preferences boolForKey:BLOCKING_STATUS_KEY] || indexForStatus
                           ) [self animateOnOffButtonWithIndex:index];
                       else if ([preferences boolForKey:BLOCKER_PERMISSION_KEY]) {
                           BOOL isBlockingOn = [preferences boolForKey:BLOCKING_STATUS_KEY];
                           [UIView animateWithDuration:SMALL_DURATION
                                            animations:^{
                                                self.blockedStatusLabel.alpha = isBlockingOn;
                                                self.unblockedStatusLabel.alpha = !isBlockingOn;
                                            }];
                           [UIView animateWithDuration:SMALL_DURATION
                                                 delay:SMALL_DURATION
                                               options:UIViewAnimationOptionCurveEaseInOut
                                            animations:^{
                                                self.blockedActionLabel.alpha = isBlockingOn;
                                                self.unblockedActionLabel.alpha = !isBlockingOn;
                                            }
                                            completion:^(BOOL finished) {
                                                self.isOnOffButtonAnimating = NO;
                                            }];
                       }
                       else self.isOnOffButtonAnimating = NO;
                   }
                   );
}

- (void)animateOnOffButton
{
    [self animateOnOffButtonWithIndex:
        self.hasLaunchScreen || [self.preferences boolForKey:BLOCKING_STATUS_KEY] ?
            0 : FRAME_COUNT / 2];
}

- (void)onOffButtonWasTapped
{
    if (!self.isOnOffButtonAnimating) {
        NSUserDefaults *preferences = self.preferences;

        if (![preferences boolForKey:BLOCKER_PERMISSION_KEY]) [self openHelp];
        else {
            self.isOnOffButtonAnimating = YES;
            [self animateOnOffButton];
            BOOL isBlockingOn = ![preferences boolForKey:BLOCKING_STATUS_KEY];
            [preferences setBool:isBlockingOn forKey:BLOCKING_STATUS_KEY];
            [SFContentBlockerManager reloadContentBlockerWithIdentifier:CONTENT_BLOCKER_ID
                                                      completionHandler:^(NSError *error) {
                if (error.code) {
                    [preferences setBool:!isBlockingOn forKey:BLOCKING_STATUS_KEY];
                    if (VERBOSE) NSLog(@"%@", error.localizedDescription);
                }

                self.isRulesetCompiling = NO;
            }];
        }
    }
}

- (void)openHelp
{
    CGFloat delay = 0;

    if (self.isAboutOpen) {
        [self closeAbout];
        delay = MEDIUM_DURATION;
    }

    BOOL isOpen = self.isHelpOpen;
    UIButton *view = self.helpView;

    if (!isOpen) {
        self.isHelpOpen = YES;
        view.alpha = 1;
    }

    CGFloat frameWidth = self.view.frame.size.width;
    CGFloat width = frameWidth - 2 * frameWidth / FRAME_WIDTH_TO_MARGIN;
    CGFloat height = self.helpViewHeight;
    CGFloat heightDuringBounce = frameWidth / width * height;
    [UIView animateWithDuration:isOpen ? MICRO_DURATION : SMALL_DURATION
                          delay:delay
                        options:UIViewAnimationOptionCurveEaseIn
                     animations:^{
                         view.transform =
                             CGAffineTransformScale(
                                                    view.transform,
                                                    isOpen ? frameWidth / width : frameWidth,
                                                    isOpen ? heightDuringBounce / height :
                                                             heightDuringBounce
                                                    );
                     }
                     completion:nil];
    [UIView animateWithDuration:MICRO_DURATION
                          delay:delay + (isOpen ? MICRO_DURATION : SMALL_DURATION)
                        options:UIViewAnimationOptionCurveEaseOut
                     animations:^{
                         view.transform = CGAffineTransformScale(
                                                                 view.transform,
                                                                 width / frameWidth,
                                                                 height / heightDuringBounce
                                                                 );
                     }
                     completion:nil];
}

- (void)closeHelp
{
    CGFloat delay = SMALL_DURATION + MICRO_DURATION;
    UIButton *view = self.helpView;
    CGFloat frameWidth = self.view.frame.size.width;
    [UIView animateWithDuration:delay
                     animations:^{
                         view.transform =
                             CGAffineTransformScale(
                                                    view.transform,
                                                    1. /
                                                        (frameWidth -
                                                             2 * frameWidth /
                                                                 FRAME_WIDTH_TO_MARGIN),
                                                    1. / self.helpViewHeight
                                                    );
                     }];
    dispatch_after(
                   dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_SEC),
                   dispatch_get_main_queue(),
                   ^{
                       self.helpView.alpha = 0;
                       self.isHelpOpen = NO;
                   }
                   );
}

- (void)openAbout
{
    CGFloat delay = 0;

    if (self.isHelpOpen) {
        [self closeHelp];
        delay = MEDIUM_DURATION;
    }

    BOOL isOpen = self.isAboutOpen;
    UIButton *view = self.aboutView;

    if (!isOpen) {
        self.isAboutOpen = YES;
        view.alpha = 1;
    }

    CGFloat frameWidth = self.view.frame.size.width;
    CGFloat width = frameWidth - 2 * frameWidth / FRAME_WIDTH_TO_MARGIN;
    CGFloat height = self.aboutViewHeight;
    CGFloat heightDuringBounce = frameWidth / width * height;
    [UIView animateWithDuration:isOpen ? MICRO_DURATION : SMALL_DURATION
                          delay:delay
                        options:UIViewAnimationOptionCurveEaseIn
                     animations:^{
                         view.transform =
                             CGAffineTransformScale(
                                                    view.transform,
                                                    isOpen ? frameWidth / width : frameWidth,
                                                    isOpen ? heightDuringBounce / height :
                                                             heightDuringBounce
                                                    );
                     }
                     completion:nil];
    [UIView animateWithDuration:MICRO_DURATION
                          delay:delay + (isOpen ? MICRO_DURATION : SMALL_DURATION)
                        options:UIViewAnimationOptionCurveEaseOut
                     animations:^{
                         view.transform = CGAffineTransformScale(
                                                                 view.transform,
                                                                 width / frameWidth,
                                                                 height / heightDuringBounce
                                                                 );
                     }
                     completion:nil];
}

- (void)closeAbout
{
    CGFloat delay = SMALL_DURATION + MICRO_DURATION;
    UIButton *view = self.aboutView;
    CGFloat frameWidth = self.view.frame.size.width;
    [UIView animateWithDuration:delay
                     animations:^{
                         view.transform =
                             CGAffineTransformScale(
                                                    view.transform,
                                                    1. /
                                                        (frameWidth -
                                                             2 * frameWidth /
                                                                 FRAME_WIDTH_TO_MARGIN),
                                                    1. / self.aboutViewHeight
                                                    );
                     }];
    dispatch_after(
                   dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_SEC),
                   dispatch_get_main_queue(),
                   ^{
                       self.aboutView.alpha = 0;
                       self.isAboutOpen = NO;
                   }
                   );
}

- (NSUserDefaults *)preferences
{
    if (!_preferences) {
        _preferences = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP_ID];
        [_preferences registerDefaults:
            [NSDictionary dictionaryWithContentsOfFile:
                [[NSBundle mainBundle] pathForResource:DEFAULT_PREFERENCES_FILENAME
                                                ofType:PREFERENCES_FILE_EXTENSION]]];
    }

    return _preferences;
}

- (CGFloat)minimumFrameDimension
{
    if (!_minimumFrameDimension) {
        CGSize size = self.view.frame.size;
        CGFloat width = size.width;
        CGFloat height = size.height;
        _minimumFrameDimension = MIN(width, height);
        if (MAX(width, height) / _minimumFrameDimension < MINIMUM_IPHONE_ASPECT_RATIO)
            _minimumFrameDimension /= IPHONE_TO_IPAD_FRAME_DIMENSION;
    }

    return _minimumFrameDimension;
}

- (UIFont *)headingFont
{
    _headingFont ||
        (_headingFont = [UIFont fontWithName:HEADING_FONT_NAME
                                        size:self.minimumFrameDimension /
                                                 MINIMUM_FRAME_DIMENSION_TO_HEADING_FONT_SIZE]);
    return _headingFont;
}

- (UIColor *)darkColor
{
    _darkColor || (_darkColor = [UIColor colorWithRed:DARK_COLOR_R
                                                green:DARK_COLOR_G
                                                 blue:DARK_COLOR_B
                                                alpha:1]);
    return _darkColor;
}

- (UILabel *)nameLabel
{
    if (!_nameLabel) {
        _nameLabel =
            [[UILabel alloc] initWithFrame:CGRectMake(
                                                      0,
                                                      0,
                                                      self.view.frame.size.width,
                                                      self.minimumFrameDimension /
                                                          MINIMUM_FRAME_DIMENSION_TO_NAVBAR_HEIGHT
                                                      )];
        if (VERBOSE) _nameLabel.layer.borderWidth = 1;
        _nameLabel.textAlignment = NSTextAlignmentCenter;
        _nameLabel.font = self.headingFont;
        _nameLabel.textColor = self.darkColor;
        _nameLabel.text = NAME;
        _nameLabel.alpha = 0;
    }

    return _nameLabel;
}

- (CGRect)onOffButtonFrame
{
    if (!_onOffButtonFrame.size.width) {
        CGSize frameSize = self.view.frame.size;
        CGPDFDocumentRef pdf =
            CGPDFDocumentCreateWithURL(
                (__bridge CFURLRef)[NSURL fileURLWithPath:
                    [[NSBundle mainBundle] pathForResource:BLOCKED_FILENAME_PREFIX @"0"
                                                    ofType:VECTOR_GRAPHIC_FILE_EXTENSION]]
                                       );
        CGSize pdfSize = CGPDFPageGetBoxRect(CGPDFDocumentGetPage(pdf, 1), kCGPDFCropBox).size;
        CGPDFDocumentRelease(pdf);
        CGFloat pdfWidth = pdfSize.width;
        CGFloat maximumDimension = self.minimumFrameDimension;
        CGFloat pdfHeight = pdfSize.height;
        CGFloat pdfToOnOffButtonDimension =
            MAX(pdfWidth / maximumDimension, pdfHeight / maximumDimension);
        CGFloat width = pdfWidth / pdfToOnOffButtonDimension;
        CGFloat height = pdfHeight / pdfToOnOffButtonDimension;
        _onOffButtonFrame = CGRectMake(
                                       (frameSize.width - width) / 2,
                                       (frameSize.height - height) / 2,
                                       width,
                                       height
                                       );
    }

    return _onOffButtonFrame;
}

- (UIFont *)bodyBoldFont
{
    _bodyBoldFont ||
        (_bodyBoldFont = [UIFont fontWithName:BODY_BOLD_FONT_NAME
                                         size:self.minimumFrameDimension /
                                                  MINIMUM_FRAME_DIMENSION_TO_BODY_FONT_SIZE]);
    return _bodyBoldFont;
}

- (UIFont *)bodyFont
{
    _bodyFont || (_bodyFont = [UIFont fontWithName:BODY_FONT_NAME
                                              size:self.minimumFrameDimension /
                                                       MINIMUM_FRAME_DIMENSION_TO_BODY_FONT_SIZE]);
    return _bodyFont;
}

- (UILabel *)disallowedStatusLabel
{
    if (!_disallowedStatusLabel) {
        UIColor *color = self.darkColor;
        NSMutableAttributedString *text =
            [[NSMutableAttributedString alloc] initWithString:STATUS_LABEL
                                                   attributes:@{
                                                                NSFontAttributeName:
                                                                    self.bodyBoldFont,
                                                                NSForegroundColorAttributeName:
                                                                    color
                                                                }];
        [text appendAttributedString:
            [[NSAttributedString alloc] initWithString:DISALLOWED_STATUS_MESSAGE
                                            attributes:@{
                                                         NSFontAttributeName: self.bodyFont,
                                                         NSForegroundColorAttributeName: color
                                                         }]];
        CGFloat height = text.size.height;
        _disallowedStatusLabel =
            [[UILabel alloc] initWithFrame:CGRectMake(
                                                      0,
                                                      self.onOffButtonFrame.origin.y - 2 * height,
                                                      self.view.frame.size.width,
                                                      height
                                                      )];
        if (VERBOSE) _disallowedStatusLabel.layer.borderWidth = 1;
        _disallowedStatusLabel.textAlignment = NSTextAlignmentCenter;
        _disallowedStatusLabel.attributedText = text;
        _disallowedStatusLabel.alpha = 0;
    }

    return _disallowedStatusLabel;
}

- (UILabel *)blockedStatusLabel
{
    if (!_blockedStatusLabel) {
        UIColor *color = self.darkColor;
        NSMutableAttributedString *text =
            [[NSMutableAttributedString alloc] initWithString:STATUS_LABEL
                                                   attributes:@{
                                                                NSFontAttributeName:
                                                                    self.bodyBoldFont,
                                                                NSForegroundColorAttributeName:
                                                                    color
                                                                }];
        [text appendAttributedString:
            [[NSAttributedString alloc] initWithString:BLOCKED_STATUS_MESSAGE
                                            attributes:@{
                                                         NSFontAttributeName: self.bodyFont,
                                                         NSForegroundColorAttributeName: color
                                                         }]];
        CGFloat height = text.size.height;
        _blockedStatusLabel =
            [[UILabel alloc] initWithFrame:CGRectMake(
                                                      0,
                                                      self.onOffButtonFrame.origin.y - 2 * height,
                                                      self.view.frame.size.width,
                                                      height
                                                      )];
        if (VERBOSE) _blockedStatusLabel.layer.borderWidth = 1;
        _blockedStatusLabel.textAlignment = NSTextAlignmentCenter;
        _blockedStatusLabel.attributedText = text;
        _blockedStatusLabel.alpha = 0;
    }

    return _blockedStatusLabel;
}

- (UILabel *)unblockedStatusLabel
{
    if (!_unblockedStatusLabel) {
        UIColor *color = self.darkColor;
        NSMutableAttributedString *text =
            [[NSMutableAttributedString alloc] initWithString:STATUS_LABEL
                                                   attributes:@{
                                                                NSFontAttributeName:
                                                                    self.bodyBoldFont,
                                                                NSForegroundColorAttributeName:
                                                                    color
                                                                }];
        [text appendAttributedString:
            [[NSAttributedString alloc] initWithString:UNBLOCKED_STATUS_MESSAGE
                                            attributes:@{
                                                         NSFontAttributeName: self.bodyFont,
                                                         NSForegroundColorAttributeName: color
                                                         }]];
        CGFloat height = text.size.height;
        _unblockedStatusLabel =
            [[UILabel alloc] initWithFrame:CGRectMake(
                                                      0,
                                                      self.onOffButtonFrame.origin.y - 2 * height,
                                                      self.view.frame.size.width,
                                                      height
                                                      )];
        if (VERBOSE) _unblockedStatusLabel.layer.borderWidth = 1;
        _unblockedStatusLabel.textAlignment = NSTextAlignmentCenter;
        _unblockedStatusLabel.attributedText = text;
        _unblockedStatusLabel.alpha = 0;
    }

    return _unblockedStatusLabel;
}

- (UIButton *)onOffButton
{
    if (!_onOffButton) {
        _onOffButton = [[UIButton alloc] initWithFrame:self.onOffButtonFrame];
        if (VERBOSE) _onOffButton.layer.borderWidth = 1;
        [_onOffButton addTarget:self
                         action:@selector(onOffButtonWasTapped)
               forControlEvents:UIControlEventTouchUpInside];
    }

    return _onOffButton;
}

- (UILabel *)disallowedActionLabel
{
    if (!_disallowedActionLabel) {
        CGRect onOffButtonFrame = self.onOffButtonFrame;
        NSAttributedString *text =
            [[NSAttributedString alloc] initWithString:DISALLOWED_ACTION_HINT
                                            attributes:@{
                                                         NSFontAttributeName: self.bodyFont,
                                                         NSForegroundColorAttributeName:
                                                             self.darkColor
                                                         }];
        CGFloat height = text.size.height;
        _disallowedActionLabel =
            [[UILabel alloc] initWithFrame:CGRectMake(
                                                      0,
                                                      onOffButtonFrame.origin.y +
                                                          onOffButtonFrame.size.height + height,
                                                      self.view.frame.size.width,
                                                      height
                                                      )];
        if (VERBOSE) _disallowedActionLabel.layer.borderWidth = 1;
        _disallowedActionLabel.textAlignment = NSTextAlignmentCenter;
        _disallowedActionLabel.attributedText = text;
        _disallowedActionLabel.alpha = 0;
    }

    return _disallowedActionLabel;
}

- (UILabel *)blockedActionLabel
{
    if (!_blockedActionLabel) {
        CGRect onOffButtonFrame = self.onOffButtonFrame;
        NSAttributedString *text =
            [[NSAttributedString alloc] initWithString:BLOCKED_ACTION_HINT
                                            attributes:@{
                                                         NSFontAttributeName: self.bodyFont,
                                                         NSForegroundColorAttributeName:
                                                             self.darkColor
                                                         }];
        CGFloat height = text.size.height;
        _blockedActionLabel =
            [[UILabel alloc] initWithFrame:CGRectMake(
                                                      0,
                                                      onOffButtonFrame.origin.y +
                                                          onOffButtonFrame.size.height + height,
                                                      self.view.frame.size.width,
                                                      height
                                                      )];
        if (VERBOSE) _blockedActionLabel.layer.borderWidth = 1;
        _blockedActionLabel.textAlignment = NSTextAlignmentCenter;
        _blockedActionLabel.attributedText = text;
        _blockedActionLabel.alpha = 0;
    }

    return _blockedActionLabel;
}

- (UILabel *)unblockedActionLabel
{
    if (!_unblockedActionLabel) {
        CGRect onOffButtonFrame = self.onOffButtonFrame;
        NSAttributedString *text =
            [[NSAttributedString alloc] initWithString:UNBLOCKED_ACTION_HINT
                                            attributes:@{
                                                         NSFontAttributeName: self.bodyFont,
                                                         NSForegroundColorAttributeName:
                                                             self.darkColor
                                                         }];
        CGFloat height = text.size.height;
        _unblockedActionLabel =
            [[UILabel alloc] initWithFrame:CGRectMake(
                                                      0,
                                                      onOffButtonFrame.origin.y +
                                                          onOffButtonFrame.size.height + height,
                                                      self.view.frame.size.width,
                                                      height
                                                      )];
        if (VERBOSE) _unblockedActionLabel.layer.borderWidth = 1;
        _unblockedActionLabel.textAlignment = NSTextAlignmentCenter;
        _unblockedActionLabel.attributedText = text;
        _unblockedActionLabel.alpha = 0;
    }

    return _unblockedActionLabel;
}

- (UIButton *)helpButton
{
    if (!_helpButton) {
        CGSize frameSize = self.view.frame.size;
        CGFloat height = self.minimumFrameDimension / MINIMUM_FRAME_DIMENSION_TO_TAB_BAR_HEIGHT;
        _helpButton =
            [[UIButton alloc] initWithFrame:CGRectMake(0,
                                                       frameSize.height - height,
                                                       frameSize.width / TAB_BAR_BUTTON_COUNT,
                                                       height
                                                       )];
        if (VERBOSE) _helpButton.layer.borderWidth = 1;
        [_helpButton setAttributedTitle:
            [[NSAttributedString alloc] initWithString:HELP_LABEL
                                            attributes:@{
                                                         NSFontAttributeName: self.headingFont,
                                                         NSForegroundColorAttributeName:
                                                             self.darkColor
                                                         }]
                               forState:UIControlStateNormal];
        _helpButton.alpha = 0;
        [_helpButton addTarget:self
                        action:@selector(openHelp)
              forControlEvents:UIControlEventTouchUpInside];
    }

    return _helpButton;
}

- (UIButton *)aboutButton
{
    if (!_aboutButton) {
        CGSize frameSize = self.view.frame.size;
        CGFloat frameWidth = frameSize.width;
        CGFloat width = frameWidth / TAB_BAR_BUTTON_COUNT;
        CGFloat height = self.minimumFrameDimension / MINIMUM_FRAME_DIMENSION_TO_TAB_BAR_HEIGHT;
        _aboutButton = [[UIButton alloc] initWithFrame:CGRectMake(frameWidth - width,
                                                                  frameSize.height - height,
                                                                  width,
                                                                  height
                                                                  )];
        if (VERBOSE) _aboutButton.layer.borderWidth = 1;
        [_aboutButton setAttributedTitle:
            [[NSAttributedString alloc] initWithString:ABOUT_LABEL
                                            attributes:@{
                                                         NSFontAttributeName: self.headingFont,
                                                         NSForegroundColorAttributeName:
                                                             self.darkColor
                                                         }]
                                forState:UIControlStateNormal];
        _aboutButton.alpha = 0;
        [_aboutButton addTarget:self
                         action:@selector(openAbout)
               forControlEvents:UIControlEventTouchUpInside];
    }

    return _aboutButton;
}

- (UIColor *)lightColor
{
    _lightColor || (_lightColor = [UIColor colorWithRed:LIGHT_COLOR_R
                                                  green:LIGHT_COLOR_G
                                                   blue:LIGHT_COLOR_B
                                                  alpha:1]);
    return _lightColor;
}

- (NSMutableAttributedString *)helpViewText
{
    if (!_helpViewText) {
        NSUInteger fontSize =
            self.minimumFrameDimension / MINIMUM_FRAME_DIMENSION_TO_BODY_FONT_SIZE;
        _helpViewText =
            [ViewController markdownToAttributedString:[self.preferences stringForKey:HELP_TEXT_KEY]
                                              fontName:BODY_FONT_NAME
                                          boldFontName:BODY_BOLD_FONT_NAME
                                              fontSize:fontSize
                                             textColor:self.lightColor];
        NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
        [paragraphStyle setLineSpacing:OVERLAY_LINE_SPACING];
        [paragraphStyle setHeadIndent:HEAD_INDENT_TO_FONT_SIZE * fontSize];
        [_helpViewText addAttribute:NSParagraphStyleAttributeName
                              value:paragraphStyle
                              range:NSMakeRange(0, _helpViewText.length)];
    }

    return _helpViewText;
}

- (NSAttributedString *)closeButtonText
{
    _closeButtonText ||
        (_closeButtonText =
             [[NSAttributedString alloc] initWithString:CLOSE_LABEL
                                             attributes:@{
                                                          NSFontAttributeName: self.bodyFont,
                                                          NSForegroundColorAttributeName:
                                                              self.lightColor
                                                          }]);
    return _closeButtonText;
}

- (CGFloat)closeButtonHeight
{
    _closeButtonHeight ||
        (_closeButtonHeight =
             2 * self.view.frame.size.width / FRAME_WIDTH_TO_MARGIN +
                 self.closeButtonText.size.height);
    return _closeButtonHeight;
}

- (CGFloat)helpViewHeight
{
    if (!_helpViewHeight) {
        CGFloat frameWidth = self.view.frame.size.width;
        CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
        _helpViewHeight =
            2 * margin +
                [self.helpViewText boundingRectWithSize:CGSizeMake(
                                                                   frameWidth - 4 * margin,
                                                                   CGFLOAT_MAX
                                                                   )
                                                options:NSStringDrawingUsesLineFragmentOrigin
                                                context:nil].size.height + self.closeButtonHeight;
    }

    return _helpViewHeight;
}

- (UIButton *)helpView
{
    if (!_helpView) {
        CGSize frameSize = self.view.frame.size;
        CGFloat frameWidth = frameSize.width;
        CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
        CGFloat width = frameWidth - 2 * margin;
        CGFloat height = self.helpViewHeight;
        _helpView =
            [[UIButton alloc] initWithFrame:CGRectMake(
                                                       (frameWidth - width) / 2,
                                                       (frameSize.height - height) / 2,
                                                       width,
                                                       height
                                                       )];
        _helpView.layer.borderWidth = OVERLAY_BORDER_WIDTH;
        CGFloat cornerRadius = margin / MARGIN_TO_CORNER_RADIUS;
        _helpView.layer.cornerRadius = cornerRadius;
        _helpView.layer.borderColor =
            [[UIColor blackColor] colorWithAlphaComponent:OVERLAY_ALPHA].CGColor;
        _helpView.layer.masksToBounds = YES;
        _helpView.backgroundColor = [UIColor colorWithRed:DARK_COLOR_R / 2
                                                    green:DARK_COLOR_G / 2
                                                     blue:DARK_COLOR_B / 2
                                                    alpha:OVERLAY_ALPHA];
        _helpView.titleEdgeInsets = UIEdgeInsetsMake(margin, margin, margin, margin);
        _helpView.contentVerticalAlignment = UIControlContentVerticalAlignmentTop;
        _helpView.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [_helpView setAttributedTitle:self.helpViewText forState:UIControlStateNormal];
        _helpView.titleLabel.numberOfLines = 0;
        NSAttributedString *closeButtonText = self.closeButtonText;
        CGFloat closeButtonWidth = 2 * margin + closeButtonText.size.width;
        CGFloat closeButtonHeight = self.closeButtonHeight;
        UIButton *closeButton =
            [[UIButton alloc] initWithFrame:CGRectMake(
                                                       (width - closeButtonWidth) / 2,
                                                       height - closeButtonHeight - margin,
                                                       closeButtonWidth,
                                                       closeButtonHeight
                                                       )];
        closeButton.layer.borderWidth = 1;
        closeButton.layer.cornerRadius = cornerRadius;
        closeButton.layer.borderColor = self.lightColor.CGColor;
        [closeButton setAttributedTitle:closeButtonText forState:UIControlStateNormal];
        [_helpView addSubview:closeButton];
        _helpView.transform = CGAffineTransformScale(_helpView.transform, 1. / width, 1. / height);
        _helpView.alpha = 0;
        [_helpView addGestureRecognizer:
            [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeHelp)]];
        [closeButton addGestureRecognizer:
            [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeHelp)]];;
    }

    return _helpView;
}

- (NSMutableAttributedString *)aboutViewText
{
    if (!_aboutViewText) {
        NSUInteger fontSize =
            self.minimumFrameDimension / MINIMUM_FRAME_DIMENSION_TO_BODY_FONT_SIZE;
        _aboutViewText =
            [ViewController markdownToAttributedString:[self.preferences stringForKey:ABOUT_TEXT_KEY]
                                              fontName:BODY_FONT_NAME
                                          boldFontName:BODY_BOLD_FONT_NAME
                                              fontSize:fontSize
                                             textColor:self.lightColor];
        NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
        [paragraphStyle setLineSpacing:OVERLAY_LINE_SPACING];
        [paragraphStyle setHeadIndent:HEAD_INDENT_TO_FONT_SIZE * fontSize];
        [_aboutViewText addAttribute:NSParagraphStyleAttributeName
                               value:paragraphStyle
                               range:NSMakeRange(0, _aboutViewText.length)];
    }

    return _aboutViewText;
}

- (CGFloat)aboutViewHeight
{
    if (!_aboutViewHeight) {
        CGFloat frameWidth = self.view.frame.size.width;
        CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
        _aboutViewHeight =
            2 * margin +
                [self.aboutViewText boundingRectWithSize:CGSizeMake(
                                                                    frameWidth - 4 * margin,
                                                                    CGFLOAT_MAX
                                                                    )
                                                 options:NSStringDrawingUsesLineFragmentOrigin
                                                 context:nil].size.height + self.closeButtonHeight;
    }

    return _aboutViewHeight;
}

- (UIButton *)aboutView
{
    if (!_aboutView) {
        CGSize frameSize = self.view.frame.size;
        CGFloat frameWidth = frameSize.width;
        CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
        CGFloat width = frameWidth - 2 * margin;
        CGFloat height = self.aboutViewHeight;
        _aboutView =
            [[UIButton alloc] initWithFrame:CGRectMake(
                                                       (frameWidth - width) / 2,
                                                       (frameSize.height - height) / 2,
                                                       width,
                                                       height
                                                       )];
        _aboutView.layer.borderWidth = OVERLAY_BORDER_WIDTH;
        CGFloat cornerRadius = margin / MARGIN_TO_CORNER_RADIUS;
        _aboutView.layer.cornerRadius = cornerRadius;
        _aboutView.layer.borderColor =
            [[UIColor blackColor] colorWithAlphaComponent:OVERLAY_ALPHA].CGColor;
        _aboutView.layer.masksToBounds = YES;
        _aboutView.backgroundColor = [UIColor colorWithRed:DARK_COLOR_R / 2
                                                     green:DARK_COLOR_G / 2
                                                      blue:DARK_COLOR_B / 2
                                                     alpha:OVERLAY_ALPHA];
        _aboutView.titleEdgeInsets = UIEdgeInsetsMake(margin, margin, margin, margin);
        _aboutView.contentVerticalAlignment = UIControlContentVerticalAlignmentTop;
        _aboutView.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [_aboutView setAttributedTitle:self.aboutViewText forState:UIControlStateNormal];
        _aboutView.titleLabel.numberOfLines = 0;
        NSAttributedString *closeButtonText = self.closeButtonText;
        CGFloat closeButtonWidth = 2 * margin + closeButtonText.size.width;
        CGFloat closeButtonHeight = self.closeButtonHeight;
        UIButton *closeButton =
            [[UIButton alloc] initWithFrame:CGRectMake(
                                                       (width - closeButtonWidth) / 2,
                                                       height - closeButtonHeight - margin,
                                                       closeButtonWidth,
                                                       closeButtonHeight
                                                       )];
        closeButton.layer.borderWidth = 1;
        closeButton.layer.cornerRadius = cornerRadius;
        closeButton.layer.borderColor = self.lightColor.CGColor;
        [closeButton setAttributedTitle:closeButtonText forState:UIControlStateNormal];
        [_aboutView addSubview:closeButton];
        _aboutView.transform =
            CGAffineTransformScale(_aboutView.transform, 1. / width, 1. / height);
        _aboutView.alpha = 0;
        [_aboutView addGestureRecognizer:
            [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeAbout)]];
        [closeButton addGestureRecognizer:
            [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeAbout)]];;
    }

    return _aboutView;
}

- (void)viewDidBecomeActive
{
    self.hasLaunchScreen = YES;
    self.isOnOffButtonAnimating = YES;
    self.isRulesetCompiling = YES;
    [self animateOnOffButton];
    dispatch_after(
                   dispatch_time(
                                 DISPATCH_TIME_NOW,
                                 (LARGE_DURATION + SMALL_DURATION) * NSEC_PER_SEC
                                 ),
                   dispatch_get_main_queue(),
                   ^{ self.isRulesetCompiling = NO; }
                   );
    NSUserDefaults *preferences = self.preferences;
    BOOL isBlockerAllowed = [preferences boolForKey:BLOCKER_PERMISSION_KEY];
    [UIView animateWithDuration:1
                          delay:MEGA_DURATION
                        options:UIViewAnimationOptionCurveEaseInOut
                     animations:^{
                         self.aboutButton.alpha = self.helpButton.alpha = self.nameLabel.alpha = 1;
                         if (!isBlockerAllowed)
                             self.disallowedActionLabel.alpha =
                                 self.disallowedStatusLabel.alpha = 1;
                         else if ([preferences boolForKey:BLOCKING_STATUS_KEY])
                             self.blockedActionLabel.alpha = self.blockedStatusLabel.alpha = 1;
                         else self.unblockedActionLabel.alpha = self.unblockedStatusLabel.alpha = 1;
                     }
                     completion:nil];
    dispatch_after(
                   dispatch_time(DISPATCH_TIME_NOW, MEGA_DURATION * NSEC_PER_SEC),
                   dispatch_get_main_queue(),
                   ^{
                       self.hasLaunchScreen = NO;
                       if (!isBlockerAllowed) [self openHelp];
                   }
                   );
    NSInteger appOpenCount = [preferences integerForKey:APP_OPEN_COUNT_KEY];
    [preferences setInteger:++appOpenCount forKey:APP_OPEN_COUNT_KEY];
}

- (void)viewDidResignActive
{
    if (self.isHelpOpen) [self closeHelp];
    self.aboutButton.alpha = self.helpButton.alpha = self.unblockedActionLabel.alpha =
        self.blockedActionLabel.alpha = self.disallowedActionLabel.alpha =
            self.unblockedStatusLabel.alpha = self.blockedStatusLabel.alpha =
                self.disallowedStatusLabel.alpha = self.nameLabel.alpha = self.helpView.alpha =
                    self.aboutView.alpha = 0;
    UIButton *onOffButton = self.onOffButton;
    UIImage *onOffButtonImage =
        [ViewController drawVectorGraphicWithFilename:BLOCKED_FILENAME_PREFIX @"0"
                                              inFrame:self.onOffButtonFrame];
    [onOffButton setImage:onOffButtonImage forState:UIControlStateNormal];
    [onOffButton setImage:onOffButtonImage forState:UIControlStateHighlighted];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.view addSubview:self.nameLabel];
    [self.view addSubview:self.disallowedStatusLabel];
    [self.view addSubview:self.blockedStatusLabel];
    [self.view addSubview:self.unblockedStatusLabel];
    [self.view addSubview:self.onOffButton];
    [self.view addSubview:self.disallowedActionLabel];
    [self.view addSubview:self.blockedActionLabel];
    [self.view addSubview:self.unblockedActionLabel];
    [self.view addSubview:self.helpButton];
    [self.view addSubview:self.aboutButton];
    [self.view addSubview:self.helpView];
    [self.view addSubview:self.aboutView];
    NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
    [notificationCenter addObserver:self
                           selector:@selector(viewDidBecomeActive)
                               name:UIApplicationDidBecomeActiveNotification
                             object:nil];
    [notificationCenter addObserver:self
                           selector:@selector(viewDidResignActive)
                               name:UIApplicationDidEnterBackgroundNotification
                             object:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
