/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */


//#import <BaiduMapAPI/BMapKit.h>
#import <BaiduMapAPI_Search/BMKSearchComponent.h>

@interface UZBMKBusLineSearch : BMKBusLineSearch

@property (nonatomic, strong) NSString *routId;
@property (nonatomic, assign) BOOL showNode, autoFitVisible;

@end
