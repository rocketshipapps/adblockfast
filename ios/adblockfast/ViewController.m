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
#import "StatusLabel.h"
#import "ActionLabel.h"
#import "Button.h"
#import "Overlay.h"

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
#define DISALLOWED_STATUS_MESSAGE @"Waiting for permission"
#define BLOCKED_STATUS_MESSAGE @"Blocking ads"
#define UNBLOCKED_STATUS_MESSAGE @"Not blocking ads"
#define DISALLOWED_ACTION_HINT @"Tap to get help"
#define BLOCKED_ACTION_HINT @"Tap to unblock"
#define UNBLOCKED_ACTION_HINT @"Tap to block"
#define HELP_LABEL @"Help"
#define ABOUT_LABEL @"About"
#define CLOSE_LABEL @"Okay"
#define OVERLAY_LINE_SPACING 2
#define LIGHT_COLOR_R (255. / 255)
#define LIGHT_COLOR_G (255. / 255)
#define LIGHT_COLOR_B (255. / 255)
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
#define MINIMUM_FRAME_DIMENSION_TO_NAVBAR_HEIGHT (16. / 3)
#define MINIMUM_FRAME_DIMENSION_TO_HEADING_FONT_SIZE 16.
#define MINIMUM_FRAME_DIMENSION_TO_BODY_FONT_SIZE (320. / 13)
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
@property (nonatomic) StatusLabel *disallowedStatusLabel;
@property (nonatomic) StatusLabel *blockedStatusLabel;
@property (nonatomic) StatusLabel *unblockedStatusLabel;
@property (nonatomic) UIButton *onOffButton;
@property (nonatomic) ActionLabel *disallowedActionLabel;
@property (nonatomic) ActionLabel *blockedActionLabel;
@property (nonatomic) ActionLabel *unblockedActionLabel;
@property (nonatomic) Button *helpButton;
@property (nonatomic) Button *aboutButton;
@property (nonatomic) UIColor *lightColor;
@property (nonatomic) NSMutableAttributedString *helpOverlayText;
@property (nonatomic) NSAttributedString *closeButtonText;
@property (nonatomic) CGFloat closeButtonHeight;
@property (nonatomic) CGFloat helpOverlayHeight;
@property (nonatomic) Overlay *helpOverlay;
@property (nonatomic) NSMutableAttributedString *aboutOverlayText;
@property (nonatomic) CGFloat aboutOverlayHeight;
@property (nonatomic) Overlay *aboutOverlay;
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
    Overlay *overlay = self.helpOverlay;

    if (!isOpen) {
        self.isHelpOpen = YES;
        overlay.alpha = 1;
    }

    CGFloat frameWidth = self.view.frame.size.width;
    CGFloat width = frameWidth - 2 * frameWidth / FRAME_WIDTH_TO_MARGIN;
    CGFloat height = self.helpOverlayHeight;
    CGFloat heightDuringBounce = frameWidth / width * height;
    [UIView animateWithDuration:isOpen ? MICRO_DURATION : SMALL_DURATION
                          delay:delay
                        options:UIViewAnimationOptionCurveEaseIn
                     animations:^{
                         overlay.transform =
                             CGAffineTransformScale(
                                                    overlay.transform,
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
                         overlay.transform = CGAffineTransformScale(
                                                                    overlay.transform,
                                                                    width / frameWidth,
                                                                    height / heightDuringBounce
                                                                    );
                     }
                     completion:nil];
}

- (void)closeHelp
{
    CGFloat delay = SMALL_DURATION + MICRO_DURATION;
    Overlay *overlay = self.helpOverlay;
    CGFloat frameWidth = self.view.frame.size.width;
    [UIView animateWithDuration:delay
                     animations:^{
                         overlay.transform =
                             CGAffineTransformScale(
                                                    overlay.transform,
                                                    1. /
                                                        (frameWidth -
                                                             2 * frameWidth /
                                                                 FRAME_WIDTH_TO_MARGIN),
                                                    1. / self.helpOverlayHeight
                                                    );
                     }];
    dispatch_after(
                   dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_SEC),
                   dispatch_get_main_queue(),
                   ^{
                       self.helpOverlay.alpha = 0;
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
    Overlay *overlay = self.aboutOverlay;

    if (!isOpen) {
        self.isAboutOpen = YES;
        overlay.alpha = 1;
    }

    CGFloat frameWidth = self.view.frame.size.width;
    CGFloat width = frameWidth - 2 * frameWidth / FRAME_WIDTH_TO_MARGIN;
    CGFloat height = self.aboutOverlayHeight;
    CGFloat heightDuringBounce = frameWidth / width * height;
    [UIView animateWithDuration:isOpen ? MICRO_DURATION : SMALL_DURATION
                          delay:delay
                        options:UIViewAnimationOptionCurveEaseIn
                     animations:^{
                         overlay.transform =
                             CGAffineTransformScale(
                                                    overlay.transform,
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
                         overlay.transform = CGAffineTransformScale(
                                                                    overlay.transform,
                                                                    width / frameWidth,
                                                                    height / heightDuringBounce
                                                                    );
                     }
                     completion:nil];
}

- (void)closeAbout
{
    CGFloat delay = SMALL_DURATION + MICRO_DURATION;
    Overlay *overlay = self.aboutOverlay;
    CGFloat frameWidth = self.view.frame.size.width;
    [UIView animateWithDuration:delay
                     animations:^{
                         overlay.transform =
                             CGAffineTransformScale(
                                                    overlay.transform,
                                                    1. /
                                                        (frameWidth -
                                                             2 * frameWidth /
                                                                 FRAME_WIDTH_TO_MARGIN),
                                                    1. / self.aboutOverlayHeight
                                                    );
                     }];
    dispatch_after(
                   dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_SEC),
                   dispatch_get_main_queue(),
                   ^{
                       self.aboutOverlay.alpha = 0;
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
        _nameLabel.text = NAME;
        _nameLabel.font = self.headingFont;
        _nameLabel.textColor = self.darkColor;
        _nameLabel.textAlignment = NSTextAlignmentCenter;
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

- (StatusLabel *)disallowedStatusLabel
{
    _disallowedStatusLabel ||
        (_disallowedStatusLabel =
             [[StatusLabel alloc] initWithYTerminus:self.onOffButtonFrame.origin.y
                                              width:self.view.frame.size.width
                                            message:DISALLOWED_STATUS_MESSAGE
                                               font:self.bodyFont
                                           boldFont:self.bodyBoldFont
                                              color:self.darkColor]);
    return _disallowedStatusLabel;
}

- (StatusLabel *)blockedStatusLabel
{
    _blockedStatusLabel ||
        (_blockedStatusLabel =
             [[StatusLabel alloc] initWithYTerminus:self.onOffButtonFrame.origin.y
                                              width:self.view.frame.size.width
                                            message:BLOCKED_STATUS_MESSAGE
                                               font:self.bodyFont
                                           boldFont:self.bodyBoldFont
                                              color:self.darkColor]);
        return _blockedStatusLabel;
}

- (StatusLabel *)unblockedStatusLabel
{
    _unblockedStatusLabel ||
        (_unblockedStatusLabel =
             [[StatusLabel alloc] initWithYTerminus:self.onOffButtonFrame.origin.y
                                              width:self.view.frame.size.width
                                            message:UNBLOCKED_STATUS_MESSAGE
                                               font:self.bodyFont
                                           boldFont:self.bodyBoldFont
                                              color:self.darkColor]);
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

- (ActionLabel *)disallowedActionLabel
{
    if (!_disallowedActionLabel) {
        CGRect onOffButtonFrame = self.onOffButtonFrame;
        _disallowedActionLabel =
            [[ActionLabel alloc] initWithYOrigin:onOffButtonFrame.origin.y +
                                                     onOffButtonFrame.size.height
                                           width:self.view.frame.size.width
                                            hint:DISALLOWED_ACTION_HINT
                                            font:self.bodyFont
                                           color:self.darkColor];
    }

    return _disallowedActionLabel;
}

- (ActionLabel *)blockedActionLabel
{
    if (!_blockedActionLabel) {
        CGRect onOffButtonFrame = self.onOffButtonFrame;
        _blockedActionLabel =
            [[ActionLabel alloc] initWithYOrigin:onOffButtonFrame.origin.y +
                                                     onOffButtonFrame.size.height
                                           width:self.view.frame.size.width
                                            hint:BLOCKED_ACTION_HINT
                                            font:self.bodyFont
                                           color:self.darkColor];
    }

    return _blockedActionLabel;
}

- (ActionLabel *)unblockedActionLabel
{
    if (!_unblockedActionLabel) {
        CGRect onOffButtonFrame = self.onOffButtonFrame;
        _unblockedActionLabel =
            [[ActionLabel alloc] initWithYOrigin:onOffButtonFrame.origin.y +
                                                     onOffButtonFrame.size.height
                                           width:self.view.frame.size.width
                                            hint:UNBLOCKED_ACTION_HINT
                                            font:self.bodyFont
                                           color:self.darkColor];
    }

    return _unblockedActionLabel;
}

- (Button *)helpButton
{
    if (!_helpButton) {
        _helpButton = [[Button alloc] initWithFrameSize:self.view.frame.size
                                  minimumFrameDimension:self.minimumFrameDimension
                                                  index:0
                                                  label:HELP_LABEL
                                                   font:self.headingFont
                                                  color:self.darkColor];
        [_helpButton addTarget:self
                        action:@selector(openHelp)
              forControlEvents:UIControlEventTouchUpInside];
    }

    return _helpButton;
}

- (Button *)aboutButton
{
    if (!_aboutButton) {
        _aboutButton = [[Button alloc] initWithFrameSize:self.view.frame.size
                                   minimumFrameDimension:self.minimumFrameDimension
                                                   index:2
                                                   label:ABOUT_LABEL
                                                    font:self.headingFont
                                                   color:self.darkColor];
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

- (NSMutableAttributedString *)helpOverlayText
{
    if (!_helpOverlayText) {
        NSUInteger fontSize =
            self.minimumFrameDimension / MINIMUM_FRAME_DIMENSION_TO_BODY_FONT_SIZE;
        _helpOverlayText =
            [ViewController markdownToAttributedString:[self.preferences stringForKey:HELP_TEXT_KEY]
                                              fontName:BODY_FONT_NAME
                                          boldFontName:BODY_BOLD_FONT_NAME
                                              fontSize:fontSize
                                             textColor:self.lightColor];
        NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
        [paragraphStyle setLineSpacing:OVERLAY_LINE_SPACING];
        [paragraphStyle setHeadIndent:HEAD_INDENT_TO_FONT_SIZE * fontSize];
        [_helpOverlayText addAttribute:NSParagraphStyleAttributeName
                                 value:paragraphStyle
                                 range:NSMakeRange(0, _helpOverlayText.length)];
    }

    return _helpOverlayText;
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

- (CGFloat)helpOverlayHeight
{
    if (!_helpOverlayHeight) {
        CGFloat frameWidth = self.view.frame.size.width;
        CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
        _helpOverlayHeight =
            2 * margin +
                [self.helpOverlayText boundingRectWithSize:CGSizeMake(
                                                                      frameWidth - 4 * margin,
                                                                      CGFLOAT_MAX
                                                                      )
                                                   options:NSStringDrawingUsesLineFragmentOrigin
                                                   context:nil].size.height +
                    self.closeButtonHeight;
    }

    return _helpOverlayHeight;
}

- (Overlay *)helpOverlay
{
    if (!_helpOverlay) {
        CGFloat height = self.helpOverlayHeight;
        _helpOverlay = [[Overlay alloc] initWithFrameSize:self.view.frame.size
                                                   height:height
                                                     text:self.helpOverlayText];
        CGFloat frameWidth = self.view.frame.size.width;
        CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
        CGFloat width = frameWidth - 2 * margin;
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
        closeButton.layer.cornerRadius = margin / MARGIN_TO_CORNER_RADIUS;
        closeButton.layer.borderColor = self.lightColor.CGColor;
        [closeButton setAttributedTitle:closeButtonText forState:UIControlStateNormal];
        [_helpOverlay addSubview:closeButton];
        [_helpOverlay addGestureRecognizer:
            [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeHelp)]];
        [closeButton addGestureRecognizer:
            [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeHelp)]];
    }

    return _helpOverlay;
}

- (NSMutableAttributedString *)aboutOverlayText
{
    if (!_aboutOverlayText) {
        NSUInteger fontSize =
            self.minimumFrameDimension / MINIMUM_FRAME_DIMENSION_TO_BODY_FONT_SIZE;
        _aboutOverlayText =
            [ViewController markdownToAttributedString:[self.preferences
                                                           stringForKey:ABOUT_TEXT_KEY]
                                              fontName:BODY_FONT_NAME
                                          boldFontName:BODY_BOLD_FONT_NAME
                                              fontSize:fontSize
                                             textColor:self.lightColor];
        NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
        [paragraphStyle setLineSpacing:OVERLAY_LINE_SPACING];
        [paragraphStyle setHeadIndent:HEAD_INDENT_TO_FONT_SIZE * fontSize];
        [_aboutOverlayText addAttribute:NSParagraphStyleAttributeName
                                  value:paragraphStyle
                                  range:NSMakeRange(0, _aboutOverlayText.length)];
    }

    return _aboutOverlayText;
}

- (CGFloat)aboutOverlayHeight
{
    if (!_aboutOverlayHeight) {
        CGFloat frameWidth = self.view.frame.size.width;
        CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
        _aboutOverlayHeight =
            2 * margin +
                [self.aboutOverlayText boundingRectWithSize:CGSizeMake(
                                                                       frameWidth - 4 * margin,
                                                                       CGFLOAT_MAX
                                                                       )
                                                    options:NSStringDrawingUsesLineFragmentOrigin
                                                    context:nil].size.height +
                    self.closeButtonHeight;
    }

    return _aboutOverlayHeight;
}

- (Overlay *)aboutOverlay
{
    if (!_aboutOverlay) {
        CGFloat height = self.aboutOverlayHeight;
        _aboutOverlay =
            [[Overlay alloc] initWithFrameSize:self.view.frame.size
                                        height:height
                                          text:self.aboutOverlayText];
        CGFloat frameWidth = self.view.frame.size.width;
        CGFloat margin = frameWidth / FRAME_WIDTH_TO_MARGIN;
        CGFloat width = frameWidth - 2 * margin;
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
        closeButton.layer.cornerRadius = margin / MARGIN_TO_CORNER_RADIUS;
        closeButton.layer.borderColor = self.lightColor.CGColor;
        [closeButton setAttributedTitle:closeButtonText forState:UIControlStateNormal];
        [_aboutOverlay addSubview:closeButton];
        [_aboutOverlay addGestureRecognizer:
            [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeAbout)]];
        [closeButton addGestureRecognizer:
            [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeAbout)]];;
    }

    return _aboutOverlay;
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
                self.disallowedStatusLabel.alpha = self.nameLabel.alpha = self.helpOverlay.alpha =
                    self.aboutOverlay.alpha = 0;
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
    [self.view addSubview:self.helpOverlay];
    [self.view addSubview:self.aboutOverlay];
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
