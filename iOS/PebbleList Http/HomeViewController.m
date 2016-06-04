//
//  FirstViewController.m
//  PebbleList Http
//
//  Created by Andrea Cerra on 31/05/16.
//  Copyright © 2016 Andrea Cerra. All rights reserved.
//

#import "HomeViewController.h"

@interface HomeViewController ()

@end

@implementation HomeViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self initPebble];
}

- (void) viewDidAppear:(BOOL)animated{
    [self loadDataFromPlist];
}

- (void) loadDataFromPlist {
    
    if (urlsArray == NULL) {
        urlsArray = [NSMutableArray new];
    }else{
        [urlsArray removeAllObjects];
    }
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *plistPath = [documentsDirectory stringByAppendingPathComponent:@"urls.plist"];

    NSMutableArray *array = [[NSMutableArray alloc] initWithContentsOfFile:plistPath];
        
    for (int i = 0; i < [array count]; i++) {
        
        NSDictionary *object = [array objectAtIndex:i];
        
        PLUrl *url = [PLUrl new];
        [url setName:[object objectForKey:@"name"]];
        [url setUrl:[object objectForKey:@"url"]];
        
        [urlsArray addObject:url];
    }
    
    [self.tablelViewUrls reloadData];
}

#pragma mark Pebble delegate

- (void) initPebble {
    
    // Wait for connection
    UIActivityIndicatorView *activityIndicator = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(0, 0, 20, 20)];
    [activityIndicator setColor:[UIColor darkGrayColor]];
    [activityIndicator startAnimating];
    
    UIBarButtonItem * barButton = [[UIBarButtonItem alloc] initWithCustomView:activityIndicator];
    self.navigationItem.leftBarButtonItem = barButton;
    
    // Set UUID of watchapp
    NSUUID *myAppUUID =[[NSUUID alloc] initWithUUIDString:@"b3578af5-8a89-4a1d-9437-060a0b481c9e"];
    [PBPebbleCentral defaultCentral].appUUID = myAppUUID;
    
    _central = [PBPebbleCentral defaultCentral];
    _central.appUUID = myAppUUID;
    [_central run];
    
    [PBPebbleCentral defaultCentral].delegate = self;
    
    [[PBPebbleCentral defaultCentral] run];
}

- (void)pebbleCentral:(PBPebbleCentral*)central watchDidConnect:(PBWatch*)watch isNew:(BOOL)isNew {
    
    NSLog(@"Pebble connected: %@", [watch name]);
    
    // Keep a reference to this watch
    self.watch = watch;

    UIBarButtonItem *refresh = [[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh
                                                                            target:self
                                                                            action:@selector(uploadListToPebble:)];
    self.navigationItem.leftBarButtonItem = refresh;
    
    self.connectionLabel.text = @"Connected";
}

- (void)pebbleCentral:(PBPebbleCentral*)central watchDidDisconnect:(PBWatch*)watch {
    
    NSLog(@"Pebble disconnected: %@", [watch name]);
    
    // If this was the recently connected watch, forget it
    if ([watch isEqual:self.watch]) {
        self.watch = nil;
    }
    
    // Wait for connection
    UIActivityIndicatorView *activityIndicator = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(0, 0, 20, 20)];
    [activityIndicator setColor:[UIColor darkGrayColor]];
    [activityIndicator startAnimating];
    
    UIBarButtonItem * barButton = [[UIBarButtonItem alloc] initWithCustomView:activityIndicator];
    self.navigationItem.leftBarButtonItem = barButton;
    
    self.connectionLabel.text = @"Disconnected";
}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)uploadListToPebble:(id)sender {
    
    // Send always 5 elements
    NSMutableArray *arrayElements = [urlsArray mutableCopy];
    NSInteger count = 5 - [urlsArray count];
    
    NSLog(@"%@", arrayElements);
    NSLog(@"%ld", count);
    
    // Add empty elements
    for (int i = 0; i < count; i++){
        PLUrl *url = [PLUrl new];
        [url setName:@"Empty"];
        [url setUrl:@"--"];
        [arrayElements addObject:url];
    }
    
    NSLog(@"%@", arrayElements);
    
    for (int i = 0; i < [arrayElements count]; i++) {
        
        PLUrl *url = [arrayElements objectAtIndex:i];
        
        NSDictionary *update = @{ @(1):url.name,
                                  @(2):url.url};
        
        [self.watch appMessagesPushUpdate:update onSent:^(PBWatch *watch, NSDictionary *update, NSError *error) {
            if (!error) {
                NSLog(@"Successfully sent message.");
            } else {
                NSLog(@"Error sending message: %@", error);
            }
        }];
    }
}

#pragma mark UITableView delegate

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    return [urlsArray count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    
    static NSString *CellIdentifier = @"urlCell";
    
    PLUrl *url = [urlsArray objectAtIndex:indexPath.row];
    
    UrlTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
    cell.nameField.text = url.name;
    cell.urlField.text = url.url;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    PLUrl *url = [urlsArray objectAtIndex:indexPath.row];
    
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:[NSString stringWithFormat:@"Do you want to delete url: %@ ?", url.name]
                                                             delegate:self
                                                    cancelButtonTitle:@"Dismiss"
                                               destructiveButtonTitle:@"Delete"
                                                    otherButtonTitles:nil];
    
    actionSheet.tag = indexPath.row;
    [actionSheet showInView:self.view];
    
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

#pragma mark UIActionSheet delegate

-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (buttonIndex == 0) {
        
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documentsDirectory = [paths objectAtIndex:0];
        NSString *plistPath = [documentsDirectory stringByAppendingPathComponent:@"urls.plist"];
        
        NSMutableArray *array = [[NSMutableArray alloc] initWithContentsOfFile:plistPath];
        
        // Remove element
        [array removeObjectAtIndex:actionSheet.tag];
        
        // Save data
        [array writeToFile:plistPath atomically:YES];
    
        // Also remove it locally and reload data
        [urlsArray removeObjectAtIndex:actionSheet.tag];
        [self.tablelViewUrls reloadData];
    }
}

#pragma mark Navigation 
- (IBAction)addUrlAction:(id)sender {
    
    if ([urlsArray count] == 5) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Attention"
                                                        message:@"In this demo app you cannot add more than 5 elements to the list"
                                                       delegate:self
                                              cancelButtonTitle:@"Ok"
                                              otherButtonTitles:nil];
        [alert show];
    }else{
        UIViewController *addController = [self.storyboard instantiateViewControllerWithIdentifier:@"urlAddController"];
        [self presentViewController:addController animated:YES completion:nil];
    }
}

@end
