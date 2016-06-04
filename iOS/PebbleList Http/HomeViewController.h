//
//  FirstViewController.h
//  PebbleList Http
//
//  Created by Andrea Cerra on 31/05/16.
//  Copyright © 2016 Andrea Cerra. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PebbleKit/PebbleKit.h"
#import "UrlTableViewCell.h"
#import "PLUrl.h"

@interface HomeViewController : UIViewController <PBPebbleCentralDelegate, UIActionSheetDelegate> {
    NSMutableArray *urlsArray;
}

@property (weak, nonatomic) PBWatch *watch;
@property (weak, nonatomic) PBPebbleCentral *central;

@property (weak, nonatomic) IBOutlet UITableView *tablelViewUrls;
@property (weak, nonatomic) IBOutlet UILabel *connectionLabel;

@end

