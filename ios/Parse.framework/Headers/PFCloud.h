/**
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <Foundation/Foundation.h>

#import <Bolts/BFTask.h>

#import <Parse/PFConstants.h>

PF_ASSUME_NONNULL_BEGIN

/*!
 The `PFCloud` class provides methods for interacting with Parse Cloud Functions.
 */
@interface PFCloud : NSObject

/*!
 @abstract Calls the given cloud function *synchronously* with the parameters provided.

 @param function The function name to call.
 @param parameters The parameters to send to the function.

 @returns The response from the cloud function.
 */
+ (PF_NULLABLE_S id)callFunction:(NSString *)function
                  withParameters:(PF_NULLABLE NSDictionary *)parameters PF_SWIFT_UNAVAILABLE;

/*!
 @abstract Calls the given cloud function *synchronously* with the parameters provided and
 sets the error if there is one.

 @param function The function name to call.
 @param parameters The parameters to send to the function.
 @param error Pointer to an `NSError` that will be set if necessary.

 @returns The response from the cloud function.
 This result could be a `NSDictionary`, an `NSArray`, `NSNumber` or `NSString`.
 */
+ (PF_NULLABLE_S id)callFunction:(NSString *)function
                  withParameters:(PF_NULLABLE NSDictionary *)parameters
                           error:(NSError **)error;

/*!
 @abstract Calls the given cloud function *asynchronously* with the parameters provided.

 @param function The function name to call.
 @param parameters The parameters to send to the function.

 @returns The task, that encapsulates the work being done.
 */
+ (BFTask PF_GENERIC(id) *)callFunctionInBackground:(NSString *)function
                                     withParameters:(PF_NULLABLE NSDictionary *)parameters;

/*!
 @abstract Calls the given cloud function *asynchronously* with the parameters provided
 and executes the given block when it is done.

 @param function The function name to call.
 @param parameters The parameters to send to the function.
 @param block The block to execute when the function call finished.
 It should have the following argument signature: `^(id result, NSError *error)`.
 */
+ (void)callFunctionInBackground:(NSString *)function
                  withParameters:(PF_NULLABLE NSDictionary *)parameters
                           block:(PF_NULLABLE PFIdResultBlock)block;

/*
 @abstract Calls the given cloud function *asynchronously* with the parameters provided
 and then executes the given selector when it is done.

 @param function The function name to call.
 @param parameters The parameters to send to the function.
 @param target The object to call the selector on.
 @param selector The selector to call when the function call finished.
 It should have the following signature: `(void)callbackWithResult:(id)result error:(NSError *)error`.
 Result will be `nil` if error is set and vice versa.
 */
+ (void)callFunctionInBackground:(NSString *)function
                  withParameters:(PF_NULLABLE NSDictionary *)parameters
                          target:(PF_NULLABLE_S id)target
                        selector:(PF_NULLABLE_S SEL)selector;

@end

PF_ASSUME_NONNULL_END
