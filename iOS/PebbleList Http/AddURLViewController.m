//
//  AddURLViewController.m
//  PebbleList Http
//
//  Created by Andrea Cerra on 31/05/16.
//  Copyright © 2016 Andrea Cerra. All rights reserved.
//

#import "AddURLViewController.h"

@interface AddURLViewController ()

@end

@implementation AddURLViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)cancelAction:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)saveAction:(id)sender {
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *plistPath = [documentsDirectory stringByAppendingPathComponent:@"urls.plist"];
    
    NSMutableArray *array = [[NSMutableArray alloc] initWithContentsOfFile:plistPath];
    
    if (![self textFieldHasText:self.nameText]) {
        [self.nameText setText:@"NO_NAME"];
    }
    
    NSDictionary *plistDict = [[NSDictionary alloc] initWithObjects:[NSArray arrayWithObjects:
                                                                     self.nameText.text,
                                                                     self.urlText.text, nil]
                                                            forKeys:[NSArray arrayWithObjects:
                                                                     @"name",
                                                                     @"url", nil]];
    
    // Add new object with others
    [array addObject:plistDict];
    
    // Save data
    [array writeToFile:plistPath atomically:YES];
    [self dismissViewControllerAnimated:YES completion:nil];
}

//remove white spaces and check if textfield has text
- (BOOL) textFieldHasText:(UITextField*)textfield {
    
    NSString *t1= [textfield.text stringByReplacingOccurrencesOfString:@" " withString:@""];
    if ([t1 length] > 0)
        return true;
    else
        return false;
}

@end
