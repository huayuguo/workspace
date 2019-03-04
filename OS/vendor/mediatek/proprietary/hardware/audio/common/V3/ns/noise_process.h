#ifndef noise_process_h
#define noise_process_h

#ifdef __cplusplus 
extern "C" {
#endif

#include "noise_suppression.h"

enum nsLevel {
    kLow,
    kModerate,
    kHigh,
    kVeryHigh
};

typedef struct NSHandle {
	NsHandle **handles;
	int16_t *frame_buffer;
	int channels;
	int samples;
	int init_ok;
} NSHandle_t;

int ns_init(NSHandle_t *handle, int sample_rate, int channels, enum nsLevel level);
int ns_uninit(NSHandle_t *handle);
int ns_process(NSHandle_t *handle, int16_t *buffer, uint64_t frames);

#ifdef __cplusplus 
}
#endif

#endif