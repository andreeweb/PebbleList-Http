#include <pebble.h>

// Keys
typedef enum {
  AppKeyReady = 0,
  AppKeyName,
  AppKeyUrl,
  AppKeySend
} AppKey;

// List max size
#define SIZE 5
// List key used for save inside pebble with function persist_write_data()
static const uint32_t keys[SIZE] = {1001, 1002, 1003, 1004, 1005};

// List values
typedef struct url {
  char name[32];
  char url[256];
} Url;

static Url *urls[5];

// Prorotypes
static void generate_menu_list();

// View objects
static Window *window;
static MenuLayer *s_menu_layer;
static Layer *window_layer;
static TextLayer *text_layer;
static uint32_t count = 0;

// Largest expected inbox and outbox message sizes
const uint32_t inbox_size = 2048;
const uint32_t outbox_size = 512;

// JS loaded
static bool s_js_ready;

// Return true if js is loaded
bool comm_is_js_ready() {
  return s_js_ready;
}

// Comunication handlers
static void inbox_received_handler(DictionaryIterator *iter, void *context) {

  Tuple *ready_tuple = dict_find(iter, AppKeyReady);
  if(ready_tuple) {

    // PebbleKit JS is ready! Safe to send messages
    s_js_ready = true;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "PebbleKit JS is ready! Safe to send messages");

  }else{

    Url *url = NULL;

    // Receive from companion App first URL Name and later URL signature

    Tuple *name_tuple = dict_find(iter, AppKeyName);
    if(name_tuple && comm_is_js_ready()) {

      url = malloc(sizeof(Url));

      static char s_buff[24];
      snprintf(s_buff, sizeof(s_buff), "Loading %lu of %d", count, SIZE);
      text_layer_set_text(text_layer, s_buff);

      char *name = name_tuple->value->cstring;
      snprintf(url->name, sizeof(url->name), "%s", name);

      APP_LOG(APP_LOG_LEVEL_DEBUG, "Loaded URL Name: %s", url->name);
    }

    Tuple *url_tuple = dict_find(iter, AppKeyUrl);
    if(url_tuple && comm_is_js_ready()) {

      char *url_scheme = url_tuple->value->cstring;
      snprintf(url->url, sizeof(url->url), "%s", url_scheme);

      APP_LOG(APP_LOG_LEVEL_DEBUG, "Loaded URL: %s", url->url);

      // Save value inside pebble
      persist_write_data(keys[count], url, sizeof(Url));

      // Save pointer
      urls[count] = url;
      count++;
    }

    if (count == SIZE) {
      APP_LOG(APP_LOG_LEVEL_DEBUG, "Render menu list");
      generate_menu_list();
      count = 0;
    }
  }
}

static void inbox_dropped_callback(AppMessageResult reason, void *context) {
  // A message was received, but had to be dropped
  APP_LOG(APP_LOG_LEVEL_ERROR, "Message dropped. Reason: %d", (int)reason);
}

// Menu handlers
static uint16_t get_num_rows_callback(MenuLayer *menu_layer,
                                      uint16_t section_index, void *context) {
  const uint16_t num_rows = SIZE;
  return num_rows;
}

static void draw_row_callback(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *context) {

  Url *url = urls[cell_index->row];
  static char t_buff[16];
  static char s_buff[32];

  snprintf(t_buff, sizeof(t_buff), "%s", url->name);
  snprintf(s_buff, sizeof(s_buff), "%s", url->url);

  // Draw this row's index
  menu_cell_basic_draw(ctx, cell_layer, t_buff, s_buff, NULL);
}

static int16_t get_cell_height_callback(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *context) {
  const int16_t cell_height = 44;
  return cell_height;
}

static void select_callback(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *context) {

  // Do something in response to the button press

  Url *url = urls[cell_index->row];

  DictionaryIterator* dictionaryIterator = NULL;
  app_message_outbox_begin (&dictionaryIterator);
  dict_write_cstring (dictionaryIterator, AppKeyUrl, url->url);
  dict_write_end (dictionaryIterator);
  app_message_outbox_send();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Make HTTP request for url: %s", url->url);

}

static void generate_menu_list(){

  // If not exit create else reload data

  if (s_menu_layer == NULL) {

    GRect bounds = layer_get_bounds(window_layer);

    // Create the MenuLayer
    s_menu_layer = menu_layer_create(bounds);

    // Let it receive click events
    menu_layer_set_click_config_onto_window(s_menu_layer, window);

    // Set the callbacks for behavior and rendering
    menu_layer_set_callbacks(s_menu_layer, NULL, (MenuLayerCallbacks) {
        .get_num_rows = get_num_rows_callback,
        .draw_row = draw_row_callback,
        .get_cell_height = get_cell_height_callback,
        .select_click = select_callback,
    });

    // Add to the Window
    layer_add_child(window_layer, menu_layer_get_layer(s_menu_layer));

  }else{

    menu_layer_reload_data(s_menu_layer);
  }
}

// Init
static void window_load(Window *window) {

  window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  text_layer = text_layer_create(GRect(0, 72, bounds.size.w, 20));
  text_layer_set_text(text_layer, "List empty");
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));

  //Check persistent data
  bool flag = false;
  for (size_t i = 0; i < SIZE; i++) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "Check exist %lu", keys[i]);
    if(persist_exists(keys[i])){

      Url *url = malloc(sizeof(Url));

      persist_read_data(keys[i], url, sizeof(Url));

      APP_LOG(APP_LOG_LEVEL_DEBUG, "Persistent data founded: %s", url->name);

      urls[i] = url;
      flag = true;
    }
  }

  // something in memory, reload menu
  if (flag) {
    generate_menu_list();
  }
}

static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
  menu_layer_destroy(s_menu_layer);
}

static void init(void) {

  window = window_create();
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);

  // Register to be notified about inbox dropped events
  app_message_register_inbox_dropped(inbox_dropped_callback);

  app_message_register_inbox_received(inbox_received_handler);
  app_message_open(inbox_size, outbox_size);
}

static void deinit(void) {
  window_destroy(window);
}

int main(void) {

  init();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", window);

  app_event_loop();
  deinit();
}
